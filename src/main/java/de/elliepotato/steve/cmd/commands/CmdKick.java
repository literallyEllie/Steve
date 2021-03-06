package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.util.UtilEmbed;
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
                "<target>", "[reason]");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final Member sender = environment.getSender();
        final String[] args = environment.getArgs();

        getBot().parseMember(channel.getGuild(), args[0], toKick -> {
            if (toKick == null) {
                environment.replyBadSyntax(sender.getAsMention() + ", I couldn't find user `" + args[0] + "`.");
                return;
            }

            if (!PermissionUtil.canInteract(sender, toKick)) {
                getBot().messageChannel(channel, "You cannot kick that person!");
                return;
            }

            if (!PermissionUtil.canInteract(channel.getGuild().getSelfMember(), toKick)) {
                environment.replyBadSyntax("I cannot kick that person!");
                return;
            }

            String reason = null;
            if (args.length > 1) {
                reason = UtilString.getFinalArg(args, 1);
            }

            String signature = sender.getEffectiveName() + " (" + sender.getId() + ")";

            channel.getGuild().kick(toKick, reason != null ?
                    "Issued by " + signature + " :: " + reason : "No reason specified from " + signature).queue();

            getBot().modLog(channel.getGuild(), UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.KICK)
                    .setTitle("Kicked " + toKick.getUser().getName() + "#" + toKick.getUser().getDiscriminator() + " (" + toKick.getId() + ")")
                    .addField("Kicker", signature, true)
                    .addField("Reason", (reason != null ? reason : "No reason specified."), false));

            environment.replySuccess("Kicked " + toKick.getUser().getName() + "#" + toKick.getUser().getDiscriminator() + " out this world. :eyes:"
                    + (reason != null ? " (`" + reason + "`)" : ""));

        });
    }

}