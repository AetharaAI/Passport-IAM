package org.passport.models.workflow;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.passport.models.PassportSession;
import org.passport.models.PassportTransaction;

import org.jboss.logging.Logger;

final class WorkflowExecutor {

    private static final Logger log = Logger.getLogger(WorkflowExecutor.class);

    private final boolean blocking;
    private final ExecutorService taskExecutor;
    private final long taskTimeout;

    WorkflowExecutor(ExecutorService taskExecutor, boolean blocking, long taskTimeout) {
        this.taskExecutor = taskExecutor;
        this.blocking = blocking;
        this.taskTimeout = taskTimeout;
    }

    void runTask(PassportSession session, Runnable task) {
        enlistTransaction(session, new WorkflowTask(this, task));
    }

    CompletableFuture<Void> submit(WorkflowTask task) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(task, taskExecutor)
                .orTimeout(taskTimeout, TimeUnit.MILLISECONDS)
                .whenComplete((result, error) -> {
                    if (error instanceof TimeoutException) {
                        log.warnf("Timeout occurred while processing workflow task: %s", task);
                    }
                    task.cancel();
                });

        if (blocking) {
            future.join();
        }

        return future;
    }

    private void enlistTransaction(PassportSession session, PassportTransaction transaction) {
        session.getTransactionManager().enlistAfterCompletion(transaction);
    }
}
