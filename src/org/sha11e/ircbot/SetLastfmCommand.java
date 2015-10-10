package org.sha11e.ircbot;

import java.util.HashMap;
import org.jibble.pircbot.PircBot;
import org.sha11e.ircbot.Command;

public class SetLastfmCommand
implements Command {
    @Override
    public String getCommandName() {
        return "SetLastfm";
    }

    @Override
    public void handleMessage(PircBot bot, String channel, String hostname, String message, String commandPrefix, HashMap<String, String> authToUser) {
        if (message.split(" ").length == 1) {
            authToUser.put(hostname.toLowerCase(), message.toLowerCase());
        }
    }
}