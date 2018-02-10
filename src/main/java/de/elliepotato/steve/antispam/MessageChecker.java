package de.elliepotato.steve.antispam;

import com.google.common.collect.Sets;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ellie for VentureNode LLC
 * at 04/02/2018
 */
public class MessageChecker extends ListenerAdapter {

    private final int MAX_MESSAGE_TAG = 8;
    private final int MAX_AD_LINE = 7; // line breaks
    private final Pattern REGEX_DOMAIN = Pattern.compile("\\b((?=[a-z0-9-]{1,63}\\.)(xn--)?([a-z0-9]+(-[a-z0-9]+)*\\.))+[a-z]{2,63}\\b");
    private Steve bot;
    private Set<String> allowedDomains = Sets.newHashSet("hastebin.com", "pastebin.com", "google.com", "google.co.uk", "google.no",
            "meloncube.net", "bisecthosting.com", "discord.gg", "discordapp.com", "dis.gd", "discord.co", "discord.com", "spigotmc.org",
            "bukkit.org", "minecraft.net", "mojang.com", "minecraftforge.net", "wikipedia.org", "stackoverflow.com", "prnt.sc", "imgur.com",
            "strawpoll.me", "strawpoll.com", "github.com", "mc-market.org", "ess3.net", "filezilla-project.org", "youtube.com", "mc-ess.net");

    private MessageHistory messageHistory;

    /**
     * The big bad chat moderating module.
     * @param bot The bot instance.
     */
    public MessageChecker(Steve bot) {
        this.bot = bot;
        this.messageHistory = new MessageHistory(bot);
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().getIdLong() == Constants.PRESUMED_SELF.getIdLong()) return;

        long channelId = event.getChannel().getIdLong();
        if (channelId == Constants.CHAT_BISECT_STAFF.getIdLong() || channelId == Constants.CHAT_MELON_STAFF.getIdLong())
            return;

        if (!tagCheck(event.getMessage())) return;
        if (!advertCheck(event.getMessage())) return;
        if (!messageHistory.call(event.getMessage())) return;

        // ...
    }

    /**
     * Check the message for any tag spamming
     * @param message The message to check
     * @return "true" if they are safe, "false" if they got rekt.
     */
    private boolean tagCheck(Message message) {
        String content = message.getContentRaw();

        final Matcher matcher = bot.getPatternUser().matcher(content);
        int hits = 0;
        while (matcher.find()) {

            final long id = Long.parseLong(matcher.group(1));
            if (id == Constants.STAFF_MAX.getIdLong()
                    || id == Constants.STAFF_ANDREW.getIdLong()
                    || id == Constants.STAFF_DANIEL.getIdLong()
                    || id == Constants.STAFF_AMBER.getIdLong()
                    || id == Constants.STAFF_JACOB.getIdLong()) {

                boolean bh = message.getGuild().getIdLong() == Constants.GUILD_BISECT.getIdLong();

                bot.tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + ", Please do not tag "
                        + (bh ? "BH" : "MC") + " staff, they will not reply. Instead please wait for a ask in <#" +
                        (bh ? String.valueOf(Constants.CHAT_BISECT_HELP.getIdLong()) : String.valueOf(Constants.CHAT_MELON_HELP.getIdLong())) +
                        "> or open a ticket. (If your request was long, ask the Discord Moderators for it back)", 10, null);

                message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Tagging official staff", message.getTextChannel())));
                return false;
            }

            hits++;

            final int finalHits = hits;
            if (finalHits > MAX_MESSAGE_TAG) {
                message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Tag spamming", message.getTextChannel())));
                return false;
            }

        }

        return true;
    }

    /**
     * Check if the message contains a bad advertisement and also check the message length of a potential advertisement.
     * @param message Message to check.
     * @return "true" if they are safe, "false" if they got rekt.
     */
    public boolean advertCheck(Message message) {
        String content = message.getContentRaw();
        // if channel isnt advertise channel
        final boolean strict = message.getChannel().getIdLong() != Constants.CHAT_BISECT_AD.getIdLong() &&
                message.getChannel().getIdLong() != Constants.CHAT_MELON_AD.getIdLong();

        Matcher matcher = Pattern.compile("\n").matcher(content);

        // stop unnessary check, "if channel is an advert channel"
        if (!strict) {
            int lineBreaks = 0;
            while (matcher.find()) {
                lineBreaks++;
                if (lineBreaks > MAX_AD_LINE) {

                    bot.tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + "Your advert is too long, please reconsider the size to make it smaller." +
                            " If you want your advert back, please check your PMs.", 10, null);
                    bot.privateMessage(message.getAuthor(), "Your deleted advert: \n```" + message.getContentRaw() + "```");

                    message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Advert too big (too many lines)", message.getTextChannel())));
                    return false;
                }

            }
            // if channel isn't an advert channel
        } else {

            if (!content.matches(".*\\b((https?:/{2}(w{3}\\.)?)|(w{3}\\.)).*")) return true;

            matcher = REGEX_DOMAIN.matcher(content);

            while (matcher.find()) {
                final String domain = matcher.group().toLowerCase().trim();

                if (allowedDomains.contains(domain.replace("www.", ""))) continue;

                // if they aren't in a advert room, be strict!
                bot.tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + ", your message contains a non-authorised link, if you want to share links" +
                        " please say it in PMs. If you want your message back, please ask staff.", 10, null);
                message.delete().queue(foo -> bot.modLog(message.getGuild(), embedMessageDelete(message, "Advertising (unauthorised link) in #" + message.getChannel().getName(), message.getTextChannel())));
                return false;
            }

        }

        return true;
    }

    /**
     * Quick embed builder getter
     * @param message Message deleted
     * @param reason Reason message was deleted
     * @param channel Channel message was deleted from
     * @return The embed builder template, with the title and reason filled in.
     */
    private EmbedBuilder embedMessageDelete(Message message, String reason, TextChannel channel) {
        final User author = message.getAuthor();
        return bot.getEmbedBuilder(Steve.DiscordColor.MESSAGE_DELETE)
                .setTitle("Deleted message from " + author.getName() + "#" + author.getDiscriminator() + " (" + author.getIdLong() + ")" +
                        " in channel #" + channel.getName()).addField("Message content:", message.getContentRaw(), false)
                .addField("Reason:", reason, false);
    }

}
