package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.UtilEmbed;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie :: 13/08/2019
 */
public class CmdSlowMode extends Command {

    public CmdSlowMode(Steve steve) {
        super(steve, "slowmode", "Set slow-mode for the server and slow down bot responses", Lists.newArrayList("sm", "vegetate"), Permission.KICK_MEMBERS, "[delay]");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final String[] args = environment.getArgs();

        int newMode = 0;

        if (args.length > 0) {

            try {
                newMode = Integer.parseInt(args[0]);
                if (newMode < 0 || newMode > 21600) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                environment.replyBadSyntax(":x: " + environment.getSender().getAsMention() + ", please specify a number between 1 and 21600.");
                return;
            }

        }

        getBot().getCommandManager().getGuildRestriction(channel.getGuild().getIdLong()).setEnabled(newMode != 0);

        int finalNewMode = newMode;
        channel.getGuild().getCategories().stream()
                .filter(guildChannel -> guildChannel.getIdLong() != Constants.CAT_MELON_STAFF.getIdLong()
                        && guildChannel.getIdLong() != Constants.CAT_BISECT_STAFF.getIdLong())
                .forEach(guildChannel -> {

                    for (GuildChannel textChannel : guildChannel.getChannels()) {
                        if (textChannel.getType() != ChannelType.TEXT) continue;
                        textChannel.getManager().setSlowmode(finalNewMode).queue();
                    }

                });

        environment.replySuccess((newMode != 0 ? "Enabled (" + newMode + "s delay)" : "Disabled") + " slow-mode for the server.");
        getBot().modLog(channel.getGuild(), UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                .setTitle((newMode != 0 ? "Enabled" : "Disabled") + " slow-mode for the server.")
                .addField("Delay:", newMode + "s", false)
                .addField("Sent by:", environment.getSender().getEffectiveName() + "#" + environment.getSender().getUser().getDiscriminator(), false));
    }

}
