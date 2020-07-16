package org.simulationautomation.util;

import com.google.gson.Gson;

public class JSONUtil {

  private Gson gson;
  private static JSONUtil INSTANCE;

  private JSONUtil() {
    gson = new Gson();
  }

  public static JSONUtil getInstance() {

    if (INSTANCE == null) {
      INSTANCE = new JSONUtil();
    }
    return INSTANCE;
  }

  public String toJson(Object src) {

    return gson.toJson(src);
  }

  public <T> T fromJson(String json, Class<T> clazz) {
    return gson.fromJson(json, clazz);
  }

}
