package kr.battlebot.battlebotverifyagent.utils;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class utils {
    public static List<String> searchList(List<String> haystack, String needle) {
        List<String> result = new ArrayList<>();

        for (String res : haystack) {
            if (res.startsWith(needle)) {
                result.add(res);
            }
        }

        return result;
    }

    public static JSONObject getJsonResponse(String response) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        Object obj = jsonParser.parse(response);
        return (JSONObject) obj;
    }
}
