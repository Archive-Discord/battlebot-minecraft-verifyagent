package kr.battlebot.battlebotverifyagent.utils.http;

import java.util.Map;
public class HttpRequestData {
    private String url;
    private String requestMethod;
    private String requestBodyJson;
    private Map<String, String> headers;

    public HttpRequestData(String url, String requestMethod, String requestBodyJson, Map<String, String> headers) {
        this.url = url;
        this.requestMethod = requestMethod;
        this.requestBodyJson = requestBodyJson;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getRequestBodyJson() {
        return requestBodyJson;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
