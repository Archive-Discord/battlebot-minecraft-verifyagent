package kr.battlebot.battlebotverifyagent.core;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;
import java.util.logging.*;
import javax.crypto.BadPaddingException;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;
import kr.battlebot.battlebotverifyagent.model.Verify;
import kr.battlebot.battlebotverifyagent.model.VerifyEvent;
import kr.battlebot.battlebotverifyagent.model.VerifyListener;
import kr.battlebot.battlebotverifyagent.utils.crypto.RSA;
import org.bukkit.Bukkit;

public class VerifyReciver extends Thread {

    /** The logger instance. */
    private static final Logger LOG = Logger.getLogger("Battlebot Verify Agent");

    private final BattlebotVerifyAgent plugin;

    /** The host to listen on. */
    private final String host;

    /** The port to listen on. */
    private final int port;

    /** The server socket. */
    private ServerSocket server;

    /** The running flag. */
    private boolean running = true;

    public VerifyReciver(final BattlebotVerifyAgent plugin, String host, int port)
            throws Exception {
        this.plugin = plugin;
        this.host = host;
        this.port = port;

        initialize();
    }

    private void initialize() throws Exception {
        try {
            server = new ServerSocket();
            server.bind(new InetSocketAddress(host, port));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE,
                    "Error initializing verify receiver. Please verify that the configured");
            LOG.log(Level.SEVERE,
                    "IP address and port are not already in use. This is a common problem");
            LOG.log(Level.SEVERE,
                    "with hosting services and, if so, you should check with your hosting provider.",
                    ex);
            throw new Exception(ex);
        }
    }

    public void shutdown() {
        running = false;
        if (server == null)
            return;
        try {
            server.close();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "Unable to shut down Battlebot Verify  receiver cleanly.");
        }
    }

    @Override
    public void run() {

        // Main loop.
        while (running) {
            try {
                Socket socket = server.accept();
                socket.setSoTimeout(5000); // Don't hang on slow connections.
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream()));
                InputStream in = socket.getInputStream();

                // Read the 256 byte block.
                byte[] block = new byte[256];
                in.read(block, 0, block.length);

                // Decrypt the block.
                block = RSA.decrypt(block, BattlebotVerifyAgent.getInstance().getKeyPair()
                        .getPrivate());
                int position = 0;

                // Perform the opcode check.
                String opcode = readString(block, position);
                position += opcode.length() + 1;

                if (!opcode.equals("VERIFY")) {
                    // Something went wrong in RSA.
                    throw new Exception("Unable to decode RSA");
                }



                String userId = readString(block, position);
                UUID userIdUUID = UUID.fromString(userId);
                position += userId.length() + 1;
                String discordId = readString(block, position);
                position += discordId.length() + 1;
                String timeStamp = readString(block, position);

                final Verify verify = new Verify();
                verify.setUserId(userIdUUID);
                verify.setDiscordId(discordId);
                verify.setTimeStamp(timeStamp);

                for (VerifyListener listener : BattlebotVerifyAgent.getInstance()
                        .getListeners()) {
                    try {
                        listener.verifyMade(verify);
                    } catch (Exception ex) {
                        String vlName = listener.getClass().getSimpleName();
                        LOG.log(Level.WARNING,
                                "Exception caught while sending the verify notification to the '"
                                        + vlName + "' listener", ex);
                    }
                }

                BattlebotVerifyAgent.plugin.getServer().getScheduler()
                        .scheduleSyncDelayedTask(plugin, new Runnable() {
                            public void run() {
                                Bukkit.getServer().getPluginManager()
                                        .callEvent(new VerifyEvent(verify));
                            }
                        });

                writer.write("SUCCESS");
                writer.flush();

                // Clean up.
                writer.close();
                in.close();
                socket.close();
            } catch (SocketException ex) {
                LOG.log(Level.WARNING, "Protocol error. Ignoring packet - "
                        + ex.getLocalizedMessage());
            } catch (BadPaddingException ex) {
                LOG.log(Level.WARNING,
                        "Unable to decrypt verify record. Make sure that that your public key");
                LOG.log(Level.WARNING,
                        "matches the one you gave the server list.", ex);
            } catch (Exception ex) {
                LOG.log(Level.WARNING,
                        "Exception caught while receiving a verify notification",
                        ex);
            }
        }
    }

    /**
     * Reads a string from a block of data.
     *
     * @param data
     *            The data to read from
     * @return The string
     */
    private String readString(byte[] data, int offset) {
        StringBuilder builder = new StringBuilder();
        for (int i = offset; i < data.length; i++) {
            if (data[i] == '\n')
                break; // Delimiter reached.
            builder.append((char) data[i]);
        }
        return builder.toString();
    }
}
