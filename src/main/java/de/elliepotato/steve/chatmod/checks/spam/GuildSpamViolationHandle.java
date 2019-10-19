package de.elliepotato.steve.chatmod.checks.spam;

import java.util.concurrent.TimeUnit;

/**
 * @author Ellie for VentureNode LLC
 * at 19/10/2019
 */
public class GuildSpamViolationHandle {

    private static final int TRIGGER_VIOLATIONS = 10; // a second.

    private final long guild;

    private int violationLevel;
    private long lastViolation;

    private long lastActivity;

    private boolean alerted;

    private ViolationTriggerListener violationTriggerListener;

    public GuildSpamViolationHandle(long guild) {
        this.guild = guild;
    }

    public void onMessage() {
        if (this.lastActivity == 0L) {
            this.lastActivity = System.currentTimeMillis();
            return;
        }

        long sinceLastDuration = System.currentTimeMillis() - this.lastViolation;
        System.out.println("last violation " + sinceLastDuration);
        // if last violation was less than 2 seconds ago
        if (this.lastViolation != 0L && sinceLastDuration <= TimeUnit.SECONDS.toMillis(2)) {
            System.out.println("viol+");
            violationLevel++;
            // If last last vision was more than 30 seconds aog.
        } else if (sinceLastDuration > TimeUnit.SECONDS.toMillis(30)) {
            System.out.println("all good " + guild);
            this.violationLevel = 0;
            this.alerted = false;
        }

        // if last activity was less than 2 seconds ago.
        if (System.currentTimeMillis() - this.lastActivity <= TimeUnit.SECONDS.toMillis(2)) {
            System.out.println("viol");
            this.lastViolation = System.currentTimeMillis();
        }

        this.lastActivity = System.currentTimeMillis();
        System.out.println("---------");

        if (!alerted && this.violationLevel >= TRIGGER_VIOLATIONS) {
            this.alerted = true;
            this.violationTriggerListener.onViolateTrigger(this.violationLevel);
        }

    }

    public boolean isAlerted() {
        return alerted;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public int getViolationLevel() {
        return violationLevel;
    }

    public long getLastViolation() {
        return lastViolation;
    }

    public void setViolationTriggerListener(ViolationTriggerListener violationTriggerListener) {
        this.violationTriggerListener = violationTriggerListener;
    }

    interface ViolationTriggerListener {
        void onViolateTrigger(long level);
    }



}
