package main.java.me.avankziar.spigot.btm.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import main.java.me.avankziar.general.object.RandomTeleport;
import main.java.me.avankziar.general.object.ServerLocation;
import main.java.me.avankziar.general.objecthandler.KeyHandler;
import main.java.me.avankziar.general.objecthandler.StaticValues;
import main.java.me.avankziar.spigot.btm.BungeeTeleportManager;
import main.java.me.avankziar.spigot.btm.assistance.ChatApi;
import main.java.me.avankziar.spigot.btm.handler.ForbiddenHandler.Mechanics;
import main.java.me.avankziar.spigot.btm.object.BTMSettings;
import net.md_5.bungee.api.chat.ClickEvent;

public class RandomTeleportHelper
{

	private BungeeTeleportManager plugin;
	private HashMap<Player, Long> cooldown = new HashMap<>();
	
	public RandomTeleportHelper(BungeeTeleportManager plugin)
	{
		this.plugin = plugin;
	}
	
	public void randomTeleportTo(Player player, String[] args)
	{
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(cooldown.containsKey(player) && cooldown.get(player) > System.currentTimeMillis())
				{
					return;
				}
				if(args.length != 0)
				{
					///Deine Eingabe ist fehlerhaft, klicke hier auf den Text um &cweitere Infos zu bekommen!
					player.spigot().sendMessage(ChatApi.clickEvent(
							plugin.getYamlHandler().getL().getString("InputIsWrong"),
							ClickEvent.Action.RUN_COMMAND, BTMSettings.settings.getCommands(KeyHandler.BTM)));
					return;
				}
				String playername = player.getName();
				String playeruuid = player.getUniqueId().toString();
				RandomTeleport rt = null;
				if(plugin.getYamlHandler().getConfig().getBoolean("RandomTeleport.UseSimpleTarget", true))
				{
					rt = getSimpleTarget(player, playeruuid, playername);
					if(rt == null)
					{
						return;
					}
				} else
				{
					rt = getComplexTarget(player, playeruuid, playername);
					if(rt == null)
					{
						return;
					}
				}
				
				double price = plugin.getYamlHandler().getConfig().getDouble("CostPer.RandomTeleport", 0.0);
				if(price > 0.0 
						&& !player.hasPermission(StaticValues.PERM_BYPASS_RANDOMTELEPORT_COST)
						&& !player.hasPermission(StaticValues.PERM_BYPASS_RANDOMTELEPORT)
						&& plugin.getEco() != null
						&& plugin.getYamlHandler().getConfig().getBoolean("useVault", false))
				{
					if(!plugin.getEco().has(player, price))
					{
						player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("Economy.NoEnoughBalance")));
						return;
					}
					if(!plugin.getEco().withdrawPlayer(player, price).transactionSuccess())
					{
						return;
					}
					if(plugin.getAdvancedEconomyHandler() != null)
					{
						String comment = plugin.getYamlHandler().getL().getString("Economy.RTComment");
						plugin.getAdvancedEconomyHandler().EconomyLogger(
	        					player.getUniqueId().toString(),
	        					player.getName(),
	        					plugin.getYamlHandler().getL().getString("Economy.RTUUID"),
	        					plugin.getYamlHandler().getL().getString("Economy.RTName"),
	        					plugin.getYamlHandler().getL().getString("Economy.RTORDERER"),
	        					price,
	        					"TAKEN",
	        					comment);
						plugin.getAdvancedEconomyHandler().TrendLogger(player, -price);
					}
				}
				if(cooldown.containsKey(player)) cooldown.replace(player, System.currentTimeMillis()+1000L*3);
				else cooldown.put(player, System.currentTimeMillis()+1000L*3);
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.RequestInProgress")));
				plugin.getUtility().givesEffect(player, Mechanics.RANDOMTELEPORT, true, true);
				plugin.getRandomTeleportHandler().sendPlayerToRT(player, rt, playername, playeruuid);
				return;
			}
		}.runTaskAsynchronously(plugin);
	}
	
	public RandomTeleport getSimpleTarget(Player player, String uuid, String playername)
	{
		String rtcode = plugin.getYamlHandler().getConfig().getString("RandomTeleport.SimpleTarget");
		String[] function = rtcode.split("@");
		if(function.length != 2)
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		if(!function[0].contains(";"))
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		String targetServer = function[0].split(";")[0];
		String targetWorld = function[0].split(";")[1];
		ServerLocation point1 = null;
		ServerLocation point2 = null;
		boolean isArea = true;
		int radius = 0;
		if(function[1].contains("[]"))
		{
			if(function[1].split("[]").length != 2)
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			if(function[1].split("[]")[0].contains(";"))
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			point1 = new ServerLocation(targetServer, targetWorld,
					Double.parseDouble(function[1].split("[]")[0].split(";")[0]),
					Double.parseDouble(function[1].split("[]")[0].split(";")[1]),
					Double.parseDouble(function[1].split("[]")[0].split(";")[2]));
			point2 = new ServerLocation(targetServer, targetWorld,
					Double.parseDouble(function[1].split("[]")[1].split(";")[0]),
					Double.parseDouble(function[1].split("[]")[1].split(";")[1]),
					Double.parseDouble(function[1].split("[]")[1].split(";")[2]));
		} else if(function[1].contains("()"))
		{
			if(function[1].split("()").length != 2)
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			if(function[1].split("()")[0].contains(";"))
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			point1 = new ServerLocation(targetServer, targetWorld,
					Double.parseDouble(function[1].split("()")[0].split(";")[0]),
					Double.parseDouble(function[1].split("()")[0].split(";")[1]),
					Double.parseDouble(function[1].split("()")[0].split(";")[2]));
			isArea = false;
			radius = Integer.parseInt(function[1].split("()")[1]);
		} else
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		return new RandomTeleport(UUID.fromString(uuid), playername, point1, point2, radius, isArea);
	}
	
	public RandomTeleport getComplexTarget(Player player, String uuid, String playername)
	{
		List<String> rtcodes = plugin.getYamlHandler().getConfig().getStringList("RandomTeleport.ComplexTarget");
		ArrayList<String> possibleTargets = new ArrayList<>();
		for(String rtcode : rtcodes)
		{
			String[] targetgroup = rtcode.split(">>");
			if(targetgroup.length != 2)
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			String formWorld = targetgroup[0];
			if(player.getWorld().getName().equals(formWorld))
			{
				possibleTargets.add(rtcode);
			}
		}
		if(possibleTargets.size() == 0)
		{
			return getSimpleTarget(player, uuid, playername);
		}
		int random = plugin.getRandomTeleportHandler().getRandomWithExclusion(new Random(), 0, possibleTargets.size()-1);
		String rtcode = possibleTargets.get(random);
		String[] function = rtcode.split("@");
		if(function.length != 2)
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		if(!function[0].contains(">>"))
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		if(!function[0].split(">>")[1].contains(";"))
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		String[] subfunction = function[0].split(">>")[1].split(";");
		if(subfunction.length != 2)
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		String targetServer = subfunction[0];
		String targetWorld = subfunction[1];
		ServerLocation point1 = null;
		ServerLocation point2 = null;
		boolean isArea = true;
		int radius = 0;
		if(function[1].contains("[]"))
		{
			if(function[1].split("[]").length != 2)
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			if(function[1].split("[]")[0].contains(";"))
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			point1 = new ServerLocation(targetServer, targetWorld,
					Double.parseDouble(function[1].split("[]")[0].split(";")[0]),
					Double.parseDouble(function[1].split("[]")[0].split(";")[1]),
					Double.parseDouble(function[1].split("[]")[0].split(";")[2]));
			point2 = new ServerLocation(targetServer, targetWorld,
					Double.parseDouble(function[1].split("[]")[1].split(";")[0]),
					Double.parseDouble(function[1].split("[]")[1].split(";")[1]),
					Double.parseDouble(function[1].split("[]")[1].split(";")[2]));
		} else if(function[1].contains("()"))
		{
			if(function[1].split("()").length != 2)
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			if(function[1].split("()")[0].contains(";"))
			{
				player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
				return null;
			}
			point1 = new ServerLocation(targetServer, targetWorld,
					Double.parseDouble(function[1].split("()")[0].split(";")[0]),
					Double.parseDouble(function[1].split("()")[0].split(";")[1]),
					Double.parseDouble(function[1].split("()")[0].split(";")[2]));
			isArea = false;
			radius = Integer.parseInt(function[1].split("()")[1]);
		} else
		{
			player.sendMessage(ChatApi.tl(plugin.getYamlHandler().getL().getString("CmdRandomTeleport.ErrorInConfig")));
			return null;
		}
		return new RandomTeleport(UUID.fromString(uuid), playername, point1, point2, radius, isArea);
	}
}
