package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageChecker;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author Ellie for VentureNode LLC
 * at 16/03/2018
 */
public class CmdDomains extends Command {

    public CmdDomains(Steve steve) {
        super(steve, "domains", "Domain whitelist management", Lists.newArrayList(), Permission.MESSAGE_MANAGE,
                Lists.newArrayList("<add> <domain.com>"));
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final TextChannel channel = environment.getChannel();
        final String[] args = environment.getArgs();

        switch (args[0].toLowerCase()) {
            case "add":
                MessageChecker messageChecker = getBot().getMessageChecker();

                String stripped = args[1].toLowerCase().replaceAll("http?s/{2}", "").replace("/", "").trim();

                if (messageChecker.getAllowedDomains().contains(stripped)) {
                    getBot().tempMessage(channel, "This domain is already whitelisted.", 7, null);
                    return;
                }

                messageChecker.getAllowedDomains().add(stripped);
                try {
                    messageChecker.getDomainsFile().write(getBot().getMessageChecker().getAllowedDomains());
                    getBot().tempMessage(channel, "Whitelisted the domain `" + stripped + "`.", 7, environment.getMessage());
                } catch (IOException e) {
                    getBot().messageChannel(channel, ":x: Failed to write domains to file!");
                    e.printStackTrace();
                }
                break;
            default:
                correctUsage("");
        }

    }


}
