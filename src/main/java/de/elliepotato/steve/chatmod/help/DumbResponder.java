package de.elliepotato.steve.chatmod.help;

import de.elliepotato.steve.Steve;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

/**
 * @author Ellie for VentureNode LLC
 * at 05/03/2018
 */
public class DumbResponder {

    private Steve steve;

    /**
     * A thing that just takes a message and see if it can help.
     * It is dumb because its just a bunch of ifs.
     *
     * @param steve Bot instance.
     */
    public DumbResponder(Steve steve) {
        this.steve = steve;
    }

    /**
     * The method to call on a message is sent.
     *
     * @param message the message sent.
     */
    public void onMessage(Message message) {
        final Guild guild = message.getGuild();
        final Member member = message.getMember();
        final String msg = message.getContentRaw();


        if (msg.contains("daemon") && (msg.contains("110") || msg.contains("111"))) {
            steve.messageChannel(message.getChannel().getIdLong(), "Hi there, " + member.getAsMention() + ". " +
                    "If you were talking about your console log not showing, it is just temporary and should be back soon! " +
                    "If it persists after about 15 minutes you can open a ticket");
        }

    }

}
