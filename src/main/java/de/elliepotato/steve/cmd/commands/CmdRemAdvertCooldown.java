package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Ellie :: 18/07/2018
 */
public class CmdRemAdvertCooldown extends Command {

    public CmdRemAdvertCooldown(Steve steve) {
        super(steve, "remadvertcooldown", "Remove an advert cooldown of a person", Lists.newArrayList(), Permission.MESSAGE_MANAGE,
                Lists.newArrayList("<person>"));
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final Member sender = environment.getSender();
        final String[] args = environment.getArgs();

        final User toReset = getBot().parseUser(args[0]);
        if (toReset == null) {
            getBot().messageChannel(channel, ":x: " + sender.getAsMention() + ", I couldn't find user `" + args[0] + "`.");
            return;
        }

        final Map<Long, Long> advertCooldown = getBot().getMessageChecker().getAdvertCooldown();
        if (!advertCooldown.containsKey(toReset.getIdLong())) {
            getBot().messageChannel(channel, ":x: That user had no cooldown to reset.");
            return;
        }

        advertCooldown.remove(toReset.getIdLong());
        getBot().messageChannel(channel, ":white_check_mark: Reset.");
    }

}
