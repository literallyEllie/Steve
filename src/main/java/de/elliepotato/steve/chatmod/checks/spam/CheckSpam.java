package de.elliepotato.steve.chatmod.checks.spam;

import com.google.common.collect.Maps;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.api.Permission;
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
        return this.guildSpamViolationHandleMap.getOrDefault(guild, new GuildSpamViolationHandle()).isAlerted();
    }

    public boolean manualReset(long guild) {
        final GuildSpamViolationHandle spamViolationHandle = this.guildSpamViolationHandleMap.get(guild);
        if (spamViolationHandle == null)
            return false;

        spamViolationHandle.reset();
        return true;
    }

    @Override
    public boolean check(Message message) {
        final Guild guild = message.getGuild();

        if (message.getCategory().getIdLong() == Constants.CAT_BISECT_FUN.getIdLong())
            return true;

        GuildSpamViolationHandle handle = guildSpamViolationHandleMap.get(guild.getIdLong());
        if (handle == null)
            handle = putViolationHandle(guild.getIdLong());

        handle.onMessage();

        if (handle.isAlerted()) {
            steve.getLogger().info("[SPAM-ALERT] (" + message.getChannel().getId() + ") DEL (" + message.getIdLong() + ") " + message.getContentRaw());
            message.delete().queue();
            return false;
        }

        return true;
    }

    private GuildSpamViolationHandle putViolationHandle(long guildId) {
        GuildSpamViolationHandle spamViolationHandle = new GuildSpamViolationHandle();

        spamViolationHandle.setViolationTriggerListener((alerted, violationLevel) -> {
            boolean bisect = guildId == Constants.GUILD_BISECT.getIdLong();

            final Guild guildById = steve.getJda().getGuildById(guildId);

            if (alerted) {
                steve.getLogger().info("[SPAM-ALERT] " + guildId + " likely under attack (VL " + violationLevel + ")");
                steve.messageChannel(Constants.CHAT_BISECT_STAFF.getIdLong(), "It is likely the **" +
                        (bisect ? "Bisect" : "Melon") + " Discord** is under attack.\n" +
                        "Messages from < level 1 members will **no longer** go through in this server until at least **1 minute** has passed since the last message " +
                        "from a regular member. To disable/if this is a false flag, do `!steve ok "
                        + (bisect ? "" : "m") + "`");

                guildById.getPublicRole().getManager()
                        .revokePermissions(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE);
            } else {
                steve.getLogger().info("[SPAM-ALERT] " + guildId + " restrictions have been turned off.");
                steve.messageChannel(Constants.CHAT_BISECT_STAFF.getIdLong(), "The **" +
                        (bisect ? "Bisect" : "Melon") + " Discord** is no longer detected to be " +
                        "under attack and restrictions have been disabled.");

                guildById.getPublicRole().getManager()
                        .givePermissions(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE);
            }

        });

        this.guildSpamViolationHandleMap.put(guildId, spamViolationHandle);
        return spamViolationHandle;
    }

}
