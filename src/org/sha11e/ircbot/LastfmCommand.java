package org.sha11e.ircbot;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import org.apache.commons.io.IOUtils;
import org.jibble.pircbot.PircBot;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sha11e.ircbot.Command;

public class LastfmCommand
implements Command {
    @Override
    public String getCommandName() {
        return "Lastfm";
    }

    @Override
    public void handleMessage(PircBot bot, String channel, String hostname, String message, String commandPrefix, HashMap<String, String> authToUser) {
	if (message.isEmpty()) {
            String uname = authToUser.get(hostname);
            if (uname == null) {
                String usage = "Usage: \"" + commandPrefix + "Lastfm <username>\" or associate your hostname with a username: \"" + commandPrefix + "SetLastfm <username>\" so that you can simply do \"" + commandPrefix + "Lastfm\"";
                bot.sendMessage(channel, usage);
            } else {
                bot.sendMessage(channel, this.getArtistAndSong(uname));
            }
        } else if (message.split(" ").length == 1) {
            String x = this.getArtistAndSong(message);
            if (x.equalsIgnoreCase("")) {
                System.out.println("IS EMPTY");
            }
            bot.sendMessage(channel, x);
        }
    }

    private String getArtistAndSong(String username) {
        String theJson = this.downloadLastfmJson(username);
        JSONObject root = null;
        JSONParser parser = new JSONParser();
        try {
            root = (JSONObject)parser.parse(theJson);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        String errorMessage = (String)root.get((Object)"message");
        if (errorMessage != null) {
            return errorMessage;
        }
        JSONObject recentTracks = (JSONObject)root.get((Object)"recenttracks");
        if (recentTracks.get((Object)"user") != null) {
            return recentTracks.get((Object)"user") + " has not scrobbled any tracks yet";
        }
        JSONArray track = (JSONArray)recentTracks.get((Object)"track");
        JSONObject mostRecent = (JSONObject)track.get(0);
        JSONObject artistx = (JSONObject)mostRecent.get((Object)"artist");
        String artist = (String)artistx.get((Object)"#text");
        String songName = (String)mostRecent.get((Object)"name");
        return String.valueOf(artist) + " - " + songName;
    }

    private String downloadLastfmJson(String username) {
        URL website = null;
        try {
            website = new URL("http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&limit=1&user=" + URLEncoder.encode(username, "UTF-8") + "&api_key=823ad6e31d56bdacbbd9957e59957b50&format=json");
            return IOUtils.toString((InputStream)website.openStream(), (String)"UTF-8");
        }
        catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "METHOD FAILED";
    }
}