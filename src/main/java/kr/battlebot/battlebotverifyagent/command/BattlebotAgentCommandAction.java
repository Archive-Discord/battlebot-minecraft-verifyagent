package kr.battlebot.battlebotverifyagent.command;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public enum BattlebotAgentCommandAction {
    HELP("help", "", "도움말을 표시합니다"),
    LOGIN("login", "", "배틀이 봇 서버와 연결을 진행합니다."),
    CONNECTION("connection", "", "배틀이 봇 서버와 연결 상태를 확인합니다.");

    String cmdline;
    String usage;
    String explanation;

    BattlebotAgentCommandAction(String cmdline, String usage, String explanation) {
        this.cmdline = cmdline;
        this.usage = usage;
        this.explanation = explanation;
    }

    public String getCommand() {
        return cmdline;
    }

    public boolean hasPermission(CommandSender sender) {
        return BattlebotAgentCommand.hasPermission(sender, this.getCommand());
    }

    public static BattlebotAgentCommandAction getAction(String string) {
        for (BattlebotAgentCommandAction action : BattlebotAgentCommandAction.values()) {
            if (action.getCommand().equalsIgnoreCase(string)) {
                return action;
            }
        }
        return null;
    }

    public static String getAllManual(CommandSender sender, String label, String name) {
        String all = "";

        for (BattlebotAgentCommandAction action : BattlebotAgentCommandAction.values()) {
            if (BattlebotAgentCommand.hasPermission(sender, action.getCommand())) {
                all += action.getManual(label, name)+"\n";
            }
        }

        return all;
    }

    public String getManual(String label, String name) {
        return ChatColor.LIGHT_PURPLE+"/"+label+" "+ChatColor.AQUA+name+" "+ChatColor.YELLOW+this.cmdline+" "+ChatColor.GRAY+this.usage+ChatColor.RESET+" : "+this.explanation;
    }
}
