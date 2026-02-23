package com.damda.global.util;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateToDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        String dateString = jsonParser.getText();

        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        LocalDate date = LocalDate.parse(dateString.trim(), DATE_FORMATTER);
        return date.atStartOfDay();
    }
}