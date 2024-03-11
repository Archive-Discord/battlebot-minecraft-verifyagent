package kr.battlebot.battlebotverifyagent.model;

import org.bukkit.event.*;
public class VerifyEvent extends Event {
    /**
     * Event listener handler list.
     */
    private static final HandlerList handlers = new HandlerList();
    private Verify verify;
    public VerifyEvent(final Verify verify) {
        this.verify = verify;
    }

    public Verify getVerify() {
        return verify;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
