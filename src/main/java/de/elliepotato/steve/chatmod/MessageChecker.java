package de.elliepotato.steve.chatmod;

import com.google.common.collect.Sets;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.chatmod.checks.CheckAdvert;
import de.elliepotato.steve.chatmod.checks.CheckTag;
import de.elliepotato.steve.chatmod.checks.MessageHistory;
import de.elliepotato.steve.chatmod.checks.help.DumbResponder;
import de.elliepotato.steve.chatmod.checks.spam.CheckSpam;
import de.elliepotato.steve.config.FileHandler;
import de.elliepotato.steve.module.DataHolder;
import de.elliepotato.steve.util.Constants;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.io.IOException;
import java.util.Set;

/**
 * @author Ellie for VentureNode LLC
 * at 04/02/2018
 */
public class MessageChecker extends ListenerAdapter implements DataHolder {

    public static final int MAX_MESSAGE_TAG = 8, MAX_AD_LINE = 8; // line breaks
    private Steve bot;

    private FileHandler<Set<String>> domainsFile, blacklistedFile;
    private Set<String> allowedDomains, blacklistedDomains;

    private Set<MessageCheck> messageChecks;

    /**
     * The big bad chat moderating module.
     *
     * @param bot The bot instance.
     */
    public MessageChecker(Steve bot) {
        this.bot = bot;
        this.domainsFile = new DomainsFile(bot);
        this.allowedDomains = Sets.newHashSet();
        this.blacklistedFile = new BlacklistedDomainsFile(bot);
        this.blacklistedDomains = Sets.newHashSet();
        this.messageChecks = Sets.newHashSet(new CheckSpam(bot), new MessageHistory(bot), new CheckTag(this),
                new CheckAdvert(this), new DumbResponder(bot));

        try {
            this.allowedDomains = domainsFile.read();
            this.blacklistedDomains = blacklistedFile.read();
        } catch (IOException e) {
            bot.getLogger().error("Failed to load domains!", e);
            e.printStackTrace();
        }

    }

    @Override
    public void shutdown() {

        for (MessageCheck messageCheck : messageChecks) {
            if (messageCheck instanceof DataHolder)
                ((DataHolder) messageCheck).shutdown();
        }

        messageChecks.clear();
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().getIdLong() == Constants.PRESUMED_SELF.getIdLong()) return;

        long channelId = event.getChannel().getIdLong();
        if (channelId == Constants.CHAT_BISECT_MOD.getIdLong() || channelId == Constants.CHAT_MELON_MOD.getIdLong())
            return;

        if (!bot.getDebugger().getEnabled() &&
                !PermissionUtil.canInteract(event.getGuild().getMember(bot.getJda().getUserById(Constants.PRESUMED_SELF.getIdLong())), event.getMember()))
            return;

        // Process messages, if a check returns false  it will stop.
        for (MessageCheck messageCheck : messageChecks) {
            if (!messageCheck.check(event.getMessage()))
                return;
        }

    }

    /**
     * @return gets the bot instance.
     */
    public Steve getBot() {
        return bot;
    }

    /**
     * @return The allowed domains.
     */
    public Set<String> getAllowedDomains() {
        return allowedDomains;
    }

    /**
     * @return The domains file handler.
     */
    public FileHandler<Set<String>> getDomainsFile() {
        return domainsFile;
    }

    /**
     * @return the blacklisted domains handler.
     */
    public FileHandler<Set<String>> getBlacklistedFile() {
        return blacklistedFile;
    }

    /**
     * @return The preloaded blacklisted domains.
     */
    public Set<String> getBlacklistedDomains() {
        return blacklistedDomains;
    }

    /**
     * @return gets the checks that are done when a message is sent.
     */
    public Set<MessageCheck> getMessageChecks() {
        return messageChecks;
    }

    public <T extends MessageCheck> MessageCheck getMessageCheck(Class<T> check) {
        for (MessageCheck messageCheck : messageChecks) {
            if (messageCheck.getClass().equals(check))
                return messageCheck;
        }
        return null;
    }

}
