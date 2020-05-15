package de.elliepotato.steve.cmd.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.chatmod.checks.spam.CheckSpam;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.UtilEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import org.jetbrains.annotations.NotNull;

/**
 * @author Ellie for VentureNode LLC
 * at 08/02/2018
 */
public class CmdSteve extends Command {

    private final MessageEmbed botInfoEmbed, helpStaffEmbed;

    /**
     * Main bio of the bot and small help list for navigating the bot.
     *
     * @param steve the  bot instance.
     */
    public CmdSteve(Steve steve) {
        super(steve, "steve", "Steve command", Lists.newArrayList(), Permission.MESSAGE_WRITE);

        this.botInfoEmbed = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                .setTitle("Steve v" + Steve.VERSION)
                .setDescription("Hi! I am Steve-0, a chat moderation bot who does various other things as well. " +
                        "If you are a user you will probably not need to worry about me, if I have mis-moderated please tell a Discord moderator!")
                .addField("For BisectHosting and MelonCube.", "", false)
                .addField("Source (contribute :D)", "https://github.com/literallyEllie/Steve", false)
                .addField("Author", Joiner.on(", ").join(Steve.AUTHORS), false)
                .build();
        this.helpStaffEmbed = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL)
                .setTitle("Steve Admin help")
                .addField("help", "This menu", false)
                .addField("commands", "All bot commands", false)
                .addField("clear", "Clear up to 50 messages", false)
                .addField("ban", "Ban user", false)
                .addField("kick", "Kick user", false)
                .addField("slowmode", "Turn on slow mode for all non-staff channels", false)
                .addField("cc", "Custom Commands", false)
                .addField("domains", "Whitelist a domain for posting", false)
                .addField("shutdown", "Shutdown the bot", false)
                .build();
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment env) {
        final String[] args = env.getArgs();
        final Member member = env.getSender();

        if (args.length > 0 && member.hasPermission(Permission.MESSAGE_MANAGE)) {

            if (args[0].equalsIgnoreCase("commands")) {
                EmbedBuilder commandList = UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.NEUTRAL);
                getBot().getCommandManager().getCommandMap().values().forEach(command -> commandList.addField(command.getLabel(), command.getDescription(), false));

                env.reply(commandList.build());
            } else if (args[0].equalsIgnoreCase("ok")) {

                long guild = env.getChannel().getGuild().getIdLong();
                if (args.length > 1) {
                    try {
                        guild = Long.parseLong(args[1]);
                    } catch (NumberFormatException e) {
                        if (args[1].startsWith("m")) {
                            guild = Constants.GUILD_MELON.getIdLong();
                        } else guild = Constants.GUILD_BISECT.getIdLong();
                    }
                }

                final MessageCheck messageCheck = getBot().getMessageChecker().getMessageCheck(CheckSpam.class);
                if (messageCheck != null) {
                    final Guild targetGuild = getBot().getJda().getGuildById(guild);
                    if (targetGuild == null) {
                        env.reply(":x: Target server not found (?");
                        return;
                    }

                    env.reply("Manually resetting spam restrictions for server " + targetGuild.getName());

                    if (((CheckSpam) messageCheck).manualReset(guild)) {

                        final Role everyoneRole  = targetGuild.getRoleById(guild == Constants.GUILD_BISECT.getIdLong() ? Constants.ROLE_BISECT_EVERYONE.getIdLong()
                                : Constants.ROLE_MELON_EVERYONE.getIdLong());
                        if (everyoneRole == null) {
                            env.reply(":x: The @-everyone role was not found for the target server.");
                            return;
                        }

                        everyoneRole.getManager().givePermissions(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_WRITE);

                        getBot().getLogger().info("[Mod-Log] " + member.getId() + " (" + member.getEffectiveName() + ") manually" +
                                " reset the spam filter for guild " + targetGuild.getName());
                    } else {
                        env.replyBadSyntax("Server has no active spam check.");
                    }

                } else {
                    env.reply("Spam check is not registered.");
                }

            } else {
                env.reply(helpStaffEmbed);
            }

            return;
        }

        env.reply(botInfoEmbed);
    }

}
