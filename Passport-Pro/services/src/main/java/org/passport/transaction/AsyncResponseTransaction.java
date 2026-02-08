package org.passport.transaction;

import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.core.Response;

import org.passport.models.PassportSession;
import org.passport.models.PassportTransaction;
import org.passport.models.PassportTransactionManager;
import org.passport.services.ErrorPage;
import org.passport.services.messages.Messages;

/**
 * When using {@link AsyncResponse#resume(Object)} directly in the code, the response is returned before all changes 
 * done withing this execution are committed. Therefore we need some mechanism that resumes the AsyncResponse after all
 * changes are successfully committed. This can be achieved by enlisting an instance of AsyncResponseTransaction into
 * the main transaction using {@link org.passport.models.PassportTransactionManager#enlistAfterCompletion(PassportTransaction)}.
 */
public class AsyncResponseTransaction implements PassportTransaction {

    private final PassportSession session;
    private final AsyncResponse responseToFinishInTransaction;
    private final Response responseToSend;

    /**
     * This method creates a new AsyncResponseTransaction instance that resumes provided AsyncResponse
     * {@code responseToFinishInTransaction} with given Response {@code responseToSend}. The transaction is enlisted 
     * to {@link PassportTransactionManager}.
     *
     * @param session Current PassportSession
     * @param responseToFinishInTransaction AsyncResponse to be resumed on {@link PassportTransactionManager} commit/rollback.
     * @param responseToSend Response to be sent
     */
    public static void finishAsyncResponseInTransaction(PassportSession session, AsyncResponse responseToFinishInTransaction, Response responseToSend) {
        session.getTransactionManager().enlistAfterCompletion(new AsyncResponseTransaction(session, responseToFinishInTransaction, responseToSend));
    }
    
    private AsyncResponseTransaction(PassportSession session, AsyncResponse responseToFinishInTransaction, Response responseToSend) {
        this.session = session;
        this.responseToFinishInTransaction = responseToFinishInTransaction;
        this.responseToSend = responseToSend;
    }

    @Override
    public void begin() {

    }

    @Override
    public void commit() {
        responseToFinishInTransaction.resume(responseToSend);
    }

    @Override
    public void rollback() {
        responseToFinishInTransaction.resume(ErrorPage.error(session, null, Response.Status.INTERNAL_SERVER_ERROR, Messages.INTERNAL_SERVER_ERROR));
    }

    @Override
    public void setRollbackOnly() {

    }

    @Override
    public boolean getRollbackOnly() {
        return false;
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
