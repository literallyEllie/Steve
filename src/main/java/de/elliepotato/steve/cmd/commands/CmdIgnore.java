package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.file.IgnoreFile;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

/**
 * @author Ellie for VentureNode LLC
 * at 30/04/2020
 */
public class CmdIgnore extends Command {

    private final IgnoreFile ignoreFile;

    /**
     * Command for the bot to ignore someone.
     */
    public CmdIgnore(Steve steve) {
        super(steve, "ignore", "Ignore a user making them exempt from chat checks", Lists.newArrayList(), Permission.KICK_MEMBERS, "<person>");

        this.ignoreFile = new IgnoreFile(steve);

        try {
            steve.getMessageChecker().setIgnoredUsers(ignoreFile.read());
        } catch (IOException e) {
            steve.getLogger().error("failed to read ignored users", e);
        }
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {

        getBot().parseMember(environment.getChannel().getGuild(), environment.getArgs()[0], member -> {

            if (member == null) {
                getBot().tempMessage(environment.getChannel(), ":x: Could not find a user from that query.", 10, environment.getMessage());
                return;
            }

            final User user = member.getUser();

            final Set<Long> ignoredUsers = getBot().getMessageChecker().getIgnoredUsers();
            if (ignoredUsers.contains(user.getIdLong())) {
                ignoredUsers.remove(user.getIdLong());
            } else {
                ignoredUsers.add(user.getIdLong());
            }

            try {
                ignoreFile.write(ignoredUsers);

                getBot().messageChannel(environment.getChannel(), user.getName() + "#" + user.getDiscriminator() + " is now "
                        + (ignoredUsers.contains(user.getIdLong()) ? " effectively immune from message check" : "no longer immune from message checks"));
            } catch (IOException e) {
                getBot().getLogger().error("failed to write ignored users", e);
                getBot().messageChannel(environment.getChannel(), ":x: Failed to save new data due to I/O error but is effective temporarily.");
            }

        });

    }

}
