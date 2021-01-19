package de.elliepotato.steve.chatmod.checks;

import com.google.common.cache.CacheBuilder;
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

/**
 * @author Ellie for VentureNode LLC
 * at 28/03/2019
 */
public class CheckTag implements MessageCheck {

    private static final long WARN_DELAY = TimeUnit.SECONDS.toMillis(90);

    private final MessageChecker messageChecker;

    public CheckTag(MessageChecker messageChecker) {
        this.messageChecker = messageChecker;
    }

    // channel, id
    private final Map<Long, Long> lastWarning = Maps.newHashMap();

    /**
     * Check the message for any tag spamming.
     *
     * @param message The message to check
     * @return "true" if they are safe, "false" if they got rekt.
     */
    @Override
    public boolean check(Message message) {
        String content = message.getContentRaw();

        final Matcher matcher = messageChecker.getBot().getPatternUser().matcher(content);
        int hits = 0;
        while (matcher.find()) {

            final long id = Long.parseLong(matcher.group(1));
            // should replcae with just role check
            if (id == Constants.STAFF_MAX.getIdLong()
                    || id == Constants.STAFF_ANDREW.getIdLong()
                    || id == Constants.STAFF_DANIEL.getIdLong()
                    || id == Constants.STAFF_AMBER.getIdLong()
                    || id == Constants.STAFF_JACOB.getIdLong()
                    || id == Constants.STAFF_JOSH.getIdLong()
                    || id == Constants.STAFF_DALETH.getIdLong()
                    || id == Constants.STAFF_JIM.getIdLong()
                    || id == Constants.STAFF_JUAN.getIdLong()
                    || id == Constants.STAFF_KURTIS.getIdLong()) {

                if (shouldMessage(message.getChannel().getIdLong())) {
                    messageChecker.getBot().tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() +
                            ", Our official staff are very busy and are not always available in Discord. " +
                            "If you require immediate assistance from them please create a support ticket instead. Thank you.", 10, null);
                }

                return true;
            }

            hits++;

            final int finalHits = hits;
            if (finalHits > MessageChecker.MAX_MESSAGE_TAG) {
                message.delete().queue(foo -> messageChecker.getBot().modLog(message.getGuild(),
                        UtilEmbed.moderatorDeletedMessage(message, "Tag spamming", message.getTextChannel())));
                return false;
            }

        }

        return true;
    }

    private boolean shouldMessage(long channel) {
        if (lastWarning.containsKey(channel)) {
            if (UtilTime.elapsed(lastWarning.get(channel), WARN_DELAY)) {
                lastWarning.put(channel, System.currentTimeMillis());
                return true;
            }

            return false;
        }

        lastWarning.put(channel, System.currentTimeMillis());
        return true;
    }

}
