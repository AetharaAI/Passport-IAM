package org.passport.models.workflow;

import java.util.Objects;

import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.PassportSessionTask;

import static org.passport.models.utils.PassportModelUtils.runJobInTransaction;

public abstract class WorkflowTransactionalTask implements Runnable, PassportSessionTask {

    private final PassportSessionFactory sessionFactory;
    private final PassportContext context;

    public WorkflowTransactionalTask(PassportSession session) {
        Objects.requireNonNull(session, "PassportSession must not be null");
        this.sessionFactory = session.getPassportSessionFactory();
        this.context = session.getContext();
    }

    @Override
    public void run() {
        runJobInTransaction(sessionFactory, context, this);
    }
}
