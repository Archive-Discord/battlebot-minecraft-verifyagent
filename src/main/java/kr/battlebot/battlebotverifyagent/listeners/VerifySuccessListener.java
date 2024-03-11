package kr.battlebot.battlebotverifyagent.listeners;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;
import kr.battlebot.battlebotverifyagent.model.Verify;
import kr.battlebot.battlebotverifyagent.model.VerifyListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class VerifySuccessListener implements VerifyListener {

    public void verifyMade(Verify verify) {
        BattlebotVerifyAgent.logger.info("Success Discord Verify: " + verify.toString());

        String[] commands = BattlebotVerifyAgent.verifyReceiverCommands;
        String[] broadcastMessages = BattlebotVerifyAgent.verifyReceiverBroadcastMessages;
        String[] playerMessages = BattlebotVerifyAgent.verifyReceiverPlayerMessages;

        Player player = BattlebotVerifyAgent.plugin.getServer().getPlayer(verify.getId());



        if (player != null) {
            for (String command : commands) {
                try {
                    boolean success = BattlebotVerifyAgent.getInstance().getServer().getScheduler().callSyncMethod( BattlebotVerifyAgent.plugin, new Callable<Boolean>() {
                        @Override
                        public Boolean call() {
                            return BattlebotVerifyAgent.getInstance().getServer().dispatchCommand( BattlebotVerifyAgent.plugin.getServer().getConsoleSender(), command.replace("%player%", player.getName()));
                        }
                    } ).get();
                    if (!success) {
                        BattlebotVerifyAgent.logger.warning("Command failed: " + command);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            for (String message : broadcastMessages) {
                BattlebotVerifyAgent.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", player.getName())));
            }

            for (String message : playerMessages) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("%player%", player.getName())));
            }
        } else {
            BattlebotVerifyAgent.logger.warning("Player not found: " + verify.getId());
        }
    }
}
