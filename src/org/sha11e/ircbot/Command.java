package org.sha11e.ircbot;

import java.util.HashMap;
import org.jibble.pircbot.PircBot;

public interface Command {
    public String getCommandName();

    public void handleMessage(PircBot var1, String var2, String var3, String var4, String var5, HashMap<String, String> var6);
}