package kr.battlebot.battlebotverifyagent;

import kr.battlebot.battlebotverifyagent.command.BattlebotAgentCommand;
import kr.battlebot.battlebotverifyagent.core.AgentCore;
import kr.battlebot.battlebotverifyagent.core.VerifyReciver;
import kr.battlebot.battlebotverifyagent.listeners.VerifySuccessListener;
import kr.battlebot.battlebotverifyagent.model.ListenerLoader;
import kr.battlebot.battlebotverifyagent.model.VerifyListener;
import kr.battlebot.battlebotverifyagent.utils.crypto.RSAIO;
import kr.battlebot.battlebotverifyagent.utils.crypto.RSAKeygen;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;

public final class BattlebotVerifyAgent extends JavaPlugin {
    public static JavaPlugin plugin;
    private static BattlebotVerifyAgent instance;
    public static String version = "";
    public static FileConfiguration config;
    public static Logger logger;
    public static AgentCore core;
    private final List<VerifyListener> listeners = new ArrayList<VerifyListener>();
    private KeyPair keyPair;
    public static String publicKey;
    private VerifyReciver verifyReciver;
    public static String verifyReceiverHost;
    public static Integer verifyReceiverPort;
    public static String[] verifyReceiverCommands;
    public static String[] verifyReceiverPlayerMessages;
    public static String[] verifyReceiverBroadcastMessages;


    @Override
    public void onEnable() {
        BattlebotVerifyAgent.instance = this;
        plugin = this;
        version = this.getDescription().getVersion();

        logger = getLogger();
        logger.info("Battlebot VerifyAgent is starting up...");

        this.saveDefaultConfig();
        config = this.getConfig();

        verifyReceiverPort = config.getInt("receiver.port", 8722);
        verifyReceiverHost = config.getString("receiver.host", "0.0.0.0");
        verifyReceiverCommands = config.getStringList("receiver.command").toArray(new String[0]);
        verifyReceiverPlayerMessages = config.getStringList("receiver.message.player").toArray(new String[0]);
        verifyReceiverBroadcastMessages = config.getStringList("receiver.message.broadcast").toArray(new String[0]);

        File rsaDirectory = new File(getDataFolder() + "/rsa");
        File credentialsFile = new File(getDataFolder(), "credentials.yml");
        String listenerDirectory = getDataFolder().toString()
                .replace("\\", "/") + "/listeners";

        if (!credentialsFile.exists()) {
            try {
                credentialsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (!rsaDirectory.exists()) {
                rsaDirectory.mkdir();
                new File(listenerDirectory).mkdir();
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            logger.warning("Error reading configuration file or RSA keys"+ ex);
            return;
        }

        listenerDirectory = config.getString("listener_folder");
        listeners.add(new VerifySuccessListener());
        listeners.addAll(ListenerLoader.load(listenerDirectory));

        try {
            verifyReciver = new VerifyReciver(this, verifyReceiverHost, verifyReceiverPort);
            verifyReciver.start();

            logger.info("Battlebot Verify receiver has started successfully!");
        } catch (Exception ex) {
            logger.warning("Error starting receiver: " + ex);
            return;
        }


        core = new AgentCore();
        core.setCredentialsFile(credentialsFile);
        core.start();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (verifyReciver != null) {
            verifyReciver.shutdown();
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        return BattlebotAgentCommand.onTabComplete(sender, command, label, args);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return BattlebotAgentCommand.onCommand(sender, command, label, args);
    }

    public static BattlebotVerifyAgent getInstance() {
        return instance;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public List<VerifyListener> getListeners() {
        return listeners;
    }
}
