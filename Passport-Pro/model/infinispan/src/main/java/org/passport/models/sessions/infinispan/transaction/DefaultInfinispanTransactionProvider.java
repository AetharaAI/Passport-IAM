/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.models.sessions.infinispan.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.passport.common.util.Retry;
import org.passport.models.AbstractPassportTransaction;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionTask;
import org.passport.models.PassportTransaction;
import org.passport.models.sessions.infinispan.changes.PersistentSessionsWorker;
import org.passport.models.utils.PassportModelUtils;

import org.infinispan.commons.util.concurrent.AggregateCompletionStage;
import org.infinispan.commons.util.concurrent.CompletionStages;

/**
 * A {@link PassportTransaction} that collects {@link NonBlockingTransaction} to commit/rollback in a non-blocking
 * fashion.
 * <p>
 * This class is not thread-safe.
 */
public class DefaultInfinispanTransactionProvider extends AbstractPassportTransaction implements InfinispanTransactionProvider {

    private final List<NonBlockingTransaction> transactionList = new ArrayList<>(4);
    private final PassportSession session;

    public DefaultInfinispanTransactionProvider(PassportSession session) {
        this.session = session;
    }

    @Override
    public void registerTransaction(NonBlockingTransaction transaction) {
        transactionList.add(Objects.requireNonNull(transaction));
    }

    @Override
    public void close() {
        transactionList.clear();
    }

    @Override
    protected void commitImpl() {
        final AggregateCompletionStage<Void> stage = CompletionStages.aggregateCompletionStage();
        final DatabaseWrites databaseWrites = new DatabaseWrites();

        // sends all the cache requests and queues any pending database writes.
        transactionList.forEach(transaction -> transaction.asyncCommit(stage, databaseWrites));

        // all the cache requests has been sent
        // apply the database changes in a blocking fashion, and in a single transaction.
        commitDatabaseUpdates(databaseWrites);

        // finally, wait for the completion of the cache updates.
        CompletionStages.join(stage.freeze());
    }

    @Override
    protected void rollbackImpl() {
        final AggregateCompletionStage<Void> stage = CompletionStages.aggregateCompletionStage();
        transactionList.forEach(transaction -> transaction.asyncRollback(stage));
        CompletionStages.join(stage.freeze());
    }

    private void commitDatabaseUpdates(DatabaseWrites databaseWrites) {
        if (databaseWrites.isEmpty()) {
            return;
        }
        Retry.executeWithBackoff(
                iteration -> PassportModelUtils.runJobInTransaction(session.getPassportSessionFactory(), databaseWrites),
                (iteration, t) -> {
                    if (iteration > 20) {
                        // never retry more than 20 times
                        throw new RuntimeException("Maximum number of retries reached", t);
                    }
                }, PersistentSessionsWorker.UPDATE_TIMEOUT, PersistentSessionsWorker.UPDATE_BASE_INTERVAL_MILLIS);
    }

    private static class DatabaseWrites implements PassportSessionTask, Consumer<DatabaseUpdate> {
        private final List<DatabaseUpdate> databaseUpdateList = new ArrayList<>(2);

        boolean isEmpty() {
            return databaseUpdateList.isEmpty();
        }

        @Override
        public void run(PassportSession session) {
            databaseUpdateList.forEach(update -> update.write(session));
        }

        @Override
        public void accept(DatabaseUpdate databaseUpdate) {
            databaseUpdateList.add(databaseUpdate);
        }

        @Override
        public String getTaskName() {
            return "Database Update";
        }
    }
}
