package de.elliepotato.steve.cmd.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.util.UtilEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 08/02/2018
 */
public class CmdSteve extends Command {

    private MessageEmbed embedBuilder, helpStaff;

    /**
     * Main bio of the bot and small help list for navigating the bot.
     *
     * @param steve the  bot instance.
     */
    public CmdSteve(Steve steve) {
        super(steve, "steve", "Steve command", Lists.newArrayList(), Permission.MESSAGE_WRITE);

        this.embedBuilder = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                .setTitle("Steve v" + Steve.VERSION)
                .setDescription("Hi !I am Steve-0, a chat moderation bot who does various other things as well. " +
                        "If you are a user you will probably not need to worry about me, if I have mis-moderated please tell a Discord moderator!")
                .addField("For BisectHosting and MelonCube.", "", false)
                .addField("Source (contribute :D)", "https://github.com/literallyEllie/Steve", false)
                .addField("Author", Joiner.on(", ").join(Steve.AUTHORS), false)
                .build();
        this.helpStaff = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                .setTitle("Steve Admin help")
                .addField("help", "This menu you're viewing", false)
                .addField("chat", "Steve-0's chat moderation settings", false)
                .addField("ban", "Ban user", false)
                .addField("kick", "Kick user", false)
                .addField("domains", "Whitelist a domain", false)
                .addField("cc", "Custom Commands", false)
                .addField("nick", "Force nickname a server [UNFINISHED]", false)
                .addField("remadvertcooldown", "Manually remove an advert cooldown from a user", false)
                .addField("shutdown", "Shutdown the bot", false)
                .addField("commands", "Command list", false)
                .build();
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final String[] args = environment.getArgs();
        final Member member = environment.getSender();

        if (args.length > 0 && member.hasPermission(Permission.MESSAGE_MANAGE)) {

            if (args[0].equalsIgnoreCase("commands")) {
                EmbedBuilder commandList = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL);
                getBot().getCommandManager().getCommandMap().values().forEach(command -> commandList.addField(command.getLabel(), command.getDescription(), false));
                getBot().messageChannel(environment.getChannel(), commandList.build());
            } else {
                getBot().messageChannel(environment.getChannel(), helpStaff);
            }

            return;
        }

        getBot().messageChannel(environment.getChannel(), embedBuilder);
    }

}
