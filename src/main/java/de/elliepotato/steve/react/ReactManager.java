package de.elliepotato.steve.react;

import com.google.common.collect.Maps;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.module.DataHolder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;

/**
 * @author Ellie for VentureNode LLC
 * at 05/10/2018
 */
public class ReactManager extends ListenerAdapter implements DataHolder {

    private final Steve bot;
    private final Map<Long, ReactData> reactDataMap;

    public ReactManager(Steve steve) {
        this.bot = steve;
        this.reactDataMap = Maps.newHashMap();

        CmdReactSetup reactSetup = new CmdReactSetup(steve);
        bot.getCommandManager().getCommandMap().put(reactSetup.getLabel().toLowerCase(), reactSetup);
    }

    @Override
    public void shutdown() {
        reactDataMap.clear();
    }

    public Map<Long, ReactData> getReactDataMap() {
        return reactDataMap;
    }

    public String setupReact(long guildId, long channel, long messageId, String emote) {
        final TextChannel textChannel = bot.getJda().getTextChannelById(channel);
        if (textChannel == null) {
            return ":x: Invalid channel ID.";
        }

        Message message;
        try {
            message = textChannel.retrieveMessageById(messageId).complete();
        } catch (Exception e) {
            return ":x: Invalid message ID.";
        }

        message.addReaction(emote).queue();

        reactDataMap.put(guildId, new ReactData(channel, messageId, emote));
        return ":thumbsup: Done!";
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        final Guild guild = event.getGuild();
        ReactData reactData = reactDataMap.get(guild.getIdLong());
        if (reactData == null) return;

        final Member member = event.getMember();

        for (TextChannel textChannel : guild.getTextChannels()) {
            if (textChannel.getIdLong() == reactData.getChannelId()) {
                textChannel.createPermissionOverride(member).complete().getManager().grant(Permission.MESSAGE_ADD_REACTION).queue();
                continue;
            }
            final PermissionOverride permissionOverride = textChannel.createPermissionOverride(member).complete();
            permissionOverride.getManager().deny(Permission.MESSAGE_READ).queue();
        }

    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        final Guild guild = event.getGuild();
        final Member member = event.getMember();

        ReactData reactData = reactDataMap.get(guild.getIdLong());
        if (reactData == null) return;

        if (reactData.getChannelId() == event.getChannel().getIdLong() && reactData.getMessageId() == event.getMessageIdLong()) {
            for (TextChannel textChannel : guild.getTextChannels()) {
                if (textChannel.getIdLong() == reactData.getChannelId()) continue;
                textChannel.getPermissionOverride(member).getManager().reset().queue();
            }
        }
    }

}
