package com.test.template.models.enums;

public enum BranchPermission {

    NONE("None"), //No branching. Everything except public
    REQUEST_ONLY("Request only"), //Branching only from approved users. Only on private and personal
    FRIENDS_ONLY("Friends only"), //Branching only from creator friends. Only on private and personal
    GROUP_ONLY("Group only"), //Defaults on group access
    ALL("All"); //Applicable on all public statuses

    private final String permission;

    BranchPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean applicableToPrivateAccess() {
        return NONE.equals(this) || REQUEST_ONLY.equals(this) || FRIENDS_ONLY.equals(this);
    }

    public boolean applicableToPersonalAccess() {
        return NONE.equals(this) || REQUEST_ONLY.equals(this);
    }

}
