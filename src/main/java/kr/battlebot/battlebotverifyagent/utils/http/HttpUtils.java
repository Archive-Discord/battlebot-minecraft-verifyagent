    package kr.battlebot.battlebotverifyagent.utils.http;

    import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;

    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.DataOutputStream;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.util.Map;
    import java.util.concurrent.CompletableFuture;

    public class HttpUtils {
        public static CompletableFuture<String> sendAsyncHttpRequest(HttpRequestData requestData) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    URL apiUrl = new URL(requestData.getUrl());
                    HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                    connection.setRequestMethod(requestData.getRequestMethod());

                    // 요청 헤더 추가
                    for (Map.Entry<String, String> entry : requestData.getHeaders().entrySet()) {
                        connection.setRequestProperty(entry.getKey(), entry.getValue());
                    }

                    // 요청 바디 추가
                    if (requestData.getRequestBodyJson() != null && !requestData.getRequestBodyJson().isEmpty()) {
                        connection.setDoOutput(true);
                        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                            outputStream.writeBytes(requestData.getRequestBodyJson());
                            outputStream.flush();
                        }
                    }

                    int responseCode = connection.getResponseCode();
                    if (responseCode >= 400) {
                        // 에러 응답 처리
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                        StringBuilder errorResponse = new StringBuilder();
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }
                        errorReader.close();
                        connection.disconnect();

                        // 에러 응답값을 JSON으로 리턴
                        throw new RuntimeException(errorResponse.toString());
                    } else {
                        // 정상 응답 처리
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        connection.disconnect();

                        return response.toString();
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            });
        }
    }
