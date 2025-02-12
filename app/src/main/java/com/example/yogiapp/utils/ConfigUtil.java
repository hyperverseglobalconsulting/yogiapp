package com.example.yogiapp.utils;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;

public class ConfigUtil {
    public static JSONObject loadConfig(Context context, String fileName) {
        String jsonString = "";
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
            return new JSONObject(jsonString);
        } catch (IOException | JSONException ex) {
            ex.printStackTrace();
            return new JSONObject();
        }
    }
}
