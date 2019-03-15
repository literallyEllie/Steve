package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.UtilString;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 10/02/2018
 */
public class CmdKick extends Command {

    /**
     * A command to quickly kick a user from the server.
     *
     * @param steve the bot instance.
     */
    public CmdKick(Steve steve) {
        super(steve, "kick", "Kick a user off the face of the Earth", Lists.newArrayList(), Permission.KICK_MEMBERS,
                Lists.newArrayList("<target> [reason]"));
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final Member sender = environment.getSender();
        final String[] args = environment.getArgs();

        final User toKick = getBot().parseUser(args[0]);
        if (toKick == null) {
            getBot().messageChannel(channel, ":x: " + sender.getAsMention() + ", I couldn't find user `" + args[0] + "`.");
            return;
        }

        if (!PermissionUtil.canInteract(channel.getGuild().getMember(getBot().getJda().getUserById(Constants.PRESUMED_SELF.getIdLong())), sender))
            return;

        String reason = null;
        if (args.length > 1) {
            reason = UtilString.getFinalArg(args, 1);
        }

        getBot().modLog(channel.getGuild(), getBot().getEmbedBuilder(Steve.DiscordColor.KICK)
                .setTitle("Kicked " + toKick.getName() + "#" + toKick.getDiscriminator() + " (" + toKick.getId() + ")")
                .addField("Kicker", (sender.getUser().getName() + "#" + sender.getUser().getDiscriminator()), true)
                .addField("Reason", (reason != null ? reason : "No reason specified."), false));

        getBot().tempMessage(channel, ":ok_hand: Kicked " + toKick.getName() + "#" + toKick.getDiscriminator() + " out this world. :eyes:"
                + (reason != null ? " (`" + reason + "`)" : ""), 10, environment.getMessage());

        if (reason != null) {
            channel.getGuild().getController().kick(channel.getGuild().getMember(toKick), reason).queue();
        } else channel.getGuild().getController().kick(channel.getGuild().getMember(toKick)).queue();
    }

}