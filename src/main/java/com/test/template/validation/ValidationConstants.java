package com.test.template.validation;

public class ValidationConstants {

  //Validation Error Messages

  //Used for initial request values validation
  public static final int DEFAULT_CONTENT_CONSTRAINT = 500;
  public static final int MAX_TEMPLATE_CONTENT_LENGTH = 1000;
  public static final int MAX_TEMPLATE_TITLE_LENGTH = 100;
  public static final int MAX_STEP_CONTENT_LENGTH = 2000;
  public static final int MAX_STEP_TITLE_LENGTH = 100;
  public static final int MIN_STEP_COUNT = 2;
  public static final int MAX_STEPS_COUNT = 25;
  public static final int MAX_REQUIREMENT_LENGTH = 100;

  //Template
  public static final String TEMPLATE_TITLE_ERROR_MESSAGE =
      "Template title cannot be blank or exceed " + MAX_TEMPLATE_TITLE_LENGTH + " characters";
  public static final String TEMPLATE_CONTENT_ERROR_MESSAGE =
      "Template content be blank or exceed " + MAX_TEMPLATE_CONTENT_LENGTH + " characters";

  //Step
  public static final String NULL_STEPS_ERROR_MESSAGE = "A template must contain steps";
  public static final String NO_STEPS_ERROR_MESSAGE = "No steps provided";
  public static final String STEP_MIN_LENGTH_ERROR_MESSAGE = "Template must have at least %d steps";
  public static final String STEP_MAX_LENGTH_ERROR_MESSAGE = "Template cannot have more than %d steps";
  public static final String STEP_BLANK_ERROR_MESSAGE = "%s of step %d cannot be blank";
  public static final String STEP_LENGTH_ERROR_MESSAGE = "%s of step %d cannot exceed %d characters";
  public static final String STEP_TITLE_ERROR_MESSAGE =
      "Step title cannot be blank or exceed " + MAX_STEP_TITLE_LENGTH + " characters";
  public static final String STEP_CONTENT_ERROR_MESSAGE =
      "Step content cannot be blank or exceed " + MAX_STEP_CONTENT_LENGTH + " characters";
  public static final String NO_CATEGORY_ERROR_MESSAGE = "A template must belong to a category";
  public static final String NO_ACCESS_STATUS_ERROR_MESSAGE = "Access status cannot have a null value";
  public static final String NO_BRANCH_PERMISSION_ERROR_MESSAGE = "Branch permission cannot have a null value";

  //Requirement
  public static final String REQUIREMENT_CONTENT_ERROR_MESSAGE =
      "Requirement content cannot be blank or exceed " + MAX_REQUIREMENT_LENGTH + " characters";
}
