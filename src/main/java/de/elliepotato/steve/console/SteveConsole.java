package de.elliepotato.steve.console;

import de.elliepotato.steve.Steve;
import de.elliepotato.steve.util.UtilString;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;

/**
 * @author Ellie for VentureNode LLC
 * at 08/02/2018
 */
public class SteveConsole extends Thread {

    private Steve steve;

    /**
     * The console thread which is only accessable from the backend.
     * Small operations can be done such as shutting down, changing the bot status and
     * looking at global status.
     *
     * @param steve the bot instance.
     */
    public SteveConsole(Steve steve) {
        this.steve = steve;
    }

    @Override
    public void run() {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        Thread.currentThread().setName("Steve-Console");

        while (true) {

            System.out.print("> ");
            try {
                final String input = bufferedReader.readLine();
                if (input == null || input.trim().isEmpty()) continue;
                final String[] args = input.split(" ");

                switch (args[0].toLowerCase()) {
                    case "shutdown":
                    case "stop":
                        steve.getLogger().info("Shutting down...");
                        steve.shutdown();
                        break;
                    case "listening":
                    case "playing":
                    case "watching":
                        if (args.length < 2) {
                            cu((args[0].toLowerCase() + " <to>"));
                            if (steve.getJda().getPresence().getActivity() != null) {
                                steve.getLogger().info("Steve is currently doing: " + steve.getJda().getPresence().getActivity().getName());
                            }
                            break;
                        }
                        String to = UtilString.getFinalArg(args, 1);
                        switch (args[0].toLowerCase()) {
                            case "listening":
                                steve.getJda().getPresence().setActivity(Activity.listening(to));
                                break;
                            case "playing":
                                steve.getJda().getPresence().setActivity(Activity.playing(to));
                                break;
                            case "watching":
                                steve.getJda().getPresence().setActivity(Activity.watching(to));
                                break;
                        }
                        steve.getLogger().info("Now " + args[0].toLowerCase() + " to: " + to);
                        break;
                    case "status":
                        if (args.length < 2) {
                            cu("status <online | idle | dnd | invisible | offline>");
                            steve.getLogger().info("Steve's current status is: " + steve.getJda().getPresence().getStatus().getKey());
                            break;
                        }
                        OnlineStatus onlineStatus = OnlineStatus.fromKey(args[1]);
                        if (onlineStatus == OnlineStatus.UNKNOWN) {
                            steve.getLogger().info("Invalid status `" + args[1] + "`");
                            break;
                        }
                        steve.getJda().getPresence().setStatus(onlineStatus);
                        steve.getLogger().info("Status set to " + onlineStatus.name());
                        break;
                    case "say":
                        if (args.length < 2) {
                            cu("say <channelId> <message>");
                        } else {
                            long channel;
                            try {
                                channel = Long.parseLong(args[1]);
                            } catch (NumberFormatException e) {
                                steve.getLogger().info("Invalid channel ID.");
                                break;
                            }
                            final String message = UtilString.getFinalArg(args, 2);
                            steve.messageChannel(channel, message);
                            steve.getLogger().info("Sent message '" + message + "' to '" + channel + "'.");
                        }
                        break;
                    case "info":
                    case "botinfo":
                        steve.getLogger().info("Existed since " + steve.getJda().getSelfUser().getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME));
                        steve.getLogger().info("ID " + steve.getJda().getSelfUser().getIdLong());
                        steve.getLogger().info("In " + steve.getJda().getGuilds().size() + " guilds");
                        steve.getLogger().info("With " + steve.getJda().getUsers().size() + " users");
                    default:
                        steve.getLogger().info("Unknown command.");
                }


            } catch (Throwable e) {
                steve.getLogger().error("Error on console thread!", e);
                e.printStackTrace();
            }


        }

    }

    private void cu(String x) {
        System.out.println("Correct usage: " + x);
    }

}
