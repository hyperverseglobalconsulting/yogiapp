package com.example.yogiapp.controllers;

import android.content.Context;
import android.util.Log;

import com.example.yogiapp.utils.ConfigUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPTController {

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    private static final String TAG = "ChatGPTController";

    private String apiKey;

    public interface ChatGPTCallback {
        void onSuccess(String jsonResponse);
        void onError(Exception e);
    }

    public ChatGPTController(Context context) {
        apiKey = ConfigUtil.loadConfig(context, "openai_config.json").optString("api_key", "");
    }

    public void callChatGPT(String commandText, final ChatGPTCallback callback) {
        String systemPrompt =
                "You are a controller for the Yogi App that can interact with video and audio streams. " +
                "The app can perform the following actions:\n" +
                "  - capture_snapshot(): Captures a still image from the video stream.\n" +
                "  - start_video(): Starts recording video from the video stream until a stop command is received.\n" +
                "  - stop_video(): Stops video recording, finalizes the file, and uploads it.\n" +
                "  - scan_barcode(image): Scans a barcode from the given image.\n" +
                "  - scan_qrcode(image): Scans a QR code from the given image.\n" +
                "  - search_amazon(image): Searches Amazon for product details based on the image.\n" +
                "  - post_social_media(image): Posts the image to social media.\n" +
                "  - search_person(name): Searches for the specified person in the video stream (using known images) and returns the last seen time.\n\n" +
                "When you receive a command, return a JSON object with two keys: 'action' and an optional 'params' object. " +
                "Return only the JSON without any extra explanation. For example:\n" +
                "  {\"action\": \"capture_snapshot\", \"params\": {}}\n" +
                "or if starting video:\n" +
                "  {\"action\": \"start_video\", \"params\": {}}\n" +
                "or if searching for a person:\n" +
                "  {\"action\": \"search_person\", \"params\": {\"name\": \"Nikola Jokić\"}}";

        String userPrompt = "The command is: " + commandText;

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-4");

            JSONArray messages = new JSONArray();
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.put(systemMessage);

            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);
            messages.put(userMessage);

            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.2);
            jsonBody.put("max_tokens", 150);
        } catch (JSONException e) {
            callback.onError(e);
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON_MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }
                String responseBody = response.body().string();
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    JSONArray choices = jsonResponse.getJSONArray("choices");
                    if (choices.length() > 0) {
                        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                        String content = message.getString("content").trim();
                        callback.onSuccess(content);
                    } else {
                        callback.onError(new Exception("No choices in ChatGPT response."));
                    }
                } catch (JSONException e) {
                    callback.onError(e);
                }
            }
        });
    }
}
