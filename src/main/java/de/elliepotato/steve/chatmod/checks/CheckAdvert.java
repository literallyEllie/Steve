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

    public CheckAdvert(MessageChecker messageChecker) {
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

        Matcher matcher = REGEX_DOMAIN.matcher(content);

        linkFinder:
        while (matcher.find()) {
            final String domain = matcher.group(5).toLowerCase().trim();
            // For every allowed domain
            for (String allowedDomain : messageChecker.getAllowedDomains()) {
                // Does the checking domain contain the allowed domain?
                if (domain.contains(allowedDomain.replace("www.", "")))
                    // Ignore and carry on
                    continue linkFinder;

            }

            // Tailor message
            messageChecker.getBot().tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() +
                    ", your message contains a " + (/*!isAdvertChannel ? "blacklisted link." :*/
                    "non-authorised link, if you want to share links please say it in PMs.") +
                    " If you want your message back, please ask staff.", 10, null);
            message.delete().queue(foo -> messageChecker.getBot().modLog(message.getGuild(),
                    UtilEmbed.moderatorDeletedMessage(message, "Advertising (unauthorised link) in #" +
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
