package kr.battlebot.battlebotverifyagent.utils;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;
import org.bukkit.ChatColor;

public class BattlebotLog {
    private static final String PREFIX = BattlebotVerifyAgent.config.getString("prefix") != null ? BattlebotVerifyAgent.config.getString("prefix") : "[Battlebot]";
    public static String log(String content) {
        return "" + ChatColor.GREEN + PREFIX + " " + ChatColor.RESET + content;
    }

    public static String warn(String content) {
        return "" + ChatColor.YELLOW + PREFIX + " " + ChatColor.RESET + content;
    }

    public static String error(String content) {
        return "" + ChatColor.RED + PREFIX + " " + ChatColor.RESET + content;
    }
}
