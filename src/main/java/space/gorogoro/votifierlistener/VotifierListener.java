package space.gorogoro.votifierlistener;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VotifierListener extends JavaPlugin implements Listener{

  @Override
  public void onEnable(){
    try{
      getLogger().info("The Plugin Has Been Enabled!");
      getServer().getPluginManager().registerEvents(this, this);

      File configFile = new File(getDataFolder(), "config.yml");
      if(!configFile.exists()){
        saveDefaultConfig();
      }
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onVotifierEvent(VotifierEvent event) {
    try {
      FileConfiguration config = getConfig();
      Vote vote = event.getVote();

      String userName = vote.getUsername();
      Pattern pattern = Pattern.compile("^[_a-zA-Z0-9]{3,16}$");
      Matcher matcher = pattern.matcher(userName);
      if(!matcher.matches()) {
        getLogger().warning("Invalid username. (" + userName + ")");
        return;
      }

      String serviceName = vote.getServiceName();
      String serviceSha256 = getSha256(serviceName);
      if(serviceSha256 == null) {
        getLogger().warning("Invalid service name. (" + serviceName + ")");
        return;
      }

      String msg = config.getString("broadcast-message");
      msg = msg.replace("%name%", userName);
      msg = msg.replace("%service%", serviceName);
      getServer().broadcastMessage(colorize(msg));

      Player p = getServer().getPlayerExact(userName);
      if (p != null) {
        sendGift(p, config);
        p.sendMessage(colorize(config.getString("vote-message")));
      } else {
        int offlineVoteLimitRows = config.getInt("offline-vote-limit-rows");
        List<String> list = config.getStringList("offline-vote-list");
        String line = String.format("%s,%s", userName, serviceSha256);
        if(!list.contains(line)) {
          list.add(line);
          Collections.sort(list);
          if(list.size() <= offlineVoteLimitRows) {
            config.set("offline-vote-list", list);
          } else {
            config.set("offline-vote-list", list.subList(0, offlineVoteLimitRows - 1));
          }
          config.set("offline-vote-limit-rows", offlineVoteLimitRows);
          saveConfig();
        }
      }
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent event) {
    try {
      Player p = event.getPlayer();
      FileConfiguration config = getConfig();

      String userName;
      String[] cols;
      List<String> list = config.getStringList("offline-vote-list");
      List<String> newList = new ArrayList<String>(); 
      for (String line : list) {
        cols = line.split(",");
        if( cols.length != 2) {
          continue;
        }
        userName = cols[0];

        if(!userName.equals(p.getName())) {
          newList.add(line);
          continue;
        }

        sendGift(p, config);
      }

      if (list.size() != newList.size()) {
        p.sendMessage(colorize(config.getString("vote-message")));
        config.set("offline-vote-list", newList);
        saveConfig();
      }

    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  @Override
  public void onDisable(){
    try {
      getLogger().info("The Plugin Has Been Disabled!");
    } catch (Exception e) {
      logStackTrace(e);
    }
  }

  private void sendGift(Player p, FileConfiguration config) {
    for (String cmd : config.getStringList("vote-command-list")) {
      cmd = cmd.replace("%name%", p.getName());
      getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }
  }

  private static String colorize(String str) {
    return ChatColor.translateAlternateColorCodes('&', str);
  }

  private static String getSha256(String str) {
    String ret = null;
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.reset();
      digest.update(str.getBytes("utf8"));
      ret = String.format("%064x", new BigInteger(1, digest.digest()));
    } catch (Exception e) {
      logStackTrace(e);
    }
    return ret;
  }

  private static void logStackTrace(Exception e){
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    pw.flush();
    Bukkit.getLogger().log(Level.WARNING, sw.toString());
  }
}
