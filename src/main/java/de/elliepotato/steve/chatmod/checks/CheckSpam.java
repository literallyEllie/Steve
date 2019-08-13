package de.elliepotato.steve.chatmod.checks;

import de.elliepotato.steve.chatmod.MessageCheck;
import net.dv8tion.jda.api.entities.Message;

import java.util.LinkedHashSet;

/**
 * @author Ellie :: 13/08/2019
 */
public class CheckSpam implements MessageCheck {

    private LinkedHashSet<Long> messageStampHistory;

    private long lastMessage = System.currentTimeMillis();

    private boolean alerted;

    @Override
    public boolean check(Message message) {
        lastMessage = System.currentTimeMillis();

        return false;
    }

}
