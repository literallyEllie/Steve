package de.elliepotato.steve.chatmod;

import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.module.DataHolder;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;
import net.dv8tion.jda.core.utils.PermissionUtil;
import net.jodah.expiringmap.ExpiringMap;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Ellie for VentureNode LLC
 * at 10/02/2018
 */
public class MessageHistory implements DataHolder {

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
    public boolean call(Message message) {
        final Member member = message.getMember();
        if (!PermissionUtil.canInteract(message.getGuild().getMember(steve.getJda().getUserById(Constants.PRESUMED_SELF.getIdLong())), member))
            return true;
        if (!message.getAttachments().isEmpty()) return false;

        final LinkedList<Message> messages = getMessageHistory(member);
        if (!messages.isEmpty() && messages.size() == MAX_MESSAGE_REPEAT) {

            boolean same = false;
            String lastMessage = null;
            for (Message s : messages) {
                String content = s.getContentRaw();
                if (lastMessage != null) {
                    same = lastMessage.equalsIgnoreCase(content);
                }
                lastMessage = content;
            }

            if (same) {
                // bye bye.
                messages.forEach(delMsg -> {
                    try {
                        delMsg.delete().queue();
                    } catch (ErrorResponseException ignored) {
                    }
                });
                steve.modLog(member.getGuild(), steve.getEmbedBuilder(Steve.DiscordColor.KICK)
                        .setTitle("User kicked " + member.getUser().getName() + "#" + member.getUser().getDiscriminator() + " (" + member.getUser().getIdLong() + ")")
                        .addField("Reason", "Suspicious message activity.", false)
                        .addField("Details", "Spamming \"" + lastMessage + "\"", false));

                message.getGuild().getController().kick(member, "Suspicious message activity. Spamming \"" + lastMessage + "\"").queue();

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
        if (messages.size() > MAX_MESSAGE_REPEAT) {
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

    private LinkedList<Message> getMessageHistory(Member member) {
        return messageHistory.getOrDefault(member.getUser().getIdLong(), Lists.newLinkedList());
    }

}