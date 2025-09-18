package com.example.common;

import java.io.IOException;
import java.sql.Time;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class SqlTimeDeserializer extends JsonDeserializer<Time> {
    @Override
    public Time deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String value = jp.getValueAsString();

        if (value == null || value.isEmpty()) {
            return null;
        }

        // "09:00" → "09:00:00" に補正
        if (value.length() == 5) {
            value += ":00";
        }

        try {
            return Time.valueOf(value); // "HH:mm:ss" 形式に変換
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid time format: " + value, e);
        }
    }
}
