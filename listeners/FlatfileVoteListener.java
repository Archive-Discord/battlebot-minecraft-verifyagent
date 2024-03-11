import kr.battlebot.battlebotverifyagent.model.Verify;
import kr.battlebot.battlebotverifyagent.model.VerifyListener;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlatfileVerifyListener implements VerifyListener {
    public static final String FILE = "./plugins/BattlebotVerifyAgent/verify.log";
    private static final Logger log = Logger.getLogger("FlatfileVerifyListener");

    @Override
    public void verifyMade(Verify verify) {
        try {
            // Open a buffered writer in append mode.
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE, true));

            writer.write(verify.toString());
            writer.newLine();
            writer.flush();

            // All done.
            writer.close();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Unable to log verify: " + verify);
        }
    }

}