package de.elliepotato.steve.chatmod.checks.spam;

import com.google.common.collect.Maps;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.DebugWriter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;

import java.util.HashMap;

/**
 * @author Ellie for VentureNode LLC
 * at 19/10/2019
 */
public class CheckSpam implements MessageCheck {

    private final Steve steve;
    private HashMap<Long, GuildSpamViolationHandle> guildSpamViolationHandleMap;

    public CheckSpam(Steve steve) {
        this.steve = steve;
        this.guildSpamViolationHandleMap = Maps.newHashMap();
    }

    public boolean isAlerted(long guild) {
        return this.guildSpamViolationHandleMap.getOrDefault(guild, new GuildSpamViolationHandle(guild)).isAlerted();
    }

    @Override
    public boolean check(Message message) {
        final Guild guild = message.getGuild();

        GuildSpamViolationHandle handle = guildSpamViolationHandleMap.get(guild.getIdLong());
        if (handle == null)
            handle = putViolationHandle(guild.getIdLong());

        handle.onMessage();

        if (handle.isAlerted()) {
            System.out.println(guild.getName() + " VL = " + handle.getViolationLevel());
            // message.delete().queue();
            return true;
        }

        return true;
    }

    private GuildSpamViolationHandle putViolationHandle(long guildId) {
        GuildSpamViolationHandle spamViolationHandle = new GuildSpamViolationHandle(guildId);

        spamViolationHandle.setViolationTriggerListener(level -> {
            steve.messageChannel(Constants.CHAT_BISECT_STAFF.getIdLong(), "[!!IGNORE ME!!] It is likely the " +
                    (guildId == Constants.GUILD_BISECT.getIdLong() ? "Bisect" : "Melon" )+ " Discord is under attack.\n " +
                    "Messages from regular messages will no longer go through in this channel until at least 30 seconds has passed since the last message " +
                    "from a regular user. To disable/if is false-flag then eat a cookie.");
        });

        this.guildSpamViolationHandleMap.put(guildId, spamViolationHandle);
        return spamViolationHandle;
    }

}
