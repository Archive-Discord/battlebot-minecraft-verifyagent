package kr.battlebot.battlebotverifyagent.command;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;
import kr.battlebot.battlebotverifyagent.utils.BattlebotLog;
import kr.battlebot.battlebotverifyagent.utils.utils;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BattlebotAgentCommand {

    public static boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission("battlebotagent." + node);
    }

    public static List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> result = new ArrayList<>();
        String commandName = label.toLowerCase();

        int currentInput = args.length - 1;
        String currentArg = args[currentInput];

        if (commandName.equals("battlebot")) {
            if (currentInput == 0) {
                for (BattlebotAgentCommandAction action : BattlebotAgentCommandAction.values()) {
                    if (action.hasPermission(sender)) {
                        result.add(action.getCommand());
                    }
                }
            }
        }

        return utils.searchList(result, currentArg);
    }

    public static boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = label.toLowerCase();

        if (commandName.equals("battlebot")) {
            if (args.length >= 1) {
                BattlebotAgentCommandAction action = BattlebotAgentCommandAction.getAction(args[0]);
                if (action == null) {
                    sender.sendMessage(
                            BattlebotLog.error("올바르지 않은 명령입니다.")
                    );
                    return true;
                }

                switch (action) {
                    case LOGIN:
                    {
                        if (args.length == 1) {
                            sender.sendMessage(BattlebotLog.error("서버를 연동하려면, "+ ChatColor.GREEN+"/battlebot login [토큰]"+ChatColor.RESET+" 명령어를 입력해주세요. 아래 링크를 눌러 토큰을 발급받으세요."));
                            sender.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "https://battlebot.kr/dashboard?redirect=/minecraft");
                            return true;
                        } else if (args.length == 2) {
                            return authorize(sender, args[1]);
                        } else {
                            sender.sendMessage(BattlebotLog.error("올바르지 않은 명령어 입니다."));
                        }
                    }
                        break;
                    case CONNECTION:
                    {
                        return serverConnection(sender);
                    }
                    case HELP:
                    default:
                        sender.sendMessage(
                                BattlebotLog.log("배틀이 인증 클라이언트 - 버전: " + BattlebotVerifyAgent.version)
                        );
                        sender.sendMessage(
                                BattlebotAgentCommandAction.getAllManual(sender, label, "")
                        );
                        return true;
                }
            } else {
                return onCommand(sender, command, label, new String[]{"help"});
            }

            return true;
        } else if (commandName.equals("디스코드인증") || commandName.equals("verifydiscord")) {
            return getVerifyURL(sender);
        }

        return false;
    }

    private static boolean authorize(CommandSender sender, String token) {
        if(BattlebotAgentCommand.hasPermission(sender, "login")) {
            if (!BattlebotVerifyAgent.core.agent.checkOnline()) {
                sender.sendMessage(
                        BattlebotLog.error("배틀이 봇 서버가 오프라인 이거나, 점검 중입니다. 아래 링크를 눌러 서비스 상태를 확인해 주세요.")
                );
                sender.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + "https://status.battlebot.kr");
            } else {
                return BattlebotVerifyAgent.core.authorization.authorizeAuthToken(sender, token);
            }
        } else {
            sender.sendMessage(
                    BattlebotLog.error("해당 명령어를 사용할 권한이 없습니다.")
            );
        }

        return true;
    }

    private static boolean getVerifyURL(CommandSender sender) {
        if (BattlebotAgentCommand.hasPermission(sender, "verify")) {
            sender.sendMessage(
                    BattlebotLog.log("디스코드 접속 링크를 생성 중입니다. 잠시만 기다려주세요.")
            );

            String uuid = Bukkit.getPlayer(sender.getName()).getUniqueId().toString();
            CompletableFuture<String> VerifyURL = BattlebotVerifyAgent.core.agent.generateVerifyURL(uuid);

            VerifyURL.thenAccept(response -> {
                try {
                    JSONObject verifyCode = utils.getJsonResponse(response);
                    String baseURL = "https://battlebot.kr/connect/minecraft?token=";

                    ComponentBuilder message = new ComponentBuilder(BattlebotLog.log("" + ChatColor.AQUA + ChatColor.UNDERLINE + "[여기]" + ChatColor.RESET + "를 클릭하여 디스코드 계정을 연동해주세요."))
                            .event(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL, baseURL + verifyCode.get("data")));

                    sender.spigot().sendMessage(message.create());

                } catch (ParseException e) {
                    sender.sendMessage(
                            BattlebotLog.error("디스코드 인증 링크를 생성하는 중 오류가 발생했습니다.")
                    );
                }
            }).exceptionally(e -> {
                try {
                    JSONObject error = utils.getJsonResponse(e.getMessage());
                    boolean hasMessage = error.containsKey("message");
                    sender.sendMessage(
                            BattlebotLog.error(hasMessage ? error.get("message").toString() : "디스코드 인증 링크를 생성하는 중 오류가 발생했습니다.")
                    );
                } catch (ParseException ex) {
                    sender.sendMessage(
                            BattlebotLog.error("디스코드 인증 링크를 생성하는 중 오류가 발생했습니다.")
                    );
                }
                return null;
            });
        } else {
            sender.sendMessage(
                    BattlebotLog.error("해당 명령어를 사용할 권한이 없습니다.")
            );
        }

        return true;
    }

    private static boolean serverConnection(CommandSender sender) {
        if (BattlebotAgentCommand.hasPermission(sender, "connection")) {
            sender.sendMessage(
                    BattlebotLog.log("배틀이 서버 상태를 확인 중입니다...")
            );

            StringBuilder message = new StringBuilder();

            message.append(BattlebotLog.log("배틀이 인증 클라이언트 - 버전: " + BattlebotVerifyAgent.version) + "\n");

            String headerStatus = ChatColor.RESET + "배틀이 서버 상태: ";

            if (BattlebotVerifyAgent.core.agent.checkOnline()) {
                message.append(headerStatus).append(ChatColor.GREEN).append("온라인\n");
            } else {
                message.append(headerStatus).append(ChatColor.RED).append("오프라인\n");
            }

            String headerLoginStatus = ChatColor.RESET + "로그인 상태: ";

            BattlebotVerifyAgent.core.authorization.isAuthorized().thenAccept(response -> {
                message.append(headerLoginStatus).append(ChatColor.GREEN).append("로그인 완료\n");
                sender.sendMessage(message.toString());
            }).exceptionally(e -> {
                try {
                    Throwable exception = e.getCause();
                    if (exception != null) {
                        JSONObject error = utils.getJsonResponse(exception.getMessage());
                        boolean hasMessage = error.containsKey("message");
                        BattlebotVerifyAgent.logger.info(error.toJSONString());

                        message.append(headerLoginStatus).append(ChatColor.RED)
                                .append(hasMessage ? "로그인 되지 않음" + " (" + error.get("message").toString() + ")\n" : "로그인 되지 않음\n");
                        sender.sendMessage(message.toString());
                    } else {
                        message.append(headerLoginStatus).append(ChatColor.RED).append("로그인 되지 않음\n");
                        sender.sendMessage(message.toString());
                    }
                } catch (Exception ex) {
                    message.append(headerLoginStatus).append(ChatColor.RED).append("로그인 되지 않음\n");
                    sender.sendMessage(message.toString());

                }
                return null;
            });

        } else {
            sender.sendMessage(BattlebotLog.error("권한이 없습니다."));
        }

        return true;
    }
}
