package com.test.template.models.enums;

public enum ProgressionStatus {

    NOT_STARTED("Not started"), //Default when created. Cannot be set again
    IN_PROGRESS("In progress"), //Set after a step status is changed. Overrides NOT_STARTED, ON_HOLD, ABANDONED
    COMPLETED("Completed"), //Only set if all steps are marked as COMPLETED
    PARTIALLY_COMPLETED("Partially completed"), //Used if ABANDONED steps are present but all others are COMPLETED
    ABANDONED("Abandoned"), //Can be applied manually to template. Also represents irrelevant or unwanted steps
    ON_HOLD("On hold"); //Can only be applied to templates. Removed automatically if any step status is changed

    private final String status;

    ProgressionStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isNotStarted() {
        return NOT_STARTED.equals(this);
    }

    public boolean isInProgress() {
        return IN_PROGRESS.equals(this);
    }

    public boolean isOnHold() {
        return ON_HOLD.equals(this);
    }

    public boolean isAbandoned() {
        return ABANDONED.equals(this);
    }

    public boolean isCompleted() {
        return COMPLETED.equals(this);
    }

    public boolean canAbandon() {
        return !isNotStarted();
    }
    
    public boolean isEffectivelyCompleted() {
        return COMPLETED.equals(this) || PARTIALLY_COMPLETED.equals(this) || ABANDONED.equals(this);
    }

    public boolean isNotApplicableToStep() {
        return ON_HOLD.equals(this) || PARTIALLY_COMPLETED.equals(this);
    }

    public boolean isTemplateLevel() {
        return ABANDONED.equals(this) || ON_HOLD.equals(this);
    }

}
