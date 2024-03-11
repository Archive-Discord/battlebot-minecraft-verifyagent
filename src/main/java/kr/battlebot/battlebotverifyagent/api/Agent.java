package kr.battlebot.battlebotverifyagent.api;

import kr.battlebot.battlebotverifyagent.utils.http.HttpRequestData;
import kr.battlebot.battlebotverifyagent.utils.http.HttpUtils;
import org.json.simple.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Agent {
    public static String baseURL = "https://api.battlebot.kr";
    public boolean checkOnline() {
        try {
            URL url = new URL(baseURL);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            int code = connection.getResponseCode();

            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<String> generateVerifyURL(String uuid) {
        Map<String, String> headers = new HashMap<>(); // 필요에 따라 헤더 추가
        headers.put("Content-Type", "application/json"); // JSON 형식으로 요청을 보냄
        headers.put("Authorization", Authorization.token);

        JSONObject body = new JSONObject();
        body.put("uuid", uuid);

        HttpRequestData requestData = new HttpRequestData(baseURL + "/minecraft/verify", "POST", body.toJSONString(), headers);
        CompletableFuture<String> future = HttpUtils.sendAsyncHttpRequest(requestData);

        return future;
    }
}
