package com.example.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class MyUtil {
	
	
	 public static Date localDateToDate(LocalDate localDate) {
	        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	    }

	public static String loadSqlFromClasspath(String resourcePath) throws IOException {
	    try (InputStream is = MyUtil.class.getClassLoader().getResourceAsStream(resourcePath);
	         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

	        if (is == null) {
	            throw new FileNotFoundException("Resource not found: " + resourcePath);
	        }

	        StringBuilder sb = new StringBuilder();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line).append(System.lineSeparator());
	        }
	        return sb.toString();
	    }
	}

}