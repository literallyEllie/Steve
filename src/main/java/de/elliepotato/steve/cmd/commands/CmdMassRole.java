package de.elliepotato.steve.cmd.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import de.elliepotato.steve.Steve;
import de.elliepotato.steve.cmd.model.Command;
import de.elliepotato.steve.cmd.model.CommandEnvironment;
import de.elliepotato.steve.sheets.request.SheetsRequestBuilder;
import de.elliepotato.steve.sheets.request.SheetsRequestMode;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.PermissionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CmdMassRole extends Command {

    private static final Pattern PATTERN_URL = Pattern.compile("https://docs\\.google\\.com/spreadsheets/d/([a-zA-Z0-9-_]+)(/.*)?");

    public CmdMassRole(Steve steve) {
        super(steve, "massrole", "Mass modification of roles (using Sheets API, read from second row)", Lists.newArrayList(),
                Permission.KICK_MEMBERS, "<add | remove>", "<role>", "<sheet url>", "<column>", "<max rows to read>", "<sheet name>");
    }

    @Override
    protected void abstractExecute(@NotNull CommandEnvironment environment) {
        final String[] args = environment.getArgs();
        final TextChannel channel = environment.getChannel();
        final Member selfUser = channel.getGuild().getMember(getBot().getJda().getSelfUser());

        String mode = args[0].toLowerCase();
        List<Role> roleResults = channel.getGuild().getRolesByName(args[1], true);
        if (roleResults.isEmpty()) {
            environment.replyBadSyntax("No roles found by this name");
            return;
        }

        if (roleResults.size() > 1) {
            environment.replyBadSyntax("Ambigious role name, please clarify between: " + Joiner.on(", ").join(roleResults));
            return;
        }

        Role role = roleResults.get(0);
        final Matcher matcher = PATTERN_URL.matcher(args[2]);
        if (!matcher.matches()) {
            environment.replyBadSyntax("Invalid sheets URL");
            return;
        }

        if (!PermissionUtil.canInteract(selfUser, role)) {
            environment.replyBadSyntax("The role is a higher permission level than the bot");
            return;
        }

        String sheetId = matcher.group(1);

        String column = args[3].toUpperCase();
        if (column.length() > 1) {
            environment.replyBadSyntax("Column can only be 1 character");
            return;
        }

        int maxRows;
        try {
            maxRows = Integer.parseInt(args[4]);
            if (maxRows < 1)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            environment.replyBadSyntax("Invalid max rows, must be above 0");
            return;
        }

        StringBuilder sheetName = new StringBuilder();
        for (int i = 5; i < args.length; i++) {
            sheetName.append(args[i]).append(" ");
        }

        SheetsRequestBuilder sheetsRequestBuilder = new SheetsRequestBuilder(SheetsRequestMode.READ)
                .setSpreadSheetId(sheetId)
                .setSheetName(sheetName.toString().trim())
                .setLowerBound(column + "2") // start at 2 presuming there is a colum header
                .setUpperBound(column + (maxRows + 1)); // it returns 1 less than wanted

        final String[][] response = getBot().getSheetsApi().readSheet(sheetsRequestBuilder);
        if (response == null) {
            environment.replyBadSyntax("Error reading from the sheet");
            return;
        }

        if (response.length == 0) {
            environment.replyBadSyntax("Empty response from reading, check input.");
            return;
        }

        final Guild guild = channel.getGuild();
        boolean addRole = mode.equals("add");

        AtomicInteger successCount = new AtomicInteger();

        getBot().lookupMembers(guild, Arrays.stream(response)
                .map(strings -> strings[0])
                .collect(Collectors.toList())).onSuccess(members -> {

            members.forEach(member -> {
                if (addRole) {
                    guild.addRoleToMember(member, role).queue();
                } else
                    guild.removeRoleFromMember(member, role).queue();

                successCount.getAndIncrement();
            });

            if (successCount.get() < response.length) {
                List<String> inconsistencies = Lists.newArrayList();

                for (String[] strings : response) {
                    final String tag = strings[0];

                    boolean match = false;
                    for (Member member : members) {
                        if (tag.equals(member.getUser().getName() + "#" + member.getUser().getDiscriminator())) {
                            match = true;
                            break;
                        }
                    }

                    if (!match)
                        inconsistencies.add(tag + " - not in this Discord server");
                }

                environment.reply("There were issues giving/taking to:\n" + Joiner.on("\n").join(inconsistencies));
            }

            if (successCount.get() > 0) {
                environment.replySuccess((addRole ? "Gave" : "Removed") + " the role to " + successCount.get() + " users");
            }

        });

    }

}
