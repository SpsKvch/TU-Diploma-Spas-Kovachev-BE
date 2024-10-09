package com.test.utils;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.core.Local;

@Slf4j
public class DateTimeParser {

  public static LocalDate parseDate(String date) {
    if (StringUtils.isBlank(date)) {
      return null;
    }

    try {
      return LocalDate.parse(date);
    } catch (DateTimeParseException e) {
      log.info("Unable to parse LocalDate for value '{}'", date);
      return null;
    }
  }

  public static Duration parseDuration(String duration) {
    if (StringUtils.isBlank(duration)) {
      return null;
    }

    try {
      return Duration.parse(duration);
    } catch (DateTimeParseException e) {
      log.info("Unable to parse duration for value '{}'", duration);
      return null;
    }
  }

}
