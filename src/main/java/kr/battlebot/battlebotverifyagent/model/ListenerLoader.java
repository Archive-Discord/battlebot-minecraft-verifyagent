package kr.battlebot.battlebotverifyagent.model;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;

import java.io.File;
import java.net.*;
import java.util.*;
import java.util.logging.*;
public class ListenerLoader {
    public static List<VerifyListener> load(String directory) {
        List<VerifyListener> listeners = new ArrayList<VerifyListener>();
        File dir = new File(directory);

        if (!dir.exists()) {
            BattlebotVerifyAgent.logger.info(
                    "No listeners loaded! Cannot find listener directory '"
                            + dir + "' ");
            return listeners;
        }

        ClassLoader loader;
        try {
            loader = new URLClassLoader(new URL[] { dir.toURI().toURL() },
                    VerifyListener.class.getClassLoader());
        } catch (MalformedURLException ex) {
            BattlebotVerifyAgent.logger.info("Error while configuring listener class loader" + ex);
            return listeners;
        }
        for (File file : dir.listFiles()) {
            if (!file.getName().endsWith(".class")) {
                continue; // Only load class files!
            }
            String name = file.getName().substring(0,
                    file.getName().lastIndexOf("."));

            try {
                Class<?> clazz = loader.loadClass(name);
                Object object = clazz.newInstance();
                if (!(object instanceof VerifyListener)) {
                    BattlebotVerifyAgent.logger.info("Not a verify listener: " + clazz.getSimpleName());
                    continue;
                }
                VerifyListener listener = (VerifyListener) object;
                listeners.add(listener);
                BattlebotVerifyAgent.logger.info("Loaded verify listener: "
                        + listener.getClass().getSimpleName());
            }
            /*
             * Catch the usual definition and dependency problems with a loader
             * and skip the problem listener.
             */
            catch (Exception ex) {
                BattlebotVerifyAgent.logger.info("Error loading '" + name
                        + "' listener! Listener disabled.");
            } catch (Error ex) {
                BattlebotVerifyAgent.logger.info("Error loading '" + name
                        + "' listener! Listener disabled.");
            }
        }
        return listeners;
    }
}
