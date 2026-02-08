package com.aetherpro.passport.agency;

/**
 * Result of validating a specific mandate for an action
 */
public class MandateValidationResult {
    
    public enum Status {
        /** Mandate permits the action */
        VALID,
        
        /** Mandate does not exist */
        NOT_FOUND,
        
        /** Mandate has expired */
        EXPIRED,
        
        /** Mandate has been revoked */
        REVOKED,
        
        /** Mandate scope doesn't cover this action */
        SCOPE_MISMATCH,
        
        /** Amount exceeds mandate limit */
        AMOUNT_EXCEEDED,
        
        /** Constraint check failed */
        CONSTRAINT_FAILED,
        
        /** Second factor authentication required */
        REQUIRES_2FA,
        
        /** Principal is suspended */
        PRINCIPAL_SUSPENDED
    }
    
    private Status status;
    private String message;
    private String constraintDetails;
    private Double remainingAmount;
    
    public MandateValidationResult() {}
    
    public MandateValidationResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }
    
    public static MandateValidationResult valid() {
        return new MandateValidationResult(Status.VALID, "Mandate is valid for this action");
    }
    
    public static MandateValidationResult notFound() {
        return new MandateValidationResult(Status.NOT_FOUND, "Mandate not found");
    }
    
    public static MandateValidationResult expired() {
        return new MandateValidationResult(Status.EXPIRED, "Mandate has expired");
    }
    
    public static MandateValidationResult revoked() {
        return new MandateValidationResult(Status.REVOKED, "Mandate has been revoked");
    }
    
    public static MandateValidationResult scopeMismatch(String requiredScope) {
        return new MandateValidationResult(
            Status.SCOPE_MISMATCH, 
            "Mandate does not cover scope: " + requiredScope
        );
    }
    
    public static MandateValidationResult amountExceeded(Double limit, Double requested) {
        MandateValidationResult r = new MandateValidationResult(
            Status.AMOUNT_EXCEEDED,
            String.format("Amount %.2f exceeds mandate limit of %.2f", requested, limit)
        );
        r.setRemainingAmount(limit - requested);
        return r;
    }
    
    public static MandateValidationResult constraintFailed(String details) {
        MandateValidationResult r = new MandateValidationResult(
            Status.CONSTRAINT_FAILED,
            "Mandate constraint check failed"
        );
        r.setConstraintDetails(details);
        return r;
    }
    
    public static MandateValidationResult requires2FA() {
        return new MandateValidationResult(
            Status.REQUIRES_2FA, 
            "This mandate requires second factor authentication"
        );
    }
    
    public static MandateValidationResult principalSuspended() {
        return new MandateValidationResult(
            Status.PRINCIPAL_SUSPENDED,
            "The principal associated with this mandate is suspended"
        );
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getConstraintDetails() {
        return constraintDetails;
    }
    
    public void setConstraintDetails(String constraintDetails) {
        this.constraintDetails = constraintDetails;
    }
    
    public Double getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(Double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public boolean isValid() {
        return status == Status.VALID;
    }
}
