package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 10/02/2018
 */
public class CmdClear extends Command {

    public CmdClear(Steve steve) {
        super(steve, "clear", "Clear messages", Lists.newArrayList(), Permission.MESSAGE_MANAGE,
                Lists.newArrayList("<amount>"));
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final Member member = environment.getSender();
        final String[] args = environment.getArgs();

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
            if (amount < 1 || amount > 50) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            getBot().tempMessage(channel, ":x: " + member.getAsMention() + ", please specify a number between 1 and 50.", 10, null);
            return;
        }

        channel.getHistory().retrievePast(amount + 1).queue(messages -> {
            try {
                channel.deleteMessages(messages).queue(success -> getBot().tempMessage(channel,
                        ":thumbsup: Cleared " + amount + " messages.", 10, null));
            } catch (IllegalArgumentException e) {
                getBot().tempMessage(channel, ":x: Failed to delete some messages as they are too old to touch", 7, null);
            }
        });

    }

}
