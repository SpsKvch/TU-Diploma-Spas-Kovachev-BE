package com.test.template.models.enums;

public enum AccessStatus {

    PERSONAL("personal"), //Can be accessed by creator and users it has been shared with. No branching.
    PRIVATE("private"), //Can be accessed by creator friends and those it has been shared with. No branching.
    GROUP("group"), //Can be accessed by members in group. Branching only allowed in group
    PUBLIC("public"); //Can be accessed by everyone. Branching allowed.

    private final String status;

    AccessStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isPrivateOrPersonal() {
        return PERSONAL.equals(this) || PRIVATE.equals(this);
    }

}
