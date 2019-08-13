package de.elliepotato.steve.chatmod.checks;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.MessageCheck;
import de.elliepotato.steve.module.DataHolder;
import de.elliepotato.steve.util.Constants;
import de.elliepotato.steve.util.UtilEmbed;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import net.jodah.expiringmap.ExpiringMap;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Ellie for VentureNode LLC
 * at 10/02/2018
 */
public class MessageHistory implements DataHolder, MessageCheck {

    private final int MAX_MESSAGE_HISTORY_STORAGE = 30;
    private final int MAX_MESSAGE_REPEAT = 3;

    private Steve steve;
    private Map<Long, LinkedList<Message>> messageHistory;

    /**
     * The spam checker, make sure people are spamming.
     *
     * @param steve Bot instance.
     */
    public MessageHistory(Steve steve) {
        this.steve = steve;
        this.messageHistory = ExpiringMap.builder()
                .expiration(10, TimeUnit.MINUTES)
                .maxSize(MAX_MESSAGE_HISTORY_STORAGE)
                .build();
    }

    @Override
    public void shutdown() {
        messageHistory.clear();
    }

    /**
     * Event call when a message should be checked
     *
     * @param message The message to check
     * @return "true" if they are safe, "false" if they got rekt.
     */
    public boolean check(Message message) {
        final Member member = message.getMember();
        if (!PermissionUtil.canInteract(message.getGuild().getMember(steve.getJda().getUserById(Constants.PRESUMED_SELF.getIdLong())), member))
            return true;
        if (!message.getAttachments().isEmpty()) return false;

        final LinkedList<Message> messages = getMessageHistory(member);
        if (!messages.isEmpty() && messages.size() == MAX_MESSAGE_REPEAT) {

            boolean same = messages.stream().distinct().limit(2).count() <= 1;

            if (same) {

                steve.modLog(member.getGuild(), UtilEmbed.getEmbedBuilder(UtilEmbed.EmbedColor.KICK)
                        .setTitle("User soft-banned " + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + " (" + member.getUser().getIdLong() + ")")
                        .addField("Reason", "Suspicious message activity.", false)
                        .addField("Details", "Spamming \"" + messages.get(0) + "\"", false));

                message.getGuild().ban(member, 1, "Suspicious message activity. Spamming \"" + messages.get(0) + "\"").queue();
                message.getGuild().unban(member.getUser()).queue();

                // bye bye.
                /*
                messages.forEach(delMsg -> {
                    try {
                        delMsg.delete().queue();
                    } catch (ErrorResponseException ignored) {
                    }
                });
                 */

                messageHistory.remove(member.getUser().getIdLong());
                return false;
            }

        }

        logMessage(message);
        return true;
    }

    /**
     * Log a message and remove old ones.
     *
     * @param message The message to log.
     */
    private void logMessage(Message message) {
        Member member = message.getMember();

        final LinkedList<Message> messages = getMessageHistory(message.getMember());
        if (messages.size() >= MAX_MESSAGE_REPEAT) {
            messages.removeLast();
        }

        if (!messageHistory.containsKey(member.getUser().getIdLong())) {
            final LinkedList<Message> toAdd = Lists.newLinkedList();
            toAdd.add(message);
            messageHistory.put(member.getUser().getIdLong(), toAdd);
            return;
        }

        messages.add(message);
    }

    /**
     * Gets the message history of a user.
     *
     * @param member the member to get the message history of.
     * @return their message history or an empty list of they have no cached history.
     */
    private LinkedList<Message> getMessageHistory(Member member) {
        return messageHistory.getOrDefault(member.getUser().getIdLong(), Lists.newLinkedList());
    }

}