package net.skycraftmc.Itemizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ItemizerPlugin extends JavaPlugin
{
	public static final int NAME = 0;
	public static final int LORE = 1;
	public static final int SKULL_OWNER = 2;
	public static final int TITLE = 3;
	public static final int AUTHOR = 4;
	public static final int PAGES = 5;
	public static final int COLOR = 6;
	private CmdDesc[] help = {
		new CmdDesc("/itemizer help", "Shows this menu", null),
		new CmdDesc("/itemizer name <name>", "Names your item", "itemizer.name"),
		new CmdDesc("/itemizer lore <lore>", "Sets the lore of your item", "itemizer.lore"),
		new CmdDesc("/itemizer advlore", "Advanced lore editing commands", "itemizer.lore"),
		new CmdDesc("/itemizer potion", "Potion editing commands", "itemizer.potion"),
		new CmdDesc("/itemizer title <title>", "Titles your book", "itemizer.title"),
		new CmdDesc("/itemizer author <name>", "Sets the author of your book", "itemizer.author"),
		new CmdDesc("/itemizer head <name>", "Sets the player of your head", "itemizer.head"),
		new CmdDesc("/itemizer clearall", "Clears all metadata your item", "itemizer.clear"),
		new CmdDesc("/itemizer clear <types...>", "Clears specific metadata from your item", "itemizer.clear"),
	};
	private CmdDesc[] advlorehelp = {
		new CmdDesc("/itemizer advlore help", "Shows this menu", null),
		new CmdDesc("/itemizer advlore add <lore>", "Adds a line of lore", "itemizer.lore"),
		new CmdDesc("/itemizer advlore remove <index>", "Removes the line of lore", "itemizer.lore"),
		new CmdDesc("/itemizer advlore change <index> <text>", "Changes a line of lore", "itemizer.lore")
	};
	private CmdDesc[] potionhelp = {
		new CmdDesc("/itemizer potion help", "Shows this menu", null),
		new CmdDesc("/itemizer potion add <effect> [level] <seconds>", "Adds the potion effect", "itemizer.potion"),
		new CmdDesc("/itemizer potion remove <effect>", "Removes the potion effect", "itemizer.potion"),
		new CmdDesc("/itemizer potion list", "Lists all potion effects", "itemizer.potion")
	};
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if(args.length >= 1)
		{
			if(args[0].equalsIgnoreCase("name"))renameCmd(sender, args, false);
			else if(args[0].equalsIgnoreCase("lore"))renameCmd(sender, args, true);
			else if(args[0].equalsIgnoreCase("help"))helpCmd(sender, args, help, "Itemizer Help");
			else if(args[0].equalsIgnoreCase("clear"))clearCmd(sender, args);
			else if(args[0].equalsIgnoreCase("clearall"))clearAllCmd(sender, args);
			else if(args[0].equalsIgnoreCase("title"))bookCmd(sender, args, false);
			else if(args[0].equalsIgnoreCase("author"))bookCmd(sender, args, true);
			else if(args[0].equalsIgnoreCase("head"))headCmd(sender, args);
			else if(args[0].equalsIgnoreCase("advlore"))advLoreCmd(sender, args);
			else if(args[0].equalsIgnoreCase("potion"))potionCmd(sender, args);
			else return msg(sender, ChatColor.GOLD + "Command unrecognized.  Type " + ChatColor.AQUA + "/itemizer help" + ChatColor.GOLD + " for help");
		}
		else
		{
			sender.sendMessage(ChatColor.GOLD + "Itemizer version " + ChatColor.AQUA + getDescription().getVersion() + ChatColor.GOLD + " by " + ChatColor.AQUA + "Technius");
			sender.sendMessage(ChatColor.GOLD + "Type " + ChatColor.AQUA + "/itemizer help" + ChatColor.GOLD + " for help");
		}
		return true;
	}
	public boolean helpCmd(CommandSender sender, String[] args, CmdDesc[] help, String title)
	{
		int page = 1;
		if(args.length == 2)
		{
			try{page = Integer.parseInt(args[1]);}catch(NumberFormatException nfe){return msg(sender, ChatColor.RED + "\"" + args[1] + "\" is not a valid number");}
		}
		ArrayList<String>d = new ArrayList<String>();
		int max = 1;
		int cmda = 0;
		for(int i = 0; i < help.length; i ++)
		{
			CmdDesc c = help[i];
			if(c.getPerm() != null)
			{
				if(!sender.hasPermission(c.getPerm()))continue;
			}
			if(d.size() < 10)
			{
				if(i >= (page - 1)*10 && i <= ((page - 1)*10) + 9)d.add(c.asDef());
			}
			if(cmda > 10 && cmda % 10 == 1)max ++;
			cmda ++;
		}
		sender.sendMessage(ChatColor.GOLD + title + "(" + ChatColor.AQUA + page + ChatColor.GOLD + "/" + ChatColor.AQUA + max + ChatColor.GOLD + ")");
		for(String s:d)sender.sendMessage(s);
		return true;
	}
	public boolean renameCmd(CommandSender sender, String[] args, boolean lore)
	{
		String a = (lore ? "lore" : "name");
		if(noPerm(sender, "itemizer." + a))return true;
		if(noConsole(sender))return true;
		if(args.length <= 1)return usage(sender, "itemizer " + a);
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		if(item == null)return msg(sender, ChatColor.RED + "You need to hold an item in your hand!");
		String name = "";
		for(int i = 1; i < args.length; i ++)
		{
			if(name.isEmpty())name = name + args[i];
			else name = name + " " + args[i];
		}
		displayAction(item, col(name), (lore ? LORE : NAME));
		sender.sendMessage(ChatColor.GREEN + "The " + a + " of the item in your hand has been set to \"" + name + "\"!");
		return true;
	}
	public boolean clearAllCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.clear"))return true;
		if(noConsole(sender))return true;
		if(args.length != 1)return usage(sender, "itemizer clearall");
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		if(item == null)return msg(sender, ChatColor.RED + "You need to hold an item in your hand!");
		clearAllData(item);
		player.sendMessage(ChatColor.GREEN + "Item metadata cleared!");
		return true;
	}
	public boolean clearCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.clear"))return true;
		if(noConsole(sender))return true;
		if(args.length < 1)return usage(sender, "itemizer clear <types...>");
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		if(item == null)return msg(sender, ChatColor.RED + "You need to hold an item in your hand!");
		String[] p = new String[args.length - 1];
		ArrayList<Integer>params = new ArrayList<Integer>();
		for(int i = 1; i < args.length; i ++)
		{
			String s = args[i];
			if(s.equalsIgnoreCase("name"))
			{
				if(!params.contains(NAME))params.add(NAME);
			}
			else if(s.equalsIgnoreCase("lore"))
			{
				if(!params.contains(LORE))params.add(LORE);
			}
			else if(s.equalsIgnoreCase("head"))
			{
				if(!params.contains(SKULL_OWNER))params.add(SKULL_OWNER);
			}
			else if(s.equalsIgnoreCase("author"))
			{
				if(!params.contains(AUTHOR))params.add(AUTHOR);
			}
			else if(s.equalsIgnoreCase("color"))
			{
				if(!params.contains(COLOR))params.add(COLOR);
			}
			else if(s.equalsIgnoreCase("title"))
			{
				if(!params.contains(TITLE))params.add(TITLE);
			}
			else return msg(sender, ChatColor.RED + "Unknown type: " + s);
			p[i - 1] = s;
		}
		if(params.isEmpty())return msg(sender, ChatColor.RED + "You have not specified any metadata!");
		Integer[] ptemp = params.toArray(new Integer[params.size()]);
		int[] pfinal = new int[ptemp.length];
		for(int i = 0; i < ptemp.length; i ++)pfinal[i] = ptemp[i];
		clearData(item, pfinal);
		sender.sendMessage(ChatColor.GREEN + "The specified metadata has been cleared.");
		return true;
	}
	public boolean bookCmd(CommandSender sender, String[] args, boolean author)
	{
		String a = (author ? "author" : "title");
		if(noPerm(sender, "itemizer." + a))return true;
		if(noConsole(sender))return true;
		if(args.length <= 1)return usage(sender, "itemizer " + a + "<" + a + ">");
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		boolean r = false;
		if(item == null)r = true;
		if(item.getType() != Material.WRITTEN_BOOK)r = true;
		if(r)return msg(sender, ChatColor.RED + "You need to hold a signed book in your hand!");
		String name = "";
		for(int i = 1; i < args.length; i ++)
		{
			if(name.isEmpty())name = name + args[i];
			else name = name + " " + args[i];
		}
		name = col(name);
		BookMeta bm = (BookMeta)item.getItemMeta();
		if(author)bm.setAuthor(name);
		else bm.setTitle(name);
		item.setItemMeta(bm);
		sender.sendMessage(ChatColor.GREEN + "The " + a + " of the item in your hand has been set to \"" + name + "\"!");
		return true;
	}
	public boolean headCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.head"))return true;
		if(noConsole(sender))return true;
		if(args.length != 2)return usage(sender, "itemizer head <name>");
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		if(item.getType() != Material.SKULL_ITEM || item.getDurability() != 3)return msg(sender, ChatColor.RED + "The item in your hand must be a player head!");
		SkullMeta sm = (SkullMeta)item.getItemMeta();
		sm.setOwner(args[1]);
		item.setItemMeta(sm);
		sender.sendMessage(ChatColor.GREEN + "The player of the head in your hand has been set to \"" + args[1] + "\"!");
		return true;
	}
	public boolean potionCmd(CommandSender sender, String[] args2)
	{
		String[] args = bumpArgs(args2);
		if(args.length >= 1)
		{
			if(args[0].equalsIgnoreCase("help"))helpCmd(sender, args, potionhelp, "Potion Help");
			else if(args[0].equalsIgnoreCase("add"))potionAddCmd(sender, args);
			else if(args[0].equalsIgnoreCase("remove"))potionRemoveCmd(sender, args);
			else if(args[0].equalsIgnoreCase("list"))potionListCmd(sender, args);
			else return msg(sender, ChatColor.GOLD + "Command unrecognized.  Type " + ChatColor.AQUA + "/itemizer potion help" + ChatColor.GOLD + " for help");
		}
		else helpCmd(sender, args, potionhelp, "Potion Help");
		return true;
	}
	public boolean potionAddCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.potion"))return true;
		if(noConsole(sender))return true;
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		boolean r = false;
		if(item == null)r = true;
		if(item.getType() != Material.POTION)r = true;
		if(r)return msg(sender, ChatColor.RED + "You need to hold a potion in your hand!");
		try
		{
			PotionEffect pot;
			if(args.length == 3)pot = parsePotionEffect(args[1], args[2], null);
			else if(args.length == 4)pot = parsePotionEffect(args[1], args[3], args[2]);
			else return usage(sender, "itemizer potion add <type> [level] <seconds>");
			PotionMeta pm = (PotionMeta)item.getItemMeta();
			if(pm.hasCustomEffect(pot.getType()))
				return msg(sender, ChatColor.RED + "This potion already has " + pot.getType().getName() + "!");
			pm.addCustomEffect(pot, false);
			player.sendMessage(ChatColor.GREEN + pot.getType().getName() + " added to the potion.");
			item.setItemMeta(pm);
			player.setItemInHand(item);
		}
		catch(IllegalArgumentException iae)
		{
			sender.sendMessage(ChatColor.RED + iae.getMessage());
		}
		return true;
	}
	public boolean potionRemoveCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.potion"))return true;
		if(noConsole(sender))return true;
		if(args.length != 2)return usage(sender, "itemizer potion remove <type>");
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		boolean r = false;
		if(item == null)r = true;
		if(item.getType() != Material.POTION)r = true;
		if(r)return msg(sender, ChatColor.RED + "You need to hold a potion in your hand!");
		PotionEffectType t = PotionEffectType.getByName(args[1].toUpperCase());
		if(t == null)return msg(sender, ChatColor.RED + "No such potion effect type: " + args[1]);
		PotionMeta pm = (PotionMeta)item.getItemMeta();
		if(!pm.hasCustomEffect(t))
			return msg(sender, ChatColor.RED + "This potion doesn't have " + t.getName() + "!");
		pm.removeCustomEffect(t);
		item.setItemMeta(pm);
		player.setItemInHand(item);
		player.sendMessage(ChatColor.GREEN + t.getName() + " removed from the potion");
		return true;
	}
	public boolean potionListCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.potion"))return true;
		StringBuilder sb = new StringBuilder();
		ArrayList<String>n = new ArrayList<String>();
		for(PotionEffectType e:PotionEffectType.values())
		{
			if(e != null)n.add(e.getName().toLowerCase());
		}
		Collections.sort(n);
		boolean f = true;
		for(String s:n)
		{
			if(f)
			{
				sb.append(s);
				f = false;
			}
			else sb.append(", " + s);
		}
		sender.sendMessage(sb.toString());
		return true;
	}
	public boolean advLoreCmd(CommandSender sender, String[] args2)
	{
		String[] args = bumpArgs(args2);
		if(args.length >= 1)
		{
			if(args[0].equalsIgnoreCase("help"))helpCmd(sender, args, advlorehelp, "Adv. Lore Help");
			else if(args[0].equalsIgnoreCase("add"))advLoreAddCmd(sender, args);
			else if(args[0].equalsIgnoreCase("remove"))advLoreRemoveCmd(sender, args);
			else if(args[0].equalsIgnoreCase("change"))advLoreChangeCmd(sender, args);
			else return msg(sender, ChatColor.GOLD + "Command unrecognized.  Type " + ChatColor.AQUA + "/itemizer advlore help" + ChatColor.GOLD + " for help");
		}
		else helpCmd(sender, args, advlorehelp, "Adv. Lore Help");
		return true;
	}
	public boolean advLoreAddCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.lore"))return true;
		if(noConsole(sender))return true;
		if(args.length <= 1)return usage(sender, "itemizer advlore add <lore>");
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		if(item == null)return msg(sender, ChatColor.RED + "You need to hold an item in your hand!");
		String name = "";
		for(int i = 1; i < args.length; i ++)
		{
			if(name.isEmpty())name = name + args[i];
			else name = name + " " + args[i];
		}
		ItemMeta im = item.getItemMeta();
		List<String>lore;
		if(im.hasLore())lore = im.getLore();
		else lore = new ArrayList<String>();
		String[] lines = name.split("\\\\n");
		for(int i = 0; i < lines.length; i ++)
			lore.add(ChatColor.translateAlternateColorCodes('&', lines[i]));
		im.setLore(lore);
		item.setItemMeta(im);
		player.setItemInHand(item);
		sender.sendMessage(ChatColor.GREEN + "Line" + (lines.length > 1 ? "s" : "") + " added to the item's lore!");
		return true;
	}
	public boolean advLoreRemoveCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.lore"))return true;
		if(noConsole(sender))return true;
		if(args.length != 2 && args.length != 1)return usage(sender, "itemizer advlore remove [index]");
		Player player = (Player)sender;
		ItemStack item = player.getItemInHand();
		int index = 1;
		if(args.length == 2)
		{
			try
			{
				index = Integer.parseInt(args[1]);
			}
			catch(NumberFormatException nfe)
			{
				return msg(sender, ChatColor.RED + "\"" + args[1] + "\" is not a valid number.");
			}
		}
		if(item == null)return msg(sender, ChatColor.RED + "You need to hold an item in your hand!");
		if(!item.hasItemMeta())return msg(sender, ChatColor.RED + "This item doesn't have lore!");
		ItemMeta im = item.getItemMeta();
		if(!im.hasLore())return msg(sender, ChatColor.RED + "This item doesn't have lore!");
		List<String>lore = im.getLore();
		int tindex = index - 1;
		if(tindex < 0 || tindex >= lore.size())return msg(sender, ChatColor.RED + "There is no lore at index " + index);
		lore.remove(tindex);
		im.setLore(lore);
		item.setItemMeta(im);
		player.setItemInHand(item);
		player.sendMessage(ChatColor.GREEN + "Lore at index " + index + " removed.");
		return true;
	}
	public boolean advLoreChangeCmd(CommandSender sender, String[] args)
	{
		if(noPerm(sender, "itemizer.lore"))return true;
		if(noConsole(sender))return true;
		if(args.length <= 2)return usage(sender, "itemizer advlore change <index> <lore>");
		Player player = (Player)sender;
		int index = 1;
		try
		{
			index = Integer.parseInt(args[1]);
		}
		catch(NumberFormatException nfe)
		{
			return msg(sender, ChatColor.RED + "\"" + args[1] + "\" is not a valid number.");
		}
		ItemStack item = player.getItemInHand();
		if(item == null)return msg(sender, ChatColor.RED + "You need to hold an item in your hand!");
		if(!item.hasItemMeta())return msg(sender, ChatColor.RED + "This item doesn't have lore!");
		ItemMeta im = item.getItemMeta();
		if(!im.hasLore())return msg(sender, ChatColor.RED + "This item doesn't have lore!");
		String name = "";
		for(int i = 2; i < args.length; i ++)
		{
			if(name.isEmpty())name = name + args[i];
			else name = name + " " + args[i];
		}
		List<String>lore = im.getLore();
		int tindex = index - 1;
		if(tindex < 0 || tindex >= lore.size())return msg(sender, ChatColor.RED + "There is no lore at index " + index);
		String[] lines = name.split("\\\\n");
		lore.set(tindex, lines[0]);
		for(int i = 1; i < lines.length; i ++)
			lore.add(tindex + i, ChatColor.translateAlternateColorCodes('&', lines[i]));
		im.setLore(lore);
		item.setItemMeta(im);
		player.setItemInHand(item);
		sender.sendMessage(ChatColor.GREEN + "Line" + (lines.length > 1 ? "s" : "") + " added to the item's lore!");
		return true;
	}
	private boolean msg(CommandSender sender, String msg)
	{
		sender.sendMessage(msg);
		return true;
	}
	private boolean noConsole(CommandSender sender)
	{
		if(sender instanceof Player)return false;
		sender.sendMessage(ChatColor.RED + "This command can only be used as an in-game player!");
		return true;
	}
	private boolean noPerm(CommandSender sender, String node)
	{
		if(sender.hasPermission(node))return false;
		sender.sendMessage(ChatColor.RED + "You are not allowed to use this command!");
		return true;
	}
	private boolean usage(CommandSender sender, String cmd)
	{
		return msg(sender, ChatColor.RED + "Usage: " + (sender instanceof Player ? "/" : "") + cmd);
	}
	private PotionEffect parsePotionEffect(String effect, String seconds, String level) throws IllegalArgumentException
	{
		PotionEffectType t;
		if(effect.equalsIgnoreCase("strength"))t = PotionEffectType.INCREASE_DAMAGE;
		else if(effect.equalsIgnoreCase("health"))t = PotionEffectType.HEAL;
		else
		{
			t = PotionEffectType.getByName(effect.toUpperCase());
			if(t == null)throw new IllegalArgumentException("No such potion effect type: " + effect);
		}
		int sec;
		int lvl = 1;
		try
		{
			sec = Integer.parseInt(seconds);
		}
		catch(NumberFormatException nfe)
		{
			throw new IllegalArgumentException("\"" + seconds + "\" is not a valid number.");
		}
		if(sec <= 0)throw new IllegalArgumentException("Seconds must be positive.");
		if(level != null)
		{
			try
			{
				lvl = Integer.parseInt(level);
			}
			catch(NumberFormatException nfe)
			{
				throw new IllegalArgumentException("\"" + level + "\" is not a valid number.");
			}
		}
		if(lvl < 1)throw new IllegalArgumentException("Level must be positive.");
		return new PotionEffect(t, sec*20, lvl - 1, true);
	}
	public void displayAction(ItemStack item, String data, int action)
	{
		ItemMeta meta = item.getItemMeta();
		if(action == NAME)meta.setDisplayName(data);
		else if(action == LORE)
		{
			String[] d = data.split(" ");
			String temp = null;
			ArrayList<String>n = new ArrayList<String>();
			for(String s:d)
			{
				if(temp == null)
				{
					temp = "" + s;
					continue;
				}
				int sl = ChatColor.stripColor(s).length();
				if(sl >= 24)
				{
					n.add(temp);
					temp = null;
					n.add(s);
					continue;
				}
				int nl = sl + ChatColor.stripColor(temp).length();
				if(nl >= 24)
				{
					n.add(temp);
					temp = "" + s;
				}
				else temp = temp + " " + s;
			}
			if(temp != null)n.add(temp);
			ArrayList<String>fin = new ArrayList<String>();
			for(String s:n)
			{
				String[] t = s.split("\\\\n");
				for(int i = 0; i < t.length; i ++)fin.add(t[i]);
			}
			meta.setLore(fin);
		}
		item.setItemMeta(meta);
	}
	public void clearAllData(ItemStack item)
	{
		item.setItemMeta(null);
		//clearData(item, NAME, LORE, SKULL_OWNER, TITLE, AUTHOR, PAGES, COLOR);
	}
	public void clearData(ItemStack item, int... params)
	{
		ItemMeta meta = item.getItemMeta();
		for(int i:params)
		{
			if(i == NAME)meta.setDisplayName(null);
			else if(i == LORE)meta.setLore(null);
			else if(i == SKULL_OWNER && meta instanceof SkullMeta)
				((SkullMeta)meta).setOwner(null);
			else if(i == TITLE && meta instanceof BookMeta)
				((BookMeta)meta).setTitle(null);
			else if(i == AUTHOR && meta instanceof BookMeta)
				((BookMeta)meta).setAuthor(null);
			else if(i == PAGES && meta instanceof BookMeta)
				((BookMeta)meta).setPages();
			else if(i == COLOR && meta instanceof LeatherArmorMeta)
				((LeatherArmorMeta)meta).setColor(null);
		}
		item.setItemMeta(meta);
	}
	private String col(String s)
	{
		return ChatColor.translateAlternateColorCodes('&', s);
	}
	private String[] bumpArgs(String[] args)
	{
		ArrayList<String>neverusethis = new ArrayList<String>();
		for(int i = 1; i < args.length; i ++)neverusethis.add(args[i]);
		return neverusethis.toArray(new String[neverusethis.size()]);
	}
	private class CmdDesc
	{
		private String cmd;
		private String desc;
		private String perm;
		public CmdDesc(String cmd, String desc, String perm)
		{
			this.cmd = cmd;
			this.desc = desc;
			this.perm = perm;
		}
		public String asDef()
		{
			return ChatColor.AQUA + cmd + ChatColor.RED + " - " + ChatColor.GOLD + desc;
		}
		public String getPerm()
		{
			return perm;
		}
	}
}
