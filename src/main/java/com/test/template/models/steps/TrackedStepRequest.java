package com.test.template.models.steps;

import com.test.template.models.enums.ProgressionStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.test.template.validation.ValidationConstants.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackedStepRequest {

    @Size(max = MAX_STEP_CONTENT_LENGTH, message = STEP_TITLE_ERROR_MESSAGE)
    private String title;
    @Size( max = MAX_STEP_CONTENT_LENGTH, message = STEP_CONTENT_ERROR_MESSAGE)
    private String markedUpContent;
    private String markedUpImage;
    private List<String> notes;
    @NotNull
    private Duration timeSpent;
    private ProgressionStatus progressionStatus;

}
