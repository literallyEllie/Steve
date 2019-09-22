package de.elliepotato.steve.chatmod;

import com.google.common.collect.Maps;
import de.elliepotato.steve.cmd.commands.CmdSlowMode;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.util.UtilTime;

import java.util.Map;

/**
 * @author Ellie :: 12/09/2019
 */
public class GuildRestriction {

    private static final int DEFAULT_COMMAND_CD = 15 * 1000;

    private boolean enabled;
    private long commandCooldown;

    // Command cool downs, Int is command hashcode, Long is when sent.
    private Map<Integer, Long> commandCooldowns;

    public GuildRestriction() {
        this.commandCooldowns = Maps.newHashMap();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getCommandCooldown() {
        return commandCooldown;
    }

    public void setCommandCooldown(long commandCooldown) {
        this.commandCooldown = commandCooldown;
    }

    public Map<Integer, Long> getCommandCooldowns() {
        return commandCooldowns;
    }

    public boolean attemptRunCommand(int hashCode) {
        if (!enabled) return true;
        if (UtilTime.elapsed(commandCooldowns.getOrDefault(hashCode, 0L), DEFAULT_COMMAND_CD)) {
            commandCooldowns.put(hashCode, UtilTime.now());
            return true;
        }

        return false;
    }

}
