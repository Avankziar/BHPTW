package main.java.me.avankziar.spigot.bungeeteleportmanager.handler;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import main.java.me.avankziar.spigot.bungeeteleportmanager.BungeeTeleportManager;

public class AdvanceEconomySC
{
	public static void EconomyLogger(String fromUUID, String fromName,
			String toUUID, String toName,
			String orderer, double price, String type, String comment)
	{
		Bukkit.getPluginManager().callEvent(new de.secretcraft.economy.spigot.events.EconomyLoggerEvent(
				LocalDateTime.now(), 
				fromUUID,
				toUUID, 
				fromName, 
				toName,
				orderer, 
				price, 
				de.secretcraft.economy.spigot.events.EconomyLoggerEvent.Type.valueOf(type),
				comment));
	}
	
	public static void TrendLogger(BungeeTeleportManager plugin, OfflinePlayer player, double price)
	{
		Bukkit.getPluginManager().callEvent(new de.secretcraft.economy.spigot.events.TrendLoggerEvent(
				LocalDate.now(),
				player.getUniqueId().toString(), 
				price, 
				plugin.getEco().getBalance(player)));
	}
}
