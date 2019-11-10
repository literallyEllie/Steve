package de.elliepotato.steve.chatmod.checks.spam;

import com.google.common.collect.Maps;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
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

    public void manualReset(long guild) {
        final GuildSpamViolationHandle spamViolationHandle = this.guildSpamViolationHandleMap.get(guild);
        if (spamViolationHandle == null)
            return;

        spamViolationHandle.reset();
    }

    @Override
    public boolean check(Message message) {
        final Guild guild = message.getGuild();

        GuildSpamViolationHandle handle = guildSpamViolationHandleMap.get(guild.getIdLong());
        if (handle == null)
            handle = putViolationHandle(guild.getIdLong());

        handle.onMessage();

        if (handle.isAlerted()) {
            steve.getLogger().info("[SPAM-ALERT] (" + guild.getIdLong() + ") DEL (" + message.getIdLong() + ") " + message.getContentRaw());
            message.delete().queue();
            return false;
        }

        return true;
    }

    private GuildSpamViolationHandle putViolationHandle(long guildId) {
        GuildSpamViolationHandle spamViolationHandle = new GuildSpamViolationHandle();

        spamViolationHandle.setViolationTriggerListener((alerted, violationLevel) -> {

            if (alerted) {
                /*
                steve.getJda().getGuildById(guildId).getCategories().stream()
                        .filter(guildChannel -> guildChannel.getIdLong() != Constants.CAT_MELON_STAFF.getIdLong()
                                && guildChannel.getIdLong() != Constants.CAT_BISECT_STAFF.getIdLong())
                        .forEach(guildChannel -> {

                            for (GuildChannel textChannel : guildChannel.getChannels()) {
                                if (textChannel.getType() != ChannelType.TEXT) continue;
                                textChannel.getManager().setSlowmode(300).queue();
                            }

                        });
                 */
                steve.getLogger().info("[SPAM-ALERT] " + guildId + " likely under attack (VL " + violationLevel + ")");
                steve.messageChannel(Constants.CHAT_BISECT_STAFF.getIdLong(), "It is likely the **" +
                        (guildId == Constants.GUILD_BISECT.getIdLong() ? "Bisect" : "Melon") + " Discord** is under attack.\n" +
                        "Messages from regular members will **no longer** go through in this server until at least **30 seconds** has passed since the last message " +
                        "from a regular member. To disable/if this is a false flag, do `!steve ok "
                        + (guildId == Constants.GUILD_BISECT.getIdLong() ? "" : "m") + "`");
            } else {
                steve.getLogger().info("[SPAM-ALERT] " + guildId + " restrictions have been turned off.");
                steve.messageChannel(Constants.CHAT_BISECT_STAFF.getIdLong(), "The **" +
                        (guildId == Constants.GUILD_BISECT.getIdLong() ? "Bisect" : "Melon") + " Discord** is no longer detected to be " +
                        "under attack and restrictions have been disabled.");
            }

        });

        this.guildSpamViolationHandleMap.put(guildId, spamViolationHandle);
        return spamViolationHandle;
    }

}
