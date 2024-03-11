package kr.battlebot.battlebotverifyagent.model.listeners;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;
import kr.battlebot.battlebotverifyagent.model.Verify;
import kr.battlebot.battlebotverifyagent.model.VerifyListener;

public class BasicVerifyListener implements VerifyListener {

    public void verifyMade(Verify verify) {
        BattlebotVerifyAgent.logger.info("Received: " + verify);
    }
}
