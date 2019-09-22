package de.elliepotato.steve.booster;

/**
 * @author Ellie :: 03/09/2019
 */
public class GuildBooster {

    private final long boosterId;
    private final String boosterUsername; // So if they leave, it can be tracked to an extent.
    private final long boosterGuild;
    private final long started; // will have to be when discovered.
    private boolean justDiscovered;

    public GuildBooster(long boosterId, String boosterUsername, long boosterGuild, long started) {
        this.boosterId = boosterId;
        this.boosterUsername = boosterUsername;
        this.boosterGuild = boosterGuild;
        this.started = started;
    }

    public long getBoosterId() {
        return boosterId;
    }

    public String getBoosterUsername() {
        return boosterUsername;
    }

    public long getBoosterGuild() {
        return boosterGuild;
    }

    public long getStarted() {
        return started;
    }

    public boolean isJustDiscovered() {
        return justDiscovered;
    }

    public void setJustDiscovered(boolean justDiscovered) {
        this.justDiscovered = justDiscovered;
    }

}
