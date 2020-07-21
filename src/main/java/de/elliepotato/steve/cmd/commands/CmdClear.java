package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.util.UtilEmbed;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 10/02/2018
 */
public class CmdClear extends Command {

    /**
     * A command to quickly clear x amount of messages. Useful for spam.
     *
     * @param steve Bot instance.
     */
    public CmdClear(Steve steve) {
        super(steve, "clear", "Clear messages", Lists.newArrayList(), Permission.MESSAGE_MANAGE, "<amount>");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final Member member = environment.getSender();
        final String[] args = environment.getArgs();

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
            if (amount < 1 || amount > 50)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            environment.replyBadSyntax(member.getAsMention() + ", please specify a number between 1 and 50.");
            return;
        }

        channel.getHistory().retrievePast(amount + 1).queue(messages -> {
            if (messages != null) {
                try {
                    channel.deleteMessages(messages).queue(success ->
                            environment.replyTemp(":thumbsup: Cleared " + amount + " messages.", 10, null));
                } catch (IllegalArgumentException e) {
                    environment.replyBadSyntax("Failed to delete some messages as they are too old to touch");
                }
            }
        });

        getBot().modLog(channel.getGuild(), UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                .setTitle("Cleared " + amount + " messages in #" + channel.getName())
                .addField("Issued by:", member.getEffectiveName() + "#" + member.getUser().getDiscriminator(), true));

    }

}
