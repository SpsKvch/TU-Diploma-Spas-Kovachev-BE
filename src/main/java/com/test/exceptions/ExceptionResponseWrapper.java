package com.test.exceptions;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponseWrapper {

    private String exceptionName;
    private List<String> messages;
    private List<String> stackTrace;

}
