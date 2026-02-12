package com.damda.global.util;

import com.damda.global.exception.BaseException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static com.damda.global.exception.ErrorCode.BAD_REQUEST_INVALID_DATE_FORMAT;

public class StringUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDateTime toLocalDateTime(String date) {
        if(date.isBlank()){
            return null;
        }

        try {
            return LocalDateTime.parse(date, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BaseException(BAD_REQUEST_INVALID_DATE_FORMAT);
        }
    }

    public static LocalDate toLocalDate(String date) {
        if(date.isBlank()){
            return null;
        }

        try {
            return LocalDate.parse(date, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new BaseException(BAD_REQUEST_INVALID_DATE_FORMAT);
        }
    }
}
