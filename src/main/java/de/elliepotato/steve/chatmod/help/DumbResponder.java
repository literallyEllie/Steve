package de.elliepotato.steve.chatmod.help;

import com.google.common.base.Joiner;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.commands.CmdPromo;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.sql.Connection;
import java.util.List;

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
        final String msg = message.getContentRaw().toLowerCase();


        if (msg.contains("daemon") && (msg.contains("110") || msg.contains("111"))) {
            steve.messageChannel(message.getChannel().getIdLong(), "Hi there, " + member.getAsMention() + ". " +
                    "If you were talking about your console log not showing, it is just temporary and should be back soon! " +
                    "If it persists after about 15 minutes you can open a ticket");
            return;
        }

        if (msg.contains("sale") || msg.contains("promo codes") || msg.contains("promotion") ||
                msg.contains("promotional") || msg.contains("coupon")) {

            List<String> codes = ((CmdPromo) steve.getCommandManager().getCommand("promo", false)).getCodesOf(guild);
            steve.messageChannel(message.getChannel().getIdLong(), codes.isEmpty() ? "Sorry there are currently no current promotional codes!" :
                    "Current codes: " + Joiner.on(", ").join(codes));

            return;
        }

        if (msg.matches("((can someone|i need) )?help( me)?( please)?\\??")) {

            String newMsg = "Hi there, " + member.getAsMention() + ". If you are looking for help please specify the problems you're experiencing " +
                    "as well as any problems in console (if necessary). A real person will get to you soon!";

            if (message.getChannel().getIdLong() == Constants.CHAT_BISECT_GENERAL.getIdLong() || message.getChannel().getIdLong() ==
                    Constants.CHAT_MELON_GENERAL.getIdLong()) {
                newMsg += " Also could you move help into <#" + (guild.getIdLong() == Constants.CHAT_BISECT_HELP.getIdLong() ?
                        Constants.CHAT_BISECT_HELP.getIdLong() : Constants.CHAT_MELON_HELP.getIdLong() + ">");
            }

            steve.messageChannel(message.getChannel().getIdLong(), newMsg);
        }



    }

}
