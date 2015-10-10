package org.sha11e.ircbot;

import java.util.HashMap;
import org.jibble.pircbot.PircBot;
import org.sha11e.ircbot.Command;

public class HelpCommand
implements Command {
    @Override
    public String getCommandName() {
        return "Help";
    }

    @Override
    public void handleMessage(PircBot bot, String channel, String hostname, String message, String commandPrefix, HashMap<String, String> authToUser) {
        bot.sendMessage(channel, "Commands availible: " + commandPrefix + "lastfm, " + commandPrefix + "np, " + commandPrefix + "cp, " + commandPrefix + "topartists and " + commandPrefix + "toptracks");
    }
}