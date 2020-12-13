package main.java.me.avankziar.spigot.btm;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import main.java.me.avankziar.general.objecthandler.KeyHandler;
import main.java.me.avankziar.general.objecthandler.StaticValues;
import main.java.me.avankziar.spigot.btm.assistance.BungeeBridge;
import main.java.me.avankziar.spigot.btm.assistance.Utility;
import main.java.me.avankziar.spigot.btm.cmd.BTMCommandExecutor;
import main.java.me.avankziar.spigot.btm.cmd.BackCommandExecutor;
import main.java.me.avankziar.spigot.btm.cmd.CommandHelper;
import main.java.me.avankziar.spigot.btm.cmd.HomeCommandExecutor;
import main.java.me.avankziar.spigot.btm.cmd.TABCompletionOne;
import main.java.me.avankziar.spigot.btm.cmd.TABCompletionTwo;
import main.java.me.avankziar.spigot.btm.cmd.TpCommandExecutor;
import main.java.me.avankziar.spigot.btm.cmd.WarpCommandExecutor;
import main.java.me.avankziar.spigot.btm.cmd.tree.ArgumentModule;
import main.java.me.avankziar.spigot.btm.cmd.tree.BaseConstructor;
import main.java.me.avankziar.spigot.btm.cmd.tree.CommandConstructor;
import main.java.me.avankziar.spigot.btm.database.MysqlHandler;
import main.java.me.avankziar.spigot.btm.database.MysqlSetup;
import main.java.me.avankziar.spigot.btm.database.YamlHandler;
import main.java.me.avankziar.spigot.btm.database.YamlManager;
import main.java.me.avankziar.spigot.btm.handler.AdvanceEconomyHandler;
import main.java.me.avankziar.spigot.btm.listener.BackListener;
import main.java.me.avankziar.spigot.btm.listener.CustomTeleportListener;
import main.java.me.avankziar.spigot.btm.listener.PlayerOnCooldownListener;
import main.java.me.avankziar.spigot.btm.listener.ServerAndWordListener;
import main.java.me.avankziar.spigot.btm.manager.BackHandler;
import main.java.me.avankziar.spigot.btm.manager.BackHelper;
import main.java.me.avankziar.spigot.btm.manager.BackMessageListener;
import main.java.me.avankziar.spigot.btm.manager.CustomHandler;
import main.java.me.avankziar.spigot.btm.manager.CustomMessageListener;
import main.java.me.avankziar.spigot.btm.manager.GeneralHandler;
import main.java.me.avankziar.spigot.btm.manager.HomeHandler;
import main.java.me.avankziar.spigot.btm.manager.HomeHelper;
import main.java.me.avankziar.spigot.btm.manager.HomeMessageListener;
import main.java.me.avankziar.spigot.btm.manager.SavePointHandler;
import main.java.me.avankziar.spigot.btm.manager.SavePointHelper;
import main.java.me.avankziar.spigot.btm.manager.SavePointMessageListener;
import main.java.me.avankziar.spigot.btm.manager.TeleportHandler;
import main.java.me.avankziar.spigot.btm.manager.TeleportHelper;
import main.java.me.avankziar.spigot.btm.manager.TeleportMessageListener;
import main.java.me.avankziar.spigot.btm.manager.WarpHandler;
import main.java.me.avankziar.spigot.btm.manager.WarpHelper;
import main.java.me.avankziar.spigot.btm.manager.WarpMessageListener;
import main.java.me.avankziar.spigot.btm.metric.Metrics;
import main.java.me.avankziar.spigot.btm.object.BTMSettings;
import net.milkbowl.vault.economy.Economy;

public class BungeeTeleportManager extends JavaPlugin
{
	public static Logger log;
	public String pluginName = "BungeeTeleportManager";
	private YamlHandler yamlHandler;
	private static YamlManager yamlManager;
	private MysqlSetup mysqlSetup;
	private MysqlHandler mysqlHandler;
	private Utility utility;
	private static BungeeTeleportManager plugin;
	//private static BackgroundTask backgroundTask;
	private BungeeBridge bungeeBridge;
	private CommandHelper commandHelper;
	private Economy eco;
	private AdvanceEconomyHandler advanceEconomyHandler;
	
	private GeneralHandler generalHandler;
	private BackHandler backHandler;
	private BackHelper backHelper;
	private CustomHandler customHandler;
	private HomeHandler homeHandler;
	private HomeHelper homeHelper;
	private SavePointHandler savePointHandler;
	private SavePointHelper savePointHelper;
	private TeleportHandler teleportHandler;
	private TeleportHelper teleportHelper;
	private WarpHandler warpHandler;
	private WarpHelper warpHelper;
	
	public static LinkedHashMap<String, ArrayList<String>> homes;
	public static LinkedHashMap<String, ArrayList<String>> warps;
	
	private ArrayList<String> players;
	private ArrayList<CommandConstructor> commandTree;
	private ArrayList<BaseConstructor> helpList;
	private LinkedHashMap<String, ArgumentModule> argumentMap;
	public static String infoCommandPath = "CmdBtm";
	public static String infoCommand = "/"; //InfoComamnd
	
	public void onEnable()
	{
		plugin = this;
		log = getLogger();
		
		//https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=AEP
		log.info(" ██████╗ ████████╗███╗   ███╗ | API-Version: "+plugin.getDescription().getAPIVersion());
		log.info(" ██╔══██╗╚══██╔══╝████╗ ████║ | Author: "+plugin.getDescription().getAuthors().toString());
		log.info(" ██████╔╝   ██║   ██╔████╔██║ | Plugin Website: "+plugin.getDescription().getWebsite());
		log.info(" ██╔══██╗   ██║   ██║╚██╔╝██║ | Depend Plugins: "+plugin.getDescription().getDepend().toString());
		log.info(" ██████╔╝   ██║   ██║ ╚═╝ ██║ | SoftDepend Plugins: "+plugin.getDescription().getSoftDepend().toString());
		log.info(" ╚═════╝    ╚═╝   ╚═╝     ╚═╝ | LoadBefore: "+plugin.getDescription().getLoadBefore().toString());
		
		commandTree = new ArrayList<>();
		helpList = new ArrayList<>();
		argumentMap = new LinkedHashMap<>();
		
		try
		{
			yamlHandler = new YamlHandler(this);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		utility = new Utility(plugin);
		homes = new LinkedHashMap<String, ArrayList<String>>();
		warps = new LinkedHashMap<String, ArrayList<String>>();
		setMysqlPlayers(new ArrayList<String>());
		if (yamlHandler.getConfig().getBoolean("Mysql.Status", false) == true)
		{
			mysqlHandler = new MysqlHandler(plugin);
			mysqlSetup = new MysqlSetup(plugin);
		} else
		{
			log.severe("MySQL is not set in the Plugin " + pluginName + "!");
			Bukkit.getPluginManager().getPlugin(pluginName).getPluginLoader().disablePlugin(this);
			return;
		}
		BTMSettings.initSettings(plugin);
		bungeeBridge = new BungeeBridge(plugin);
		commandHelper = new CommandHelper(plugin);
		generalHandler = new GeneralHandler(plugin);
		backHelper = new BackHelper(plugin);
		backHandler = new BackHandler(plugin);
		customHandler = new CustomHandler(plugin);
		homeHelper = new HomeHelper(plugin);
		homeHandler = new HomeHandler(plugin);
		savePointHelper = new SavePointHelper(plugin);
		savePointHandler = new SavePointHandler(plugin);
		teleportHelper = new TeleportHelper(plugin);
		teleportHandler = new TeleportHandler(plugin);
		warpHelper = new WarpHelper(plugin);
		warpHandler = new WarpHandler(plugin);
		
		//backgroundTask = new BackgroundTask(this);
		setupLogHandler();
		setupEconomy();
		setupCommandTree();
		ListenerSetup();
		setupBstats();
		plugin.getUtility().setTpaPlayersTabCompleter();
	}
	
	public void onDisable()
	{
		Bukkit.getScheduler().cancelTasks(this);
		homes.clear();
		warps.clear();
		HandlerList.unregisterAll(this);
		if (yamlHandler.getConfig().getBoolean("Mysql.Status", false) == true)
		{
			if (mysqlSetup.getConnection() != null) 
			{
				mysqlSetup.closeConnection();
			}
		}
		log.info(pluginName + " is disabled!");
	}

	public static BungeeTeleportManager getPlugin()
	{
		return plugin;
	}
	
	public YamlHandler getYamlHandler() 
	{
		return yamlHandler;
	}
	
	public YamlManager getYamlManager()
	{
		return yamlManager;
	}

	public void setYamlManager(YamlManager yamlManager)
	{
		BungeeTeleportManager.yamlManager = yamlManager;
	}
	
	public MysqlSetup getMysqlSetup() 
	{
		return mysqlSetup;
	}
	
	public MysqlHandler getMysqlHandler()
	{
		return mysqlHandler;
	}
	
	public Utility getUtility()
	{
		return utility;
	}
	
	/*public static BackgroundTask getBackgroundTask()
	{
		return backgroundTask;
	}*/

	public CommandHelper getCommandHelper()
	{
		return commandHelper;
	}
	
	private void setupCommandTree()
	{
		setupBypassPerm();
		//Zuletzt infoCommand deklarieren
		infoCommand += plugin.getYamlHandler().getCom().getString("BTM.Name");
		CommandConstructor btm = new CommandConstructor(plugin, "btm", false);
		
		registerCommand(btm.getPath(), btm.getName());
		getCommand(btm.getName()).setExecutor(new BTMCommandExecutor(plugin, btm));
		getCommand(btm.getName()).setTabCompleter(new TABCompletionTwo(plugin));
		BTMSettings.settings.addCommands(KeyHandler.BTM, btm.getCommandString());
		
		addingHelps(btm);
		
		if(BTMSettings.settings.isBack())
		{
			CommandConstructor back = new CommandConstructor(plugin, "back", false);
			
			registerCommand(back.getPath(), back.getName());
			getCommand(back.getName()).setExecutor(new BackCommandExecutor(plugin, back));
			getCommand(back.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			addingHelps(back);
		}
		
		if(BTMSettings.settings.isDeathback())
		{
			CommandConstructor deathback = new CommandConstructor(plugin, "deathback", false);
			
			registerCommand(deathback.getPath(), deathback.getName());
			getCommand(deathback.getName()).setExecutor(new BackCommandExecutor(plugin, deathback));
			getCommand(deathback.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			addingHelps(deathback);
		}
		
		if(BTMSettings.settings.isHome())
		{
			CommandConstructor sethome = new CommandConstructor(plugin, "sethome", false);
			
			registerCommand(sethome.getPath(), sethome.getName());
			getCommand(sethome.getName()).setExecutor(new HomeCommandExecutor(plugin, sethome));
			getCommand(sethome.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.HOME_SET, sethome.getCommandString());
			
			CommandConstructor homecreate = new CommandConstructor(plugin, "homecreate", false);
			
			registerCommand(homecreate.getPath(), homecreate.getName());
			getCommand(homecreate.getName()).setExecutor(new HomeCommandExecutor(plugin, homecreate));
			getCommand(homecreate.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.HOME_CREATE, homecreate.getCommandString());
			
			CommandConstructor delhome = new CommandConstructor(plugin, "delhome", false);
			
			registerCommand(delhome.getPath(), delhome.getName());
			getCommand(delhome.getName()).setExecutor(new HomeCommandExecutor(plugin, delhome));
			getCommand(delhome.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.HOME_DEL, delhome.getCommandString());
			
			CommandConstructor homeremove = new CommandConstructor(plugin, "homeremove", false);
			
			registerCommand(homeremove.getPath(), homeremove.getName());
			getCommand(homeremove.getName()).setExecutor(new HomeCommandExecutor(plugin, homeremove));
			getCommand(homeremove.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.HOME_REMOVE, homeremove.getCommandString());
			
			CommandConstructor homesdeleteserverworld = new CommandConstructor(plugin, "homesdeleteserverworld", false);
			
			registerCommand(homesdeleteserverworld.getPath(), homesdeleteserverworld.getName());
			getCommand(homesdeleteserverworld.getName()).setExecutor(new HomeCommandExecutor(plugin, homesdeleteserverworld));
			getCommand(homesdeleteserverworld.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			CommandConstructor home = new CommandConstructor(plugin, "home", false);
			
			registerCommand(home.getPath(), home.getName());
			getCommand(home.getName()).setExecutor(new HomeCommandExecutor(plugin, home));
			getCommand(home.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.HOME, home.getCommandString());
			
			CommandConstructor homes = new CommandConstructor(plugin, "homes", false);
			
			registerCommand(homes.getPath(), homes.getName());
			getCommand(homes.getName()).setExecutor(new HomeCommandExecutor(plugin, homes));
			getCommand(homes.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.HOMES, homes.getCommandString());
			
			CommandConstructor homelist = new CommandConstructor(plugin, "homelist", false);
			
			registerCommand(homelist.getPath(), homelist.getName());
			getCommand(homelist.getName()).setExecutor(new HomeCommandExecutor(plugin, homelist));
			getCommand(homelist.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			addingHelps(sethome, delhome, homecreate, homeremove, homesdeleteserverworld, home, homes, homelist);
		}
		
		if(BTMSettings.settings.isPortal())
		{
			
		}
		
		if(BTMSettings.settings.isRespawnPoint())
		{
			
		}
		
		if(BTMSettings.settings.isSavePoint())
		{
			// ADDME
		}
		
		if(BTMSettings.settings.isTeleport())
		{
			CommandConstructor tpaccept = new CommandConstructor(plugin, "tpaccept", false);
			
			registerCommand(tpaccept.getPath(), tpaccept.getName());
			getCommand(tpaccept.getName()).setExecutor(new TpCommandExecutor(plugin, tpaccept));
			getCommand(tpaccept.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.TPACCEPT, tpaccept.getCommandString());
			
			CommandConstructor tpdeny = new CommandConstructor(plugin, "tpadeny", false);
			
			registerCommand(tpdeny.getPath(), tpdeny.getName());
			getCommand(tpdeny.getName()).setExecutor(new TpCommandExecutor(plugin, tpdeny));
			getCommand(tpdeny.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.TPDENY, tpdeny.getCommandString());
			
			CommandConstructor tpquit = new CommandConstructor(plugin, "tpquit", false);
			
			registerCommand(tpquit.getPath(), tpquit.getName());
			getCommand(tpquit.getName()).setExecutor(new TpCommandExecutor(plugin, tpaccept));
			getCommand(tpquit.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			CommandConstructor tpatoggle = new CommandConstructor(plugin, "tpatoggle", false);
			
			registerCommand(tpatoggle.getPath(), tpatoggle.getName());
			getCommand(tpatoggle.getName()).setExecutor(new TpCommandExecutor(plugin, tpatoggle));
			getCommand(tpatoggle.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			CommandConstructor tpaignore = new CommandConstructor(plugin, "tpaignore", false);
			
			registerCommand(tpaignore.getPath(), tpaignore.getName());
			getCommand(tpaignore.getName()).setExecutor(new TpCommandExecutor(plugin, tpaignore));
			getCommand(tpaignore.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.TPAIGNORE, tpaignore.getCommandString());
			
			CommandConstructor tpaignorelist = new CommandConstructor(plugin, "tpaignorelist", false);
			
			registerCommand(tpaignorelist.getPath(), tpaccept.getName());
			getCommand(tpaignorelist.getName()).setExecutor(new TpCommandExecutor(plugin, tpaignorelist));
			getCommand(tpaignorelist.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			CommandConstructor tpa = new CommandConstructor(plugin, "tpa", false);
			
			registerCommand(tpa.getPath(), tpa.getName());
			getCommand(tpa.getName()).setExecutor(new TpCommandExecutor(plugin, tpa));
			getCommand(tpa.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.TPA, tpa.getCommandString());
			
			CommandConstructor tpahere = new CommandConstructor(plugin, "tpahere", false);
			
			registerCommand(tpahere.getPath(), tpahere.getName());
			getCommand(tpahere.getName()).setExecutor(new TpCommandExecutor(plugin, tpahere));
			getCommand(tpahere.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.TPAHERE, tpahere.getCommandString());
			
			CommandConstructor tp = new CommandConstructor(plugin, "tp", false);
			
			registerCommand(tp.getPath(), tp.getName());
			getCommand(tp.getName()).setExecutor(new TpCommandExecutor(plugin, tp));
			getCommand(tp.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.TP, tp.getCommandString());
			
			CommandConstructor tphere = new CommandConstructor(plugin, "tphere", false);
			
			registerCommand(tphere.getPath(), tphere.getName());
			getCommand(tphere.getName()).setExecutor(new TpCommandExecutor(plugin, tphere));
			getCommand(tphere.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.TPHERE, tphere.getCommandString());
			
			CommandConstructor tpall = new CommandConstructor(plugin, "tpall", false);
			
			registerCommand(tpall.getPath(), tpall.getName());
			getCommand(tpall.getName()).setExecutor(new TpCommandExecutor(plugin, tpall));
			getCommand(tpall.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			CommandConstructor tppos = new CommandConstructor(plugin, "tppos", false);
			
			registerCommand(tppos.getPath(), tppos.getName());
			getCommand(tppos.getName()).setExecutor(new TpCommandExecutor(plugin, tppos));
			getCommand(tppos.getName()).setTabCompleter(new TABCompletionOne(plugin));
			
			addingHelps(tpaccept, tpdeny, tpa, tpahere, tpquit, tpatoggle, tpaignore, tpaignorelist,
					tp, tphere, tpall, tppos);
		}
		
		if(BTMSettings.settings.isWarp())
		{
			CommandConstructor warpcreate = new CommandConstructor(plugin, "warpcreate", false);
			
			registerCommand(warpcreate.getPath(), warpcreate.getName());
			getCommand(warpcreate.getName()).setExecutor(new WarpCommandExecutor(plugin, warpcreate));
			getCommand(warpcreate.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_CREATE, warpcreate.getCommandString());
			
			CommandConstructor warpremove = new CommandConstructor(plugin, "warpremove", false);
			
			registerCommand(warpremove.getPath(), warpremove.getName());
			getCommand(warpremove.getName()).setExecutor(new WarpCommandExecutor(plugin, warpremove));
			getCommand(warpremove.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_REMOVE, warpremove.getCommandString());
			
			CommandConstructor warplist = new CommandConstructor(plugin, "warplist", false);
			
			registerCommand(warplist.getPath(), warplist.getName());
			getCommand(warplist.getName()).setExecutor(new WarpCommandExecutor(plugin, warplist));
			getCommand(warplist.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_LIST, warplist.getCommandString());
			
			CommandConstructor warp = new CommandConstructor(plugin, "warp", false);
			
			registerCommand(warp.getPath(), warp.getName());
			getCommand(warp.getName()).setExecutor(new WarpCommandExecutor(plugin, warp));
			getCommand(warp.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP, warp.getCommandString());
			
			CommandConstructor warps = new CommandConstructor(plugin, "warps", false);
			
			registerCommand(warps.getPath(), warps.getName());
			getCommand(warps.getName()).setExecutor(new WarpCommandExecutor(plugin, warps));
			getCommand(warps.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARPS, warps.getCommandString());
			
			CommandConstructor warpinfo = new CommandConstructor(plugin, "warpinfo", false);
			
			registerCommand(warpinfo.getPath(), warpinfo.getName());
			getCommand(warpinfo.getName()).setExecutor(new WarpCommandExecutor(plugin, warpinfo));
			getCommand(warpinfo.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_INFO, warpinfo.getCommandString());
			
			CommandConstructor warpsetname = new CommandConstructor(plugin, "warpsetname", false);
			
			registerCommand(warpsetname.getPath(), warpsetname.getName());
			getCommand(warpsetname.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsetname));
			getCommand(warpsetname.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_SETNAME, warpsetname.getCommandString());
			
			CommandConstructor warpsetposition = new CommandConstructor(plugin, "warpsetposition", false);
			
			registerCommand(warpsetposition.getPath(), warpsetposition.getName());
			getCommand(warpsetposition.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsetposition));
			getCommand(warpsetposition.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_SETPOSITION, warpsetposition.getCommandString());
			
			CommandConstructor warpsetowner = new CommandConstructor(plugin, "warpsetowner", false);
			
			registerCommand(warpsetowner.getPath(), warpsetowner.getName());
			getCommand(warpsetowner.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsetowner));
			getCommand(warpsetowner.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_SETOWNER, warpsetowner.getCommandString());
			
			CommandConstructor warpsetpermission = new CommandConstructor(plugin, "warpsetpermission", false);
			
			registerCommand(warpsetpermission.getPath(), warpsetpermission.getName());
			getCommand(warpsetpermission.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsetpermission));
			getCommand(warpsetpermission.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_SETPERMISSION, warpsetpermission.getCommandString());
			
			CommandConstructor warpsetpassword = new CommandConstructor(plugin, "warpsetpassword", false);
			
			registerCommand(warpsetpassword.getPath(), warpsetpassword.getName());
			getCommand(warpsetpassword.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsetpassword));
			getCommand(warpsetpassword.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_SETPASSWORD, warpsetpassword.getCommandString());
			
			CommandConstructor warpsetprice = new CommandConstructor(plugin, "warpsetprice", false);
			
			registerCommand(warpsetprice.getPath(), warpsetprice.getName());
			getCommand(warpsetprice.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsetprice));
			getCommand(warpsetprice.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_SETPRICE, warpsetprice.getCommandString());
			
			CommandConstructor warphidden = new CommandConstructor(plugin, "warphidden", false);
			
			registerCommand(warphidden.getPath(), warphidden.getName());
			getCommand(warphidden.getName()).setExecutor(new WarpCommandExecutor(plugin, warphidden));
			getCommand(warphidden.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_HIDDEN, warphidden.getCommandString());
			
			CommandConstructor warpaddmember = new CommandConstructor(plugin, "warpaddmember", false);
			
			registerCommand(warpaddmember.getPath(), warpaddmember.getName());
			getCommand(warpaddmember.getName()).setExecutor(new WarpCommandExecutor(plugin, warpaddmember));
			getCommand(warpaddmember.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_ADDMEMBER, warpaddmember.getCommandString());
			
			CommandConstructor warpremovemember = new CommandConstructor(plugin, "warpremovemember", false);
			
			registerCommand(warpremovemember.getPath(), warpremovemember.getName());
			getCommand(warpremovemember.getName()).setExecutor(new WarpCommandExecutor(plugin, warpremovemember));
			getCommand(warpremovemember.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_REMOVEMEMBER, warpremovemember.getCommandString());
			
			CommandConstructor warpaddblacklist = new CommandConstructor(plugin, "warpaddblacklist", false);
			
			registerCommand(warpaddblacklist.getPath(), warpaddblacklist.getName());
			getCommand(warpaddblacklist.getName()).setExecutor(new WarpCommandExecutor(plugin, warpaddblacklist));
			getCommand(warpaddblacklist.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_ADDBLACKLIST, warpaddblacklist.getCommandString());
			
			CommandConstructor warpremoveblacklist = new CommandConstructor(plugin, "warpremoveblacklist", false);
			
			registerCommand(warpremoveblacklist.getPath(), warpremoveblacklist.getName());
			getCommand(warpremoveblacklist.getName()).setExecutor(new WarpCommandExecutor(plugin, warpremoveblacklist));
			getCommand(warpremoveblacklist.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_REMOVEBLACKLIST, warpremoveblacklist.getCommandString());
			
			CommandConstructor warpsetcategory = new CommandConstructor(plugin, "warpsetcategory", false);
			
			registerCommand(warpsetcategory.getPath(), warpsetcategory.getName());
			getCommand(warpsetcategory.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsetcategory));
			getCommand(warpsetcategory.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_SETCATEGORY, warpsetcategory.getCommandString());
			
			CommandConstructor warpsdeleteserverworld = new CommandConstructor(plugin, "warpsdeleteserverworld", false);
			
			registerCommand(warpsdeleteserverworld.getPath(), warpsdeleteserverworld.getName());
			getCommand(warpsdeleteserverworld.getName()).setExecutor(new WarpCommandExecutor(plugin, warpsdeleteserverworld));
			getCommand(warpsdeleteserverworld.getName()).setTabCompleter(new TABCompletionOne(plugin));
			BTMSettings.settings.addCommands(KeyHandler.WARP_DELETESERVERWORLD, warpsdeleteserverworld.getCommandString());
			
			addingHelps(warp, warps, warpcreate, warpremove, warplist, warpinfo, warpsetname, warpsetowner, warpsetposition,
					warpsetpassword, warpsetprice, warphidden, warpaddmember, warpremovemember, warpaddblacklist, warpremoveblacklist,
					warpsetcategory, warpsdeleteserverworld);
		}
		
		/*
		 	CommandConstructor  = new CommandConstructor(plugin, "", false);
			
			registerCommand(.getPath(), .getName());
			getCommand(.getName()).setExecutor(new TpCommandExecutor(plugin, ));
			getCommand(.getName()).setTabCompleter(new TABCompletionOne(plugin));
		 */
	}
	
	public void setupBypassPerm()
	{
		String path = "Bypass.";
		StaticValues.PERM_BYPASS_BACK_COST = yamlHandler.getCom().getString(path+"Back.Cost");
		StaticValues.PERM_BYPASS_BACK_DELAY = yamlHandler.getCom().getString(path+"Back.Delay");
		
		StaticValues.PERM_BYPASS_CUSTOM_DELAY = yamlHandler.getCom().getString(path+"Custom.Delay");
		
		StaticValues.PERM_HOME_OTHER = yamlHandler.getCom().getString(path+"Home.Other");
		StaticValues.PERM_HOMES_OTHER = yamlHandler.getCom().getString(path+"Home.HomesOther");
		StaticValues.PERM_BYPASS_HOME = yamlHandler.getCom().getString(path+"Home.Admin");
		StaticValues.PERM_BYPASS_HOME_COST = yamlHandler.getCom().getString(path+"Home.Cost");
		StaticValues.PERM_BYPASS_HOME_DELAY = yamlHandler.getCom().getString(path+"Home.Delay");
		StaticValues.PERM_BYPASS_HOME_FORBIDDEN = yamlHandler.getCom().getString(path+"Home.Forbidden");
		StaticValues.PERM_BYPASS_HOME_TOOMANY = yamlHandler.getCom().getString(path+"Home.Toomany");
		StaticValues.PERM_HOME_COUNTHOMES_WORLD = yamlHandler.getCom().getString(path+"Home.Count.World");
		StaticValues.PERM_HOME_COUNTHOMES_SERVER = yamlHandler.getCom().getString(path+"Home.Count.Server");
		StaticValues.PERM_HOME_COUNTHOMES_GLOBAL = yamlHandler.getCom().getString(path+"Home.Count.Global");
		
		StaticValues.PERM_BYPASS_TELEPORT_COST = yamlHandler.getCom().getString(path+"Tp.Cost");
		StaticValues.PERM_BYPASS_TELEPORT_DELAY = yamlHandler.getCom().getString(path+"Tp.Delay");
		StaticValues.PERM_BYPASS_TELEPORT_FORBIDDEN = yamlHandler.getCom().getString(path+"Tp.Forbidden");
		StaticValues.PERM_BYPASS_TELEPORT_SILENT = yamlHandler.getCom().getString(path+"Tp.Silent");
		StaticValues.PERM_BYPASS_TELEPORT_TPATOGGLE = yamlHandler.getCom().getString(path+"Tp.Tpatoggle");
		
		StaticValues.PERM_WARP_OTHER = yamlHandler.getCom().getString(path+"Warp.Other");
		StaticValues.PERM_WARPS_OTHER = yamlHandler.getCom().getString(path+"Warp.WarpsOther");
		StaticValues.PERM_BYPASS_WARP = yamlHandler.getCom().getString(path+"Warp.Admin");
		StaticValues.PERM_BYPASS_WARP_BLACKLIST = yamlHandler.getCom().getString(path+"Warp.Blacklist");
		StaticValues.PERM_BYPASS_WARP_COST = yamlHandler.getCom().getString(path+"Warp.Cost");
		StaticValues.PERM_BYPASS_WARP_DELAY = yamlHandler.getCom().getString(path+"Warp.Delay");
		StaticValues.PERM_BYPASS_WARP_FORBIDDEN = yamlHandler.getCom().getString(path+"Warp.Forbidden");
		StaticValues.PERM_BYPASS_WARP_TOOMANY = yamlHandler.getCom().getString(path+"Warp.Toomany");
		StaticValues.PERM_WARP_COUNTWARPS_WORLD = yamlHandler.getCom().getString(path+"Warp.Count.World");
		StaticValues.PERM_WARP_COUNTWARPS_SERVER = yamlHandler.getCom().getString(path+"Warp.Count.Server");
		StaticValues.PERM_WARP_COUNTWARPS_GLOBAL = yamlHandler.getCom().getString(path+"Warp.Count.Global");
		
		//StaticValues.PERM_ = yamlHandler.getCom().getString(path+"");
	}
	
	public ArrayList<BaseConstructor> getHelpList()
	{
		return helpList;
	}
	
	public void addingHelps(BaseConstructor... objects)
	{
		for(BaseConstructor bc : objects)
		{
			helpList.add(bc);
		}
	}
	
	public ArrayList<CommandConstructor> getCommandTree()
	{
		return commandTree;
	}
	
	public CommandConstructor getCommandFromPath(String commandpath)
	{
		CommandConstructor cc = null;
		for(CommandConstructor coco : getCommandTree())
		{
			if(coco.getPath().equalsIgnoreCase(commandpath))
			{
				cc = coco;
				break;
			}
		}
		return cc;
	}
	
	public CommandConstructor getCommandFromCommandString(String command)
	{
		CommandConstructor cc = null;
		for(CommandConstructor coco : getCommandTree())
		{
			if(coco.getName().equalsIgnoreCase(command))
			{
				cc = coco;
				break;
			}
		}
		return cc;
	}

	public LinkedHashMap<String, ArgumentModule> getArgumentMap()
	{
		return argumentMap;
	}
	
	public void registerCommand(String... aliases) 
	{
		PluginCommand command = getCommand(aliases[0], plugin);
	 
		command.setAliases(Arrays.asList(aliases));
		getCommandMap().register(plugin.getDescription().getName(), command);
	}
	 
	private static PluginCommand getCommand(String name, BungeeTeleportManager plugin) 
	{
		PluginCommand command = null;
	 
		try {
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
	 
			command = c.newInstance(name, plugin);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	 
		return command;
	}
	 
	private static CommandMap getCommandMap() 
	{
		CommandMap commandMap = null;
	 
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) 
			{
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
	 
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	 
		return commandMap;
	}
	
	public void ListenerSetup()
	{
		PluginManager pm = getServer().getPluginManager();
		getServer().getMessenger().registerOutgoingPluginChannel(this, StaticValues.GENERAL_TOBUNGEE);
		getServer().getMessenger().registerOutgoingPluginChannel(this, StaticValues.BACK_TOBUNGEE);
		getServer().getMessenger().registerIncomingPluginChannel(this, StaticValues.BACK_TOSPIGOT, new BackMessageListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, StaticValues.CUSTOM_TOBUNGEE);
		getServer().getMessenger().registerIncomingPluginChannel(this, StaticValues.CUSTOM_TOSPIGOT, new CustomMessageListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, StaticValues.HOME_TOBUNGEE);
		getServer().getMessenger().registerIncomingPluginChannel(this, StaticValues.HOME_TOSPIGOT, new HomeMessageListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, StaticValues.SAVEPOINT_TOBUNGEE);
		getServer().getMessenger().registerIncomingPluginChannel(this, StaticValues.SAVEPOINT_TOSPIGOT, new SavePointMessageListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, StaticValues.TP_TOBUNGEE);
		getServer().getMessenger().registerIncomingPluginChannel(this, StaticValues.TP_TOSPIGOT, new TeleportMessageListener(this));
		getServer().getMessenger().registerOutgoingPluginChannel(this, StaticValues.WARP_TOBUNGEE);
		getServer().getMessenger().registerIncomingPluginChannel(this, StaticValues.WARP_TOSPIGOT, new WarpMessageListener(this));
		pm.registerEvents(new BackListener(plugin), plugin);
		pm.registerEvents(new ServerAndWordListener(plugin), plugin);
		pm.registerEvents(new CustomTeleportListener(plugin), plugin);
		pm.registerEvents(new PlayerOnCooldownListener(plugin), plugin);
	}
	
	public boolean reload() throws IOException
	{
		if(!yamlHandler.loadYamlHandler())
		{
			return false;
		}
		if(yamlHandler.getConfig().getBoolean("Mysql.Status", false))
		{
			mysqlSetup.closeConnection();
			if(!mysqlHandler.loadMysqlHandler())
			{
				return false;
			}
			if(!mysqlSetup.loadMysqlSetup())
			{
				return false;
			}
		} else
		{
			return false;
		}
		return true;
	}

	private boolean setupEconomy()
    {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) 
        {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) 
        {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }
	
	public Economy getEco()
	{
		return this.eco;
	}
	
	public void setupBstats()
	{
		int pluginId = 7692;
        new Metrics(this, pluginId);
	}
	
	public boolean existHook(String externPluginName)
	{
		if(plugin.getServer().getPluginManager().getPlugin(externPluginName) == null)
		{
			return false;
		}
		return true;
	}
	
	private void setupLogHandler()
	{
		if(existHook("AdvancedEconomy") || existHook("AdvanceEconomyPlus"))
		{
			advanceEconomyHandler = new AdvanceEconomyHandler(plugin, false);
		} else
		{
			advanceEconomyHandler = null;
		}
	}
	
	public AdvanceEconomyHandler getAdvanceEconomyHandler()
	{
		return advanceEconomyHandler;
	}

	public BungeeBridge getBungeeBridge()
	{
		return bungeeBridge;
	}

	public BackHelper getBackHelper()
	{
		return backHelper;
	}
	
	public BackHandler getBackHandler()
	{
		return backHandler;
	}
	
	public CustomHandler getCustomHandler()
	{
		return customHandler;
	}

	public TeleportHelper getTeleportHelper()
	{
		return teleportHelper;
	}
	
	public TeleportHandler getTeleportHandler()
	{
		return teleportHandler;
	}

	public WarpHandler getWarpHandler()
	{
		return warpHandler;
	}

	public WarpHelper getWarpHelper()
	{
		return warpHelper;
	}

	public HomeHandler getHomeHandler()
	{
		return homeHandler;
	}

	public HomeHelper getHomeHelper()
	{
		return homeHelper;
	}

	public ArrayList<String> getMysqlPlayers()
	{
		return players;
	}

	public void setMysqlPlayers(ArrayList<String> players)
	{
		this.players = players;
	}

	public GeneralHandler getGeneralHandler()
	{
		return generalHandler;
	}

	public SavePointHandler getSavePointHandler()
	{
		return savePointHandler;
	}

	public SavePointHelper getSavePointHelper()
	{
		return savePointHelper;
	}
}