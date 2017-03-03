package com.anthony.runwithme;

import com.google.gson.Gson;

/**
 * Created by asus on 2016/12/3.
 */

public class GsonService {

    public static <T> T parseJson(String jsonString, Class<T> clazz)
    {
        T t = null;
        try {
            Gson gson = new Gson();
            t = gson.fromJson(jsonString, clazz);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            System.out.println("无法转换json");
        }
        return t;

    }
}
