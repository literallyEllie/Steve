package de.elliepotato.steve.chatmod.checks;

import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.chatmod.MessageChecker;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.UtilEmbed;
import net.dv8tion.jda.api.entities.Message;

import java.util.regex.Matcher;

/**
 * @author Ellie for VentureNode LLC
 * at 28/03/2019
 */
public class CheckTag implements MessageCheck {

    private MessageChecker messageChecker;

    public CheckTag(MessageChecker messageChecker) {
        this.messageChecker = messageChecker;
    }

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
            if (id == Constants.STAFF_MAX.getIdLong()
                    || id == Constants.STAFF_ANDREW.getIdLong()
                    || id == Constants.STAFF_DANIEL.getIdLong()
                    || id == Constants.STAFF_AMBER.getIdLong()
                    || id == Constants.STAFF_JACOB.getIdLong()
                    || id == Constants.STAFF_JOSH.getIdLong()) {

                messageChecker.getBot().tempMessage(message.getTextChannel(), message.getAuthor().getAsMention() + ", Our official staff are very busy and are not always available in Discord. " +
                        "If you require immediate assistance from them please create a support ticket instead. Thank you.", 10, null);
                return true;
            }

            hits++;

            final int finalHits = hits;
            if (finalHits > MessageChecker.MAX_MESSAGE_TAG) {
                message.delete().queue(foo -> messageChecker.getBot().modLog(message.getGuild(), UtilEmbed.moderatorDeletedMessage(message, "Tag spamming", message.getTextChannel())));
                return false;
            }

        }

        return true;
    }

}
