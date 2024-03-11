package kr.battlebot.battlebotverifyagent.api;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;

import kr.battlebot.battlebotverifyagent.command.BattlebotAgentCommand;
import kr.battlebot.battlebotverifyagent.utils.BattlebotLog;
import kr.battlebot.battlebotverifyagent.utils.http.HttpRequestData;
import kr.battlebot.battlebotverifyagent.utils.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static kr.battlebot.battlebotverifyagent.api.Agent.baseURL;
import static kr.battlebot.battlebotverifyagent.utils.http.HttpUtils.sendAsyncHttpRequest;

public class Authorization {
    private static final Logger logger = BattlebotVerifyAgent.logger;
    public static String token;
    public CompletableFuture<String> isAuthorized() {
        Map<String, String> headers = new HashMap<>(); // 필요에 따라 헤더 추가
        headers.put("Content-Type", "application/json"); // JSON 형식으로 요청을 보냄
        headers.put("Authorization", token);

        HttpRequestData requestData = new HttpRequestData(baseURL + "/minecraft/auth/me", "GET", null, headers);
        CompletableFuture<String> future = sendAsyncHttpRequest(requestData);

        return future;
    }

    public CompletableFuture<String> registerServer() {
        Map<String, String> headers = new HashMap<>(); // 필요에 따라 헤더 추가
        headers.put("Content-Type", "application/json"); // JSON 형식으로 요청을 보냄
        headers.put("Authorization", token);

        JSONObject body = new JSONObject();
        body.put("publicKey", BattlebotVerifyAgent.publicKey);
        body.put("host", BattlebotVerifyAgent.verifyReceiverHost);
        body.put("port", BattlebotVerifyAgent.verifyReceiverPort);

        HttpRequestData requestData = new HttpRequestData(baseURL + "/minecraft/auth/register", "POST", body.toJSONString(), headers);
        CompletableFuture<String> future = sendAsyncHttpRequest(requestData);

        return future;
    }

    public boolean authorizeAuthToken(CommandSender sender, String authorizeToken) {
        if(BattlebotAgentCommand.hasPermission(sender, "login")) {
            String lastToken = token;
            token = authorizeToken;

            sender.sendMessage(
                    BattlebotLog.log("배틀이 봇 서버와 연결을 시도합니다...")
            );

            registerServer().thenAccept(response -> {
                boolean isSaved = BattlebotVerifyAgent.core.save();
                if(isSaved) {
                    sender.sendMessage(BattlebotLog.log("배틀이 봇 서버와 성공적으로 연결되었습니다."));
                } else {
                    sender.sendMessage(BattlebotLog.error("로그인 정보 저장에 실패했습니다. 다시 시도해주세요."));
                }
            }).exceptionally(e -> {
                try {
                    Throwable exception = e.getCause();
                    if (exception != null) {
                        JSONObject error = utils.getJsonResponse(exception.getMessage());
                        boolean hasMessage = error.containsKey("message");
                        BattlebotVerifyAgent.logger.info(error.toJSONString());
                        sender.sendMessage(BattlebotLog.error(hasMessage ? "배틀이 봇 서버와 연결에 실패했습니다. " + "(" + error.get("message").toString() + ")" : "배틀이 봇 서버와 연결에 실패했습니다."));
                    } else {
                        sender.sendMessage(BattlebotLog.error("배틀이 봇 서버와 연결에 실패했습니다."));
                    }
                } catch (Exception ex) {
                    sender.sendMessage(
                            BattlebotLog.error("배틀이 봇 서버와 연결에 실패했습니다.")
                    );
                }

                token = lastToken;

                return null;
            });

            return true;
        } else {
            sender.sendMessage(BattlebotLog.error("권한이 없습니다."));
        }

        return true;
    }

    public void importConfig(YamlConfiguration config) {
        String accessToken = config.getString("credentials.token", null);

        if (accessToken == null) {
            logger.warning("Battlebot Agent Token Not Found. Please Setting Battlebot Agent Connect");
        } else {
            token = accessToken;
        }
    }

    public void exportConfig(YamlConfiguration config) {
        config.set("credentials.token", token);
    }
}
