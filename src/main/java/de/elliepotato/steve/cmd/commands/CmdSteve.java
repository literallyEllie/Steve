package de.elliepotato.steve.cmd.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.config.FileHandler;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 08/02/2018
 */
public class CmdSteve extends Command  {

    private MessageEmbed embedBuilder, helpStaff;

    public CmdSteve(Steve steve) {
        super(steve, "steve", "Steve command", Lists.newArrayList(), Permission.MESSAGE_WRITE, Lists.newArrayList(""));

        this.embedBuilder = steve.getEmbedBuilder(Steve.DiscordColor.NEUTRAL)
                .setTitle("Steve v" + Steve.VERSION)
                .setDescription("Hi !I am Steve-0, a chat moderation bot who does various other things as well." +
                        "If you are a user you will probably not need to worry about me, if I have mis-moderated please tell a Discord moderator!")
                .addField("For BisectHosting and MelonCube.", "", false)
                .addField("Source (contribute :D)", "https://github.com/literallyEllie/Steve", false)
                .addField("Author", Joiner.on(", ").join(Steve.AUTHORS), false)
                .build();
        this.helpStaff = steve.getEmbedBuilder(Steve.DiscordColor.NEUTRAL)
                .setTitle("Steve Admin help")
                .addField("help", "This menu you're viewing", false)
                .addField("chat", "Steve-0's chat moderation settings", false)
                .addField("commands", "Command list", false)
                .build();
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final String[] args = environment.getArgs();
        final Member member = environment.getSender();

        if (args.length > 0 && member.hasPermission(Permission.MESSAGE_MANAGE)) {

            if (args[0].equalsIgnoreCase("chat")) {

                if (args.length > 1) {

                    if (args[1].equalsIgnoreCase("domains")) {

                        if (args.length > 2) {
                            switch (args[2]) {
                                case "add":

                                    break;
                                case "remove":

                                    break;
                                case "list":

                                    break;
                                default:
                                    getBot().messageChannel(environment.getChannel(), "");

                            }
                        }

                    }

                }

            } else if (args[1].equalsIgnoreCase("commands")) {
                EmbedBuilder commandList = getBot().getEmbedBuilder(Steve.DiscordColor.NEUTRAL);
                getBot().getCommandManager().getCommandMap().values().forEach(command -> commandList.addField(command.getLabel(), command.getDescription(), true));
                getBot().messageChannel(environment.getChannel(), commandList.build());
            } else {
                getBot().messageChannel(environment.getChannel(), helpStaff);
            }

            return;

        }

        getBot().messageChannel(environment.getChannel(), embedBuilder);
    }

}
