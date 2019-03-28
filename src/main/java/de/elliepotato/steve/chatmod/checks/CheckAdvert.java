package de.elliepotato.steve.chatmod.checks;

import com.google.common.collect.Maps;
import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.chatmod.MessageChecker;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.UtilEmbed;
import de.elliepotato.steve.util.UtilTime;
import net.dv8tion.jda.api.entities.Message;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ellie for VentureNode LLC
 * at 28/03/2019
 */
public class CheckAdvert implements MessageCheck {

    private final Pattern REGEX_DOMAIN = Pattern.compile("\\b((https?:/{2}(w{3}\\.)?)|(w{3}\\.))([^:/\\?\\=]+)\\b", Pattern.MULTILINE);

    private MessageChecker messageChecker;
    private Map<Long, Long> advertCooldown;
    private long requiredAdCooldown;

    public CheckAdvert (MessageChecker messageChecker) {
        this.messageChecker = messageChecker;
        this.advertCooldown = Maps.newHashMap();
        this.requiredAdCooldown = TimeUnit.DAYS.toMillis(1);
    }

    /**
     * Check if the message contains a bad advertisement and also check the message length of a potential advertisement.
     *
     * @param message Message to check.
     * @return "true" if they are safe, "false" if they got rekt.
     */
    @Override
    public boolean check(Message message) {
        String content = message.getContentRaw();
        // if channel isn't advertise channel
        final boolean isAdvertChannel = message.getChannel().getIdLong() != Constants.CHAT_BISECT_AD.getIdLong() &&
                message.getChannel().getIdLong() != Constants.CHAT_MELON_AD.getIdLong();

        Matcher matcher = Pattern.compile("\n").matcher(content);

        // stop unnecessary check, "if channel is an advert channel"
        if (!isAdvertChannel) {
            int lineBreaks = 0;
            while (matcher.find()) {
                lineBreaks++;
                if (lineBreaks > MessageChecker.MAX_AD_LINE) {

                    messageChecker.getBot().tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + "Your advert is **too long**, please reconsider the size to make it smaller." +
                            " (Must fit in " + MessageChecker.MAX_AD_LINE + " lines). If you want your advert back, please check your PMs.", 15, null);
                    messageChecker.getBot().privateMessage(message.getAuthor(), "Your deleted advert: \n```" + message.getContentRaw() + "```");

                    message.delete().queue(foo -> messageChecker.getBot().modLog(message.getGuild(), UtilEmbed.moderatorDeletedMessage(message, "Advert too big (too many lines - > " +
                                    MessageChecker.MAX_AD_LINE + ")",
                            message.getTextChannel())));
                    return false;
                }

            }

            long lastMsg = advertCooldown.getOrDefault(message.getAuthor().getIdLong(), -1L);
            if (lastMsg != -1) {
                messageChecker.getBot().getDebugger().write("last message existed");

                if (!UtilTime.elapsed(lastMsg, requiredAdCooldown)) {
                    messageChecker.getBot().getDebugger().write("not elapsed");

                    messageChecker.getBot().tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + "You last posted an advert less than 24 hours ago, " +
                                    "please wait and then post. If this is a revised advert/a mistake please message a Discord Moderator and they can help you.", 10,
                            message);

                    message.delete().queue(foo -> messageChecker.getBot().modLog(message.getGuild(), UtilEmbed.moderatorDeletedMessage(message,
                            "Advert posted before 24 hours waiting", message.getTextChannel())));
                    return false;
                }
            }

            messageChecker.getBot().getDebugger().write("all good");
        }

        messageChecker.getBot().getDebugger().write("Strict advert check, (message = " + content + ")");


        /*
        // Contains a URL?
        if (!Pattern.compile(".*\\b((https?:/{2}(w{3}\\.)?)|(w{3}\\.))(.*)?", Pattern.MULTILINE).matcher(content).matches() &&
            !Pattern.compile(".*\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b(:\\d{5})?.*", Pattern.MULTILINE).matcher(content).matches()) {
        // doesn't contain URL -- is in ad channel?
            if (!strict && bot.getDebugger().getEnabled()) {
                bot.getDebugger().write("strict: false\n" + "content: `" + content + "` \n" + "match : " + content.matches(".*\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b(:\\d{5})?.*"));
                bot.tempMessage(message.getChannel().getIdLong(), message.getAuthor().getAsMention() + ", your message doesn't look like a server advert and will " +
                        "subsequently be deleted in **10 seconds**, please either talk in #general, if this is a mistake please tell a Discord Moderator.", 10, message);

                bot.modLog(message.getGuild(), embedMessageDelete(message, "It appears to be a non ad-message.", message.getTextChannel()));
                return false;
            }

            return true;
        }
        */

        matcher = REGEX_DOMAIN.matcher(content);

        linkFinder:
        while (matcher.find()) {
            final String domain = matcher.group(5).toLowerCase().trim();

            if (isAdvertChannel) {
                // For every allowed domain
                for (String allowedDomain : messageChecker.getAllowedDomains()) {
                    // Does the checking domain contain the allowed domain?
                    if (domain.contains(allowedDomain.replace("www.", "")))
                        // Ignore and carry on
                        continue linkFinder;

                }
            } else {
                messageChecker.getBot().getDebugger().write("not-advert-channel check ");
                // If it is in an advert channel, check if the link is blacklisted or not.
                boolean foundLink = false;
                // For every denied domain
                for (String deniedLink : messageChecker.getBlacklistedDomains()) {
                    if (domain.contains(deniedLink.replace("www.", ""))) {
                        foundLink = true;
                        break;
                    }
                }

                messageChecker.getBot().getDebugger().write("deleting ? " + foundLink);
                if (!foundLink) {
                    //if (bot.getDebugger().getEnabled()) {
                    advertCooldown.put(message.getAuthor().getIdLong(), System.currentTimeMillis());
                    // bot.getDebugger().write("put in");
                    //}
                    return true;
                }

            }

            // Tailor message
            messageChecker.getBot().tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + ", your message contains a " + (!isAdvertChannel ? "blacklisted link." :
                    "non-authorised link, if you want to share links" +
                            " please say it in PMs.") + " If you want your message back, please ask staff.", 10, null);
            message.delete().queue(foo -> messageChecker.getBot().modLog(message.getGuild(),
                    UtilEmbed.moderatorDeletedMessage(message, "Advertising (" + (isAdvertChannel ? "unauthorised" : "blacklisted") + " link) in #" +
                            message.getChannel().getName(), message.getTextChannel()).addField("Match domain:", domain, true)));
            return false;
        }

        return true;
    }

    /**
     * @return the advert cooldowns.
     */
    public Map<Long, Long> getAdvertCooldown() {
        return advertCooldown;
    }
}
