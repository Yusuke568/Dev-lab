package com.example.common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

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
	
	public static StringBuilder getJsonString(List<?> beanList, String... keys) {
	    StringBuilder json = new StringBuilder("[");
	    for (int i = 0; i < beanList.size(); i++) {
	        Object bean = beanList.get(i);
	        json.append("{");
	        for (int j = 0; j < keys.length; j++) {
	            String key = keys[j];
	            try {
	                Method method = bean.getClass().getMethod("get" + capitalize(key));
	                Object value = method.invoke(bean);
	                json.append("\"").append(key).append("\":\"").append(value).append("\"");
	            } catch (Exception e) {
	                json.append("\"").append(key).append("\":\"\"");
	            }
	            if (j < keys.length - 1) {
	                json.append(",");
	            }
	        }
	        json.append("}");
	        if (i < beanList.size() - 1) {
	            json.append(",");
	        }
	    }
	    json.append("]");
	    return json;
	}

	private static String capitalize(String str) {
	    if (str == null || str.isEmpty()) return str;
	    return str.substring(0, 1).toUpperCase() + str.substring(1);
	}




}