package de.elliepotato.steve.cmd.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.cmd.model.CustomCommand;
import de.elliepotato.steve.util.UtilString;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Ellie for VentureNode LLC
 * at 05/02/2018
 */
public class CmdCustomCommand extends Command {

    public CmdCustomCommand(Steve steve) {
        super(steve, "customcommands", "Custom command manager", Lists.newArrayList("cc"),
                Permission.KICK_MEMBERS, Lists.newArrayList("<create | delete | set | list>"));
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final Member sender = environment.getSender();
        final String[] args = environment.getArgs();
        final TextChannel channel = environment.getChannel();

        switch (args[0].toLowerCase()) {
            case "create":
                // cc create <name> <response>
                if (args.length < getMinArgs() + 2) {
                    getBot().messageChannel(channel, correctUsage("<name> --<description> --<response>"));
                    return;
                }

                String label = args[1].toLowerCase();

                Map<String, CustomCommand> customCommandMap = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong());
                if (customCommandMap != null && customCommandMap.containsKey(label)) {
                    getBot().tempMessage(channel, sender.getAsMention() + ", the custom command by `" + label + "` already exist on this server.", 7, null);
                    return;
                }

                final String[] parts = UtilString.getFinalArg(args, 2).split("--");

                for (String part : parts) {
                    System.out.println("`" + part + "`");
                }

                String description;
                String response;
                if (parts.length > 2) {
                    description = parts[1].trim();
                    response = parts[2].trim();    // Lazy ezpz :D
                } else {
                    getBot().messageChannel(channel, correctUsage("<name> --<description> --<response>"));
                    return;
                }

                final CustomCommand customCommand = new CustomCommand(getBot(), label, description, response);
                getBot().getCustomCommandManager().addCustomCommand(channel.getGuild().getIdLong(), customCommand, true);

                getBot().messageChannel(environment.getChannel(), getBot().getEmbedBuilder(Steve.DiscordColor.NEUTRAL)
                        .setTitle("Custom command created!")
                        .addField("Label", label, true).addField("Description", customCommand.getDescription(), true)
                        .addField("Response", customCommand.getResponseMessage(), true).build());
                // woop

                break;
            case "delete":
                // "cc delete <name>"
                if (args.length < getMinArgs() + 1) {
                    getBot().messageChannel(channel, correctUsage("<name>"));
                    return;
                }
                label = args[1].toLowerCase();

                final Map<String, CustomCommand> guildCommands = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong());
                if (!guildCommands.containsKey(label)) {
                    getBot().tempMessage(channel, sender.getAsMention() + ", the custom command by `" + label + "` doesn't exist for this server.", 7, null);
                    return;
                }

                getBot().getCustomCommandManager().deleteCustomCommand(channel.getGuild().getIdLong(), label);
                getBot().messageChannel(channel, "Custom command `" + label + "` deleted!");

                break;
            case "set":
                // "cc set <name> <type> <value>"
                if (args.length < getMinArgs() + 3) {
                    getBot().messageChannel(channel, correctUsage("<name> <" + Joiner.on(" | ").join(CustomCommandValue.values()) + "> <value>"));
                    return;
                }

                label = args[1].toLowerCase();

                CustomCommand command = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong()).get(label);
                if (command == null) {
                    getBot().tempMessage(channel, sender.getAsMention() + ", the custom command by `" + label + "` doesn't exist for this server.", 7, null);
                    return;
                }

                final CustomCommandValue customCommandValue = CustomCommandValue.fromString(args[2]);
                if (customCommandValue == null) {
                    getBot().tempMessage(channel, sender.getAsMention() + ", invalid custom command type `" + args[2] + "`.", 7, null);
                    return;
                }

                final String data = UtilString.getFinalArg(args, 3);
                setValue(channel.getGuild().getIdLong(), command, customCommandValue, data);

                getBot().messageChannel(environment.getChannel(), getBot().getEmbedBuilder(Steve.DiscordColor.NEUTRAL)
                        .setTitle("Custom command updated!")
                        .addField("Label", label, true).addField("Description", command.getDescription(), true)
                        .addField("Response", command.getResponseMessage(), true).build());

                break;
            case "list":
                final Map<String, CustomCommand> guildCommandMap = getBot().getCustomCommandManager().getCustomCommandsOf(channel.getGuild().getIdLong());

                EmbedBuilder embedBuilder = getBot().getEmbedBuilder(Steve.DiscordColor.NEUTRAL)
                        .setTitle("Guild commands (" + (guildCommandMap != null ? guildCommandMap.size() : 0)  + ")");

                // meh @ pagination
                if (guildCommandMap != null) {
                    guildCommandMap.values().forEach(customCommand1 -> embedBuilder.addField(customCommand1.getLabel(), customCommand1.getDescription(), false));
                }

                getBot().messageChannel(channel.getIdLong(), embedBuilder.build());
                break;
                default:
                    getBot().messageChannel(channel, correctUsage(""));
        }

    }

    private void setValue(long guildId, CustomCommand customCommand, CustomCommandValue type, String value) {

        switch (type) {
            case DESCRIPTION:
                customCommand.setDescription(value);
                break;
            case RESPONSE:
                customCommand.setResponseMessage(value);
                break;
            default:
                return; // ???
        }

        getBot().getCustomCommandManager().addCustomCommand(guildId, customCommand, true);
    }

    enum CustomCommandValue {

        DESCRIPTION,
        RESPONSE

        ;

        public static CustomCommandValue fromString(String input) {
            for (CustomCommandValue customCommandValue : values()) {
                if (customCommandValue.name().equalsIgnoreCase(input))
                    return customCommandValue;
            }
            return null;
        }

    }

}
