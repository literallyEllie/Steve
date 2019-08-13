package de.elliepotato.steve.react;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 05/10/2018
 */
public class CmdReactSetup extends Command {

    public CmdReactSetup(Steve steve) {
        super(steve, "reactsetup", "Sets up the reaction system for the guild", Lists.newArrayList(), Permission.KICK_MEMBERS,
                "<channel>", "<message id>", "<emoji>");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final String[] args = environment.getArgs();
        ReactManager reactManager = getBot().getReactManager();

        long channel = -1, messageId = -1;
        try {
            channel = Long.parseLong(args[0]);
            messageId = Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            getBot().messageChannel(environment.getChannel(), ":x: Invalid ID on the first -1.  (channel: " + channel + ", message: " + messageId + ")");
            return;
        }

        getBot().messageChannel(environment.getChannel(), reactManager.setupReact(environment.getChannel().getGuild().getIdLong(), channel, messageId, args[2]));
    }

}
