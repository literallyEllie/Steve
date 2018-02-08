package de.elliepotato.steve.cmd.commands;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 08/02/2018
 */
public class CmdSteve extends Command {

    private MessageEmbed embedBuilder;

    public CmdSteve(Steve steve) {
        super(steve, "steve", "Steve command", Lists.newArrayList(), Permission.MESSAGE_WRITE, Lists.newArrayList(""));

        this.embedBuilder = steve.getEmbedBuilder(Steve.DiscordColor.NEUTRAL)
                .setTitle("Steve v" + Steve.VERSION)
                .setDescription("Hi !I am Steve-0, a chat moderation bot who does various other things as well." +
                        "If you are a user you will probably not need to worry about me, if I have mis-moderated please tell a Discord moderator!")
                .addField("", "For BisectHosting and MelonCube", false)
                .addField("Source (contribute :D)", "https://github.com/literallyEllie/Steve", false)
                .addField("Author", "Ellie#0006", false)
                .build();
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        getBot().messageChannel(environment.getChannel(), embedBuilder);
    }

}
