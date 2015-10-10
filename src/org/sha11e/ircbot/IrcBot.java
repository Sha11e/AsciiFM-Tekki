package org.sha11e.ircbot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

public class IrcBot
extends PircBot {
    private final BotProperties props = new BotProperties();
    private final String commandPrefix = this.props.getProperty("irc.command.prefix");
    private final String avoidAuth = this.props.getProperty("irc.avoid.auth");
    private boolean channelPost = false;
    private HashMap<String, String> authToUser = null;
    private List<Command> commands = new ArrayList<Command>();

    public IrcBot() {
        System.out.println(String.valueOf(IrcBot.getTime()) + " - Setting up the bot");
        this.addCommands();
        this.createEssentialFolders();
        this.loadAuthToUsername();
        this.setLogin(this.props.getProperty("irc.login"));
        this.setVersion(this.props.getProperty("irc.version"));
        this.setFinger(this.props.getProperty("irc.finger"));
        this.setName(this.props.getProperty("irc.nickname"));
        try {
            this.setEncoding(this.props.getProperty("irc.encoding"));
        }
        catch (UnsupportedEncodingException e) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - Unsupported encoding error: " + e.getMessage());
        }
        this.setAutoNickChange(true);
        this.connectToIrc();
    }

    private void addCommands() {
        this.commands.add((Command)new LastfmCommand());
        this.commands.add((Command)new SetLastfmCommand());
        //this.commands.add((Command)new CompatibleCommand());
        //this.commands.add((Command)new TopTracksCommand());
        //this.commands.add((Command)new TopArtistsCommand());
        //this.commands.add((Command)new HelpCommand());
    }

    private void loadAuthToUsername() {
        try {
            FileInputStream fis = new FileInputStream("authToUsername.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.authToUser = (HashMap)ois.readObject();
            ois.close();
            fis.close();
        }
        catch (IOException e) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - Could not find a authToUsername object. Creating an empty one.");
            this.authToUser = new HashMap();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectToIrc() {
        System.out.println(String.valueOf(IrcBot.getTime()) + " - Connecting to IRC");
        String server = this.props.getProperty("irc.server");
        int port = Integer.parseInt(this.props.getProperty("irc.port"));
        try {
            this.connect(server, port);
        }
        catch (NickAlreadyInUseException e) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - Nick already in use. Adding numbers to get a unique nick");
        }
        catch (IOException e) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - IOException: " + e.getMessage());
        }
        catch (IrcException e) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - IrcException: " + e.getMessage());
        }
    }

    private void giefBinds() {
        HashMap a = null;
        try {
            FileInputStream fis = new FileInputStream("authToUsername.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            a = (HashMap)ois.readObject();
            ois.close();
            fis.close();
        }
        catch (IOException e) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - Could not find a authToUsername object. Creating an empty one.");
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Iterator y = a.keySet().iterator();
        Iterator x = a.values().iterator();
        while (x.hasNext()) {
            System.out.println(String.valueOf((String)y.next()) + " - " + (String)x.next());
        }
    }

    protected void onConnect() {
        System.out.println(String.valueOf(IrcBot.getTime()) + " - Authenticating, hiding hostmask and the channel");
        String authBot = this.props.getProperty("irc.authbot");
        String username = this.props.getProperty("irc.username");
        String password = this.props.getProperty("irc.password");
        String hostmask = this.props.getProperty("irc.hostmask");
        String channel = this.props.getProperty("irc.channel");
        String channelPassword = this.props.getProperty("irc.channel.password");
        this.sendRawLine("PRIVMSG " + authBot + " :AUTH " + username + " " + password);
        this.sendRawLine(hostmask.replaceFirst("(?i)botname", this.getNick()));
        try {
            Thread.sleep(300);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (channelPassword.isEmpty()) {
            this.joinChannel(channel);
        } else {
            this.joinChannel(channel, channelPassword);
        }
        System.out.println(String.valueOf(IrcBot.getTime()) + " - The bot has been set up");
    }

    protected void onUserList(String channel, User[] users) {
        String avoidNick = this.props.getProperty("irc.avoid.nick");
        for (User user : users) {
            if (!user.getNick().equalsIgnoreCase(avoidNick) && !user.getNick().equalsIgnoreCase("botumak")) continue;
            this.channelPost = false;
            System.out.println(String.valueOf(IrcBot.getTime()) + " - " + avoidNick + " is in the channel. Setting channelPost to false.");
            return;
        }
        this.channelPost = true;
        System.out.println(String.valueOf(IrcBot.getTime()) + " - " + avoidNick + " is not in the channel. Setting channelPost to true.");
    }

    protected void onJoin(String channel, String sender, String login, String hostname) {
        if (hostname.equalsIgnoreCase(this.avoidAuth)) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - " + this.avoidAuth + " joined the channel. Setting channelPost to false.");
            this.channelPost = false;
        }
    }

    protected void onPart(String channel, String sender, String login, String hostname) {
        if (hostname.equalsIgnoreCase(this.avoidAuth)) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - " + this.avoidAuth + " parted the channel. Setting channelPost to true.");
            this.channelPost = true;
        }
    }

    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        if (sourceHostname.equalsIgnoreCase(this.avoidAuth)) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - " + this.avoidAuth + " quit the server. Setting channelPost to true.");
            this.channelPost = true;
        }
        if (sourceNick.equalsIgnoreCase(this.getNick())) {
            System.out.println("Thefk onquit got called for me");
            this.serializeAuthToUsername();
        }
    }

    private void serializeAuthToUsername() {
        try {
            FileOutputStream fos = new FileOutputStream("authToUsername.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.authToUser);
            oos.close();
            fos.close();
        }
        catch (FileNotFoundException e) {
            System.out.println("Could not create authToUsername.ser: " + e.getMessage());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void onMessage(String channel, String sender, String login, String hostname, String message) {	
	boolean letThroughAnyway;
        boolean bl = letThroughAnyway = hostname.equalsIgnoreCase("IcedTxx.users.quakenet.org") && message.startsWith(this.commandPrefix) || message.toLowerCase().startsWith("!setlastfm");
        if (message.equalsIgnoreCase("!tekki") && hostname.equalsIgnoreCase("wubdagg.users.quakenet.org")) {
            this.sendMessage(channel, "Hehehehe.");
            return;
        }
        if (message.toLowerCase().startsWith("!np")) {
            message = message.replaceFirst("(?i)!np", "!lastfm");
        }
        if (hostname.equalsIgnoreCase(this.avoidAuth) && this.channelPost) {
            this.channelPost = false;
            System.out.println(String.valueOf(hostname) + " talked in the channel. Setting channelPost to false");
            return;
        }
        if (!(message.startsWith(this.commandPrefix) && (this.channelPost || letThroughAnyway))) {
            return;
        }
        
        if (hostname.equalsIgnoreCase("highgate.irccloud.com") && (message.toLowerCase().startsWith("!lastfm") || message.toLowerCase().startsWith("!setlastfm"))) {
            hostname = "irccloud." + login;
        }
        for (Command cmd : this.commands) {
            String cmdName = cmd.getCommandName().toLowerCase();
            if (!message.toLowerCase().startsWith(String.valueOf(this.commandPrefix) + cmdName)) continue;
            String msg = message.replaceFirst("(?i)" + this.commandPrefix + cmdName, "").trim();
            cmd.handleMessage((PircBot)this, channel, hostname, msg, this.commandPrefix, this.authToUser);
        }
    }

    protected void onPrivateMessage(String sender, String login, String hostname, String message) {
	       if (hostname.equalsIgnoreCase("wubdagg.users.quakenet.org") && (message.toLowerCase().startsWith("!adduser")))   {
		   message = message.substring(message.indexOf(" "));
		   message = message.trim();
		   String[] argss = message.split(" ");
		   authToUser.put(argss[0], argss[1]);
		   serializeAuthToUsername();
		   System.out.println("Key: " + argss[0] + " -> " + argss[1]);
	       }
	
	System.out.println(String.valueOf(IrcBot.getTime()) + " - " + sender + " PM'd: " + message);
        if (!hostname.equalsIgnoreCase(this.props.getProperty("irc.master"))) {
            return;
        }
        if (message.equalsIgnoreCase("!help")) {
            this.sendMessage(sender, "Commands: " + this.commandPrefix + "SetChannelPost [true|false], rawline: <rawlineMessage>, " + this.commandPrefix + "getBinds");
        } else if (message.toLowerCase().startsWith(String.valueOf(this.commandPrefix) + "setchannelpost")) {
            this.channelPost = message.toLowerCase().indexOf("true") != -1;
            System.out.println("Setting channelPost to true");
        } else if (message.toLowerCase().startsWith("rawline:")) {
            String command = message.replaceFirst("(?i)RAWLINE:", "");
            if (command.toLowerCase().startsWith("quit")) {
                this.sendRawLine(command);
                System.out.println("Master sent us a quit message. Serializing AuthToUsername.");
                this.serializeAuthToUsername();
                System.exit(0);
            }
            this.sendRawLine(command);
        } else if (message.equalsIgnoreCase("!getBinds")) {
            this.giefBinds();
        }
    }

    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        if (recipientNick.equalsIgnoreCase(this.getNick())) {
            System.out.println(String.valueOf(IrcBot.getTime()) + " - The bot was kicked by " + kickerNick + ":" + reason);
        }
    }

    protected void onDisconnect() {
        this.serializeAuthToUsername();
        System.out.println("onDisconnect! The Auth to Nick file has been serialized.");
        this.connectToIrc();
    }

    private void createEssentialFolders() {
        File dir = new File("tmp");
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            }
            catch (SecurityException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String getTime() {
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Copenhagen"));
        return df.format(date);
    }
}