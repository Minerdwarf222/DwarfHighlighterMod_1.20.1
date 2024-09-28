package net.dwarfhighlight;

import net.dwarfhighlight.suggestions.CurrencyConverterSuggestions;
import net.dwarfhighlight.suggestions.EditItemSuggestions;
import net.dwarfhighlight.suggestions.EditTitleSuggestions;
import net.dwarfhighlight.suggestions.FishingInteractSuggestions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

public class DwarfHighlightMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	
	// How to access github raw config: https://raw.githubusercontent.com/Minerdwarf222/DwarfHighlighterMod/main/Release/DwarfHighlighterTCLList.txt
	
	public static final Logger LOGGER = LoggerFactory.getLogger("DwarfsHighlighterMod");
	
	// Hash maps of Item names wanted for each set with the corresponding values listed (Qty wanted, list, etc.). Private, TCL, and Combined.
	public static HashMap<String, List<String>> needed_items = new HashMap<String, List<String>>();
	public static HashMap<String, List<String>> needed_personal_items = new HashMap<String, List<String>>();
	public static HashMap<String, List<String>> needed_tcl_items = new HashMap<String, List<String>>();
	
	public static boolean log_fishing = false;
	public static boolean container_checkpointing = false;
	
	// Why does this exist?
	public static int list_size = 2;
	
	private static String tcl_list_url = "https://raw.githubusercontent.com/Minerdwarf222/DwarfHighlighterMod/Dev/Release/DwarfHighlighterTCLListTest.txt";
	private static String tcl_uuids_url = "https://raw.githubusercontent.com/Minerdwarf222/DwarfHighlighterMod/Dev/Release/DwarfUniqueUUIDs";
	
	// List of Zenith Charm UUIDs to check for repeats.
	public static HashMap<Long, Boolean> unique_zenith_charms = new HashMap<Long, Boolean>();
	
	//Directories
	public static String top_level_dir = FabricLoader.getInstance().getConfigDir().resolve("DwarfHighlighterMod").toString();
	public static String charm_dir = FabricLoader.getInstance().getConfigDir().resolve("DwarfHighlighterMod/charmlogs").toString();
	public static String barrel_checkpoints_dir = FabricLoader.getInstance().getConfigDir().resolve("DwarfHighlighterMod/barrelcheckpoints").toString();
	
	//File Names
	public static String fishing_log_file = top_level_dir + "/DwarfHighlighterFishingLog.txt";
	public static String tcl_rare_list_file = top_level_dir + "/DwarfHighlighterTCLList.txt";
	public static String private_rare_list_file = top_level_dir + "/DwarfHighlighterList.txt";
	public static String config_file = top_level_dir + "/DwarfHighlighterConfig.txt";
	public static String baseCharmWeightFile = top_level_dir + "/DwarfHighlighterCharmWeights";
	public static String jsonCharmWeightFile = top_level_dir + "/DwarfHighlighterCharmWeights.json";
	public static String fishing_minigames_log_file = top_level_dir + "/DwarfHighlighterFishingMinigamesLog.txt";
	public static String containers_checkpointing = top_level_dir + "/containercoords.json";
	public static String unique_charm_uuids_file = top_level_dir + "/DwarfUniqueUUIDs";
	
	public static String charm_effects_maxes = top_level_dir+"/charm_maximums";
	public static String charm_file = charm_dir + "/DwarfHighlighterZenithCharms.txt";
	
	public static String base_barrel_checkpoints_file = barrel_checkpoints_dir+"/";
	
	//Tooltip Vars
	private static Charm cached_charm = null;
	private static List<Text> gen_text_list = new ArrayList<Text>();
	private static boolean force_reload_list = true;
	public static boolean show_roll_value = false;
	public static boolean old_roll_value = false;
	public static boolean grab_tool_tip = false;
	public static boolean show_tool_tip = true;
	
	public static int update_charm_summary_lines = 0;
	public static HashMap<String, Integer> charm_summary_overflow = new HashMap<String, Integer>();
	public static HashMap<String, Double> charm_summary_maxes = new HashMap<String, Double>();
	public static HashMap<String, Boolean> charm_summary_is_percent = new HashMap<String, Boolean>();
	
	public static JsonObject json_weights = new JsonObject();
	
	//Last right clicked block
	public static BlockPos last_right_clicked = null;
	public static HashMap<String, String> check_containers = new HashMap<String,String>();
	public static boolean anti_monu_flag = false;
	
	public static CharmMaxs charm_maxes = new CharmMaxs(top_level_dir);
	
	//Chat grab toggle
	public static boolean grab_from_chat = false;
	//public static boolean grabPlayersEffects = false;
	
	public static long sirius_start_time = 0;
	
	// Test Boss Bar stuff
	public static int paradox_mode = 0;
	public static boolean check_bars = false;
	public static HashMap<String, UUID> paradox_players_bars = new HashMap<String, UUID>();
	public static HashMap<String, Long> paradox_players_times = new HashMap<String, Long>();
	public static HashMap<String, Boolean> paradox_players_flag = new HashMap<String, Boolean>();
	public static int silver_construct_phase = 0;
	public static List<Double> silver_construct_coords = Arrays.asList(242.0, 215.0, 190.0);
	public static boolean watch_for_next_clear = false;
	
	// Various Toggles
	public static boolean have_pop_sounds = true;
	public static boolean show_charm_grab_msg = true;
	public static boolean changed_pop_sounds = true;
	public static boolean show_chat_message_if_highlighted_items = false;
	
	public static boolean show_charm_overflow = false;
	
	public static ItemStack test_stack = null;
	public static List<Text> bazaar_text = new ArrayList<Text>();
	
	public static boolean show_sirius = false;
	public static boolean show_paradox = false;
	
	public static boolean show_all_profiles = false;
	
	/*
	 * TODO:
	 * Finish Changing over to weight and highlight only being in json.
	 */
	
	// private static int rainbowCounter = 0;
	// private static boolean rainbowCharm = false;
	
	// What is a good rainbow?
	// Dark red, red, orange, yellow, light green, dark green, aqua, blue, dark blue, dark purple?
	// private static Formatting[] rainbowSetup = {Formatting.DARK_RED, Formatting.RED, Formatting.GOLD, Formatting.YELLOW, Formatting.GREEN, Formatting.DARK_GREEN, Formatting.AQUA, Formatting.BLUE, Formatting.DARK_BLUE, Formatting.DARK_PURPLE};
	
	private static void grabGitUUIDList () {
		
		try {
			@SuppressWarnings("resource")
			String out = new Scanner(new URL(tcl_uuids_url).openStream(), "UTF-8").useDelimiter("\\A").next();
			
			//System.out.println(out);
			
			String[] git_uuids = out.split("\\n");
			
			//System.out.println(git_uuids.length);
			
			List<Long> new_uuids = new ArrayList<Long>();
			
			for(String test_uuid : git_uuids) {
				
				if(test_uuid.length()==0) continue;
				
				if(unique_zenith_charms.containsKey(Long.parseLong(test_uuid))) continue;
				
				new_uuids.add(Long.parseLong(test_uuid));
				
				//System.out.println("Added New UUID! "+test_uuid);
				
				unique_zenith_charms.put(Long.parseLong(test_uuid), true);
				
				//System.out.println(test);
			}
			
			
			// https://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
			try(FileWriter fw = new FileWriter(DwarfHighlightMod.unique_charm_uuids_file, true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out_pw = new PrintWriter(bw))
				{
					for(Long depths_uuid : new_uuids) {
						out_pw.print("\n"+depths_uuid+"");
					}
				} catch (IOException e) {
					//exception handling left as an exercise for the reader
				}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	
	private static void grabGitTCLList () 
	{
		FileWriter temp_file_writer;
		try {
			temp_file_writer = new FileWriter(tcl_rare_list_file);
			BufferedWriter temp_write = new BufferedWriter(temp_file_writer);

			@SuppressWarnings("resource")
			String out = new Scanner(new URL(tcl_list_url).openStream(), "UTF-8").useDelimiter("\\A").next();
			
			//System.out.println(out);
			for(String test_titles : DwarfHighlighterModMenuIntegration.guild_list_titles.keySet()) {
				if(DwarfHighlighterModMenuIntegration.guild_list_titles.get(test_titles)) {
					out = out.replace("!"+test_titles, "%"+test_titles);
				}else {
					out = out.replace("%"+test_titles, "!"+test_titles);
				}
			}
			
			temp_write.write(out);
			temp_write.close();
			
			DwarfHighlighterModMenuIntegration.setcheckTCLListValue(true);
			DwarfHighlighterModMenuIntegration.reloadLists();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void setneededPersonalItems (List<List<String>> needed_items_update) 
	{
		
		needed_personal_items.clear();
		
		for(List<String> item : needed_items_update) {
			
			needed_personal_items.put(item.get(0),item.subList(1,item.size()));
			
		}
	}
	
	
	public static void setneededTCLItems (List<List<String>> needed_items_update) 
	{
		
		needed_tcl_items.clear();
		
		for(List<String> item : needed_items_update) {
			
			needed_tcl_items.put(item.get(0),item.subList(1,item.size()));
			
		}		
	}
	
	
	public static void mergeItemLists () 
	{
		
		needed_items.clear();
		
		for(String item_name : needed_personal_items.keySet()) {
			
			if(!needed_items.containsKey(item_name)) {
				
				needed_items.put(item_name, needed_personal_items.get(item_name).subList(0,list_size));
				
			}
		}
		
		for(String item_name : needed_tcl_items.keySet()) {
			
			if(needed_items.containsKey(item_name)) {
				
				if(needed_items.get(item_name).get(1).equals("p")) {
					
					needed_items.get(item_name).set(1, "p/g");
					needed_items.put(item_name, needed_items.get(item_name));
					
				}
				
			}else {
				
				needed_items.put(item_name, needed_tcl_items.get(item_name).subList(0,list_size));
				
			}
			
		}
	}
	
	
	private static String bestConversion(String going_from, String going_to) 
	{
		
		int going_from_currency;
		int going_to_currency;
		
		switch(going_from.toLowerCase()) {
		
		case("hxp"):
			going_from_currency = 0;
			break;
			
		case("hcs"):
			going_from_currency = 1;
			break;
			
		case("har"):
			going_from_currency = 2;
			break;
			
		default:
			return "First argument should be hxp/hcs/har.";
		}
		
		
		switch(going_to.toLowerCase()) {
		
		case("cxp"):
			going_to_currency = 0;
			break;
		
		case("ccs"):
			going_to_currency = 1;
			break;
		
		case("ar"):
			going_to_currency = 2;
			break;
		
		default:
			return "Second argument should be cxp/ccs/ar.";
		}
		
		if(going_from_currency == going_to_currency) { return "Just hand convert it.";}
		
		int inter_trade_currency = 0;
		String hacky_solution = going_from_currency + "" + going_to_currency;
		
		if(!hacky_solution.contains("0")) {
			inter_trade_currency = 0;
		}else if(!hacky_solution.contains("1")) {
			inter_trade_currency = 1;
		}else {
			inter_trade_currency = 2;
		}
		
		return DwarfHighlighterModMenuIntegration.conversionComparer(going_from_currency, going_to_currency, inter_trade_currency);
	}
	
	
	private static String parseEditItem(String List, String item_name, String edit_type, String Input) 
	{
		
		if(!List.equals("private") && !List.equals("tcl") && !List.equals("both")) return "Invalid List name given.";
		if(!needed_items.containsKey(item_name)) return "The given item does not exist.";
		if(!edit_type.equals("rename")&&!edit_type.equals("quantity")) return "The given edit type does not exist.";
		if(edit_type.equals("rename") && needed_items.containsKey(Input)) return "There is already an item named that.";
		if(edit_type.equals("rename") && Input.equals("")) return "No name was entered.";
		
		if(edit_type.equals("quantity") && Input.isEmpty()) Input = "0";
		
		if(List.equals("private") || List.equals("both")) {
			if(edit_type.equals("rename")) {			
				for(String temp : needed_personal_items.keySet()) {
					if (temp.equals("item_name")) {
						needed_personal_items.put(Input, needed_personal_items.get(temp));
						needed_personal_items.remove(temp);
						break;
					}
				}				
				
			}else if(edit_type.equals("quantity")) {				
				for(String temp : needed_personal_items.keySet()) {
					if (temp.equals("item_name")) {
						needed_personal_items.get(temp).set(0, Input);
						break;
					}
				}		
			}
			DwarfHighlighterModMenuIntegration.updateList(true);
		}
		
		if(List.equals("tcl") || List.equals("both")) {
			if(edit_type.equals("rename")) {
				if(needed_tcl_items.containsKey(item_name)) {
					needed_tcl_items.put(Input, needed_tcl_items.get(item_name));
					needed_tcl_items.remove(item_name);	
				}
				
			}else if(edit_type.equals("quantity")) {
				if(needed_tcl_items.containsKey(item_name)) {
					needed_tcl_items.get(item_name).set(0, Input);
				}		
			}
			DwarfHighlighterModMenuIntegration.updateList(false);
		}
		
		return "Edit made.";
	}
	
	
	private static String parseDeleteItem(String List, String item_name) 
	{
		
		if(!List.equals("private") && !List.equals("tcl")) return "Invalid List name given.";
		if(!needed_items.containsKey(item_name)) return "The given item does not exist.";
			
		if(List.equals("private")) {
				for(String temp : needed_personal_items.keySet()) {
					if (temp.equals("item_name")) {
						needed_personal_items.remove(temp);
						break;
					}
				}				
				DwarfHighlighterModMenuIntegration.updateList(true);
		}
		
		if(List.equals("tcl")) {
				if(needed_tcl_items.containsKey(item_name)) {
					needed_tcl_items.remove(item_name);	
				}
				DwarfHighlighterModMenuIntegration.updateList(false);
		}
		return "Item Deleted.";
	}
	
	
	private static String parseEditTitle(String List, String Title) 
	{
		
		if(!List.equals("private") && !List.equals("tcl")) return "Invalid List name given.";
		
		boolean visibility_status = false;
		
		if(List.equals("private")) {
			if(!DwarfHighlighterModMenuIntegration.private_list_titles.containsKey(Title)) return "Invalid Title for list.";
			visibility_status = DwarfHighlighterModMenuIntegration.private_list_titles.get(Title);
			DwarfHighlighterModMenuIntegration.private_list_titles.replace(Title, !DwarfHighlighterModMenuIntegration.private_list_titles.get(Title));
			DwarfHighlighterModMenuIntegration.updateList(true);
		}
		
		if(List.equals("tcl")) {
			if(!DwarfHighlighterModMenuIntegration.guild_list_titles.containsKey(Title)) return "Invalid Title for list.";
			visibility_status = DwarfHighlighterModMenuIntegration.guild_list_titles.get(Title);
			DwarfHighlighterModMenuIntegration.guild_list_titles.replace(Title, !DwarfHighlighterModMenuIntegration.guild_list_titles.get(Title));
			DwarfHighlighterModMenuIntegration.updateList(false);
		}
		
		return Title + " is now " + (!visibility_status ? "hidden." : "visible.");
	}
		
	
	private static void writeFishingInteractions(String Interaction, String Item, String Qty) 
	{
		
		if(Interaction.equals("combat")) {
			Interaction = Qty+";combat";
			
			if (!(Qty.equals("0") || Qty.equals("1") || Qty.equals("2"))) {
				return;
			}
			
		}else if(Interaction.equals("minigame")) {
			Interaction = "1;minigame";
		}else { return; }
		
		writeToFile(Interaction, fishing_log_file, top_level_dir + "/DwarfHighlightModFishing_temp.txt", "interactions", 1.0);
		
		if(Interaction.contains("combat")) return;
		
		// Guard Clause
		if(Qty.equals("0") || !(Qty.equals("1") || Qty.equals("3") || Qty.equals("4") || Qty.equals("5"))) return;
		
		List<String> fish_types = Arrays.asList("salmon","flounder","carp","sardine","trout","seabass","shroomfish","mungfish","monkfish");
		
		// Guard Clauses
		if(Item.equals("cache") && !Qty.equals("1")) {
			return;
		}else if(!Item.equals("cache") && !fish_types.contains(Item)) {
			return;
		}else if(fish_types.contains(Item) && !(Qty.equals("3") || Qty.equals("4") || Qty.equals("5"))) {
			return;
		}
		
		if(Item.equals("cache")) {
			writeToFile("1;"+Item, fishing_minigames_log_file, top_level_dir + "/DwarfHighlightModFishingMinigames_temp.txt", "cache", 1.0);
		}else {
			writeToFile("1;"+Item+Qty, fishing_minigames_log_file, top_level_dir + "/DwarfHighlightModFishingMinigames_temp.txt", "quality " + Qty, 1.0);
		}
		
	}
	
	
	public static void writeToFile(String item_to_add, String base_file_name, String temp_file_name, String item_title, double add_amt)
	{
    	
        FileWriter temp_file_writer; 
                	
        File temp_file = new File(temp_file_name);
        File old_file = new File(base_file_name);
                	
        try {
            			
        	if(temp_file.createNewFile()) {
            				
            	//DwarfHighlightMod.LOGGER.info("Created Temp List File.");
            				
            }
            			
        } catch (IOException e) {
            			
            e.printStackTrace();
            			
        }	            		
            		
        try {
			BufferedReader reader = Files.newBufferedReader(Paths.get(base_file_name));
			temp_file_writer = new FileWriter(temp_file_name);
	        			
	        BufferedWriter temp_write = new BufferedWriter(temp_file_writer);
	        			
	        String input_line = "";
	        String title = "";
	        			
	        boolean item_saved = false;
	        			
	        while ((input_line = reader.readLine()) != null) {
	        				       				
	        	if(input_line.isEmpty()) {
	        		if(item_saved) continue;
	        					
	        		temp_write.write(item_to_add + ";" + add_amt);
	        					
	        		item_saved = true;
	        				
	        		continue;
	        	}
	        				
	        	input_line = input_line.toLowerCase();	
	        				
	        	if(input_line.charAt(0) == '!') {
	        		if(!item_saved && !title.isEmpty()) {
	        			if(title.equals(item_title)) {
	        				temp_write.write(item_to_add+";" + add_amt);
	        				temp_write.newLine();
	        				item_saved = true;
	        			}
	        		}
	        		title = input_line.substring(1);
	        	}
	        				
	        	if(!item_saved && input_line.startsWith(item_to_add)) {
	        					
	        		double item_qty = Double.parseDouble(input_line.substring(input_line.lastIndexOf(";")+1))+add_amt;
	        					
	        		temp_write.write(item_to_add+";"+item_qty);
	        		temp_write.newLine();
	        		item_saved = true;
	        		continue;
	        					
	        	}else {
	        					
	        		temp_write.write(input_line);
	        		temp_write.newLine();
	        		continue;
	        					
	        	}
	        						        				
	        }
	        			
	        if(!item_saved) {
    					
	        	temp_write.write(item_to_add + ";"+add_amt);
    					
    			item_saved = true;
	        }
	        			
	        temp_write.close();
	        reader.close();
	        temp_file_writer.close();
	        			
	        old_file.delete();
	        temp_file.renameTo(old_file);
	        			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
	
	
	@SuppressWarnings("resource")
	private static void initializeCommands() 
	{
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfToggleCheckpointing")
	    		.executes(context -> {
	    			container_checkpointing = !container_checkpointing;
	    			if(container_checkpointing) {
		    			context.getSource().sendFeedback(Text.literal("Checkpointing ON."));
	    			}else {
		    			context.getSource().sendFeedback(Text.literal("Checkpointing OFF."));	
	    			}
	    			return 1;
	    		}
	    )));	
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfToggleFishing")
	    		.executes(context -> {
	    			log_fishing = !log_fishing;
	    			if(log_fishing) {
		    			context.getSource().sendFeedback(Text.literal("Fish Logging ON."));
	    			}else {
		    			context.getSource().sendFeedback(Text.literal("Fish Logging OFF."));	
	    			}
	    			return 1;
	    		}
	    )));	
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfAddFishingInter")
				.then(argument("Interaction", StringArgumentType.string())
						.suggests(FishingInteractSuggestions.interactionSUGGESTION_PROVIDER)
	    		.then(argument("Loot", StringArgumentType.string())
	    				.suggests(FishingInteractSuggestions.itemRecvSUGGESTION_PROVIDER)
	    		.then(argument("Quality", StringArgumentType.string())
	    				.suggests(FishingInteractSuggestions.numberSUGGESTION_PROVIDER)
	    		.executes(context ->{
	    			if(!log_fishing) return 1;
	    			
	    			String interaction_given = StringArgumentType.getString(context, "Interaction").toLowerCase();
	    			String item_given = StringArgumentType.getString(context, "Loot").toLowerCase();
	    			String qty_given = StringArgumentType.getString(context, "Quality").toLowerCase();
	    			
	    			writeFishingInteractions(interaction_given, item_given, qty_given);
	    			
	    			return 1;
	    		})
	    )))));	
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfGrabNewTCLList")
	    		.executes(context -> {
	    			grabGitTCLList();
	    			context.getSource().sendFeedback(Text.literal("Grabbed new list."));
	    			return 1;
	    		}
	    )));		
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfGrabNewUUIDList")
	    		.executes(context -> {
	    			grabGitUUIDList();
	    			context.getSource().sendFeedback(Text.literal("Grabbed new UUID list."));
	    			return 1;
	    		}
	    )));	
		
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfDeleteItem")
	    		.then(argument("List", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.listSUGGESTION_PROVIDER)
	    		.then(argument("Item", StringArgumentType.string())
	    				.suggests(EditItemSuggestions.itemSUGGESTION_PROVIDER)
	    		.executes(context -> {
	    			context.getSource().sendFeedback(Text.literal(parseDeleteItem(StringArgumentType.getString(context, "List").toLowerCase(),StringArgumentType.getString(context, "Item").toLowerCase())));
	    			return 1;
	    		}
	    )))));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfCreateCharmProfile")
	    		.then(argument("Profile", StringArgumentType.string())
	    		.executes(context -> {
	    			String charm_profile_name = StringArgumentType.getString(context, "Profile");
	    			
	    			if(json_weights.keySet().contains(charm_profile_name)) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " already exists."));
	    				return 1;
	    			}
	    			
	    			json_weights.addProperty("enabled", charm_profile_name);
	    			
	    			JsonObject new_charm_profile = new JsonObject();
	    			new_charm_profile.addProperty("cutoff", 0.0);
	    			
	    			json_weights.add(charm_profile_name, new_charm_profile);
	    			
	    			context.getSource().sendFeedback(Text.literal("Created new charm profile: " + charm_profile_name + " and is new current profile."));
	    			
	    			saveCharmJson();
	    			
	    			return 1;
	    		}
	    ))));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfCurrentCharmProfile")
	    		.executes(context -> {
	    				    			
	    			context.getSource().sendFeedback(Text.literal(DwarfHighlightMod.json_weights.get("enabled").getAsString() + " is the current profile."));
	    			
	    			return 1;
	    		}
	    )));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfChangeCharmProfile")
	    		.then(argument("Profile", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.ProfileSUGGESTION_PROVIDER)
	    		.executes(context -> {
	    			String charm_profile_name = StringArgumentType.getString(context, "Profile");
	    			
	    			if(!json_weights.keySet().contains(charm_profile_name)) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " does not exist."));
	    				return 1;
	    			}
	    			
	    			if(json_weights.get(charm_profile_name).isJsonPrimitive()) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " is an invalid profile name."));
	    				return 1;
	    			}
	    			
	    			json_weights.addProperty("enabled", charm_profile_name);
	    			
	    			charm_maxes.reloadCharmWeights();
	    			
	    			context.getSource().sendFeedback(Text.literal(charm_profile_name + " is now the profile."));
	    			
	    			saveCharmJson();
	    			
	    			return 1;
	    		}
	    ))));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfDeleteCharmProfile")
	    		.then(argument("Profile", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.ProfileSUGGESTION_PROVIDER)
	    		.executes(context -> {
	    			String charm_profile_name = StringArgumentType.getString(context, "Profile");
	    			
	    			if(!json_weights.keySet().contains(charm_profile_name)) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " does not exist."));
	    				return 1;
	    			}
	    			
	    			if(json_weights.get(charm_profile_name).isJsonPrimitive()) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " is an invalid profile name."));
	    				return 1;
	    			}
	    			
	    			if(charm_profile_name.equals(DwarfHighlightMod.json_weights.get("enabled").getAsString())) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " is the current profile. You cannot delete it."));
	    				return 1;
	    			}
	    			
	    			json_weights.remove(charm_profile_name);
	    			
	    			charm_maxes.reloadCharmWeights();
	    			
	    			context.getSource().sendFeedback(Text.literal(charm_profile_name + " is now deleted."));
	    			
	    			saveCharmJson();
	    			
	    			return 1;
	    		}
	    ))));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfChangeProfileCutoff")
	    		.then(argument("Profile", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.ProfileSUGGESTION_PROVIDER)
	    		.then(argument("Cutoff", DoubleArgumentType.doubleArg())
	    		.executes(context -> {
	    			double new_cutoff = DoubleArgumentType.getDouble(context, "Cutoff");
	    			String charm_profile_name = StringArgumentType.getString(context, "Profile");
	    			
	    			if(!json_weights.keySet().contains(charm_profile_name)) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " does not exist."));
	    				return 1;
	    			}
	    			
	    			if(json_weights.get(charm_profile_name).isJsonPrimitive()) {
	    				context.getSource().sendFeedback(Text.literal(charm_profile_name + " is an invalid profile name."));
	    				return 1;
	    			}
	    			
	    			json_weights.getAsJsonObject(charm_profile_name).addProperty("cutoff", new_cutoff);
	    			
	    			context.getSource().sendFeedback(Text.literal(new_cutoff + " is the new cutoff for profile " + charm_profile_name + "."));
	    			
	    			saveCharmJson();
	    			
	    			return 1;
	    		}
	    )))));
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfEditCharm")
	    		.then(argument("Effect", StringArgumentType.string())
	    				.suggests(EditItemSuggestions.charmSUGGESTION_PROVIDER)
	    		.then(argument("New Value", DoubleArgumentType.doubleArg())
	    		.executes(context -> {
	    			
	    			String given_effect = StringArgumentType.getString(context, "Effect").toLowerCase();
	    			
	    			if(!charm_maxes.charm_maxes.containsKey(given_effect)) {
	    				context.getSource().sendFeedback(Text.literal(given_effect + " does not exist."));
	    				return 1;
	    			}
	    			
	    			updateCharmMaxes(given_effect, DoubleArgumentType.getDouble(context, "New Value"));
	    			context.getSource().sendFeedback(Text.literal(StringArgumentType.getString(context, "Effect") + " is now weighted as " + DoubleArgumentType.getDouble(context, "New Value")));
	    			return 1;
	    		}
	    )))));
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfHighlightEffect")
	    		.then(argument("Effect", StringArgumentType.string())
	    				.suggests(EditItemSuggestions.charmSUGGESTION_PROVIDER)
	    		.executes(context -> {
	    			
	    			String given_effect = StringArgumentType.getString(context, "Effect").toLowerCase();
	    			
	    			if(!charm_maxes.charm_maxes.containsKey(given_effect)) {
	    				context.getSource().sendFeedback(Text.literal(given_effect + " does not exist."));
	    				return 1;
	    			}
	    			
	    			boolean highlight_effect = false;
	    			
	    			String enabled_profile = json_weights.get("enabled").getAsString();
	    			
	    			if(json_weights.getAsJsonObject(enabled_profile).get(given_effect) != null && !json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(given_effect).get("highlight").getAsBoolean()) {
	    				highlight_effect = true;
	    			}	    			
	    			
	    			//charm_maxes.charm_maxes.get(given_effect).set(2, highlight_effect);
	    			
	    			if(!json_weights.getAsJsonObject(enabled_profile).has(given_effect)) {
	    				
	    				JsonObject temp_object = new JsonObject();
	    				temp_object.addProperty("weight", DwarfHighlighterModMenuIntegration.default_effect_weight);
	    				temp_object.addProperty("highlight", false);
	    				
	    				json_weights.getAsJsonObject(enabled_profile).add(given_effect, temp_object);
	    			}
	    			
	    			json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(given_effect).remove("highlight");
	    			json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(given_effect).addProperty("highlight", highlight_effect);
	    			
	    			if(json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(given_effect).get("weight").getAsDouble() == DwarfHighlighterModMenuIntegration.default_effect_weight && (!highlight_effect)) {
	    				json_weights.getAsJsonObject(enabled_profile).remove(given_effect);
	    			}
	    			
	    			saveCharmJson();
	    			updateCharmMaxes(StringArgumentType.getString(context, "Effect"), json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(given_effect).get("weight").getAsDouble());
	    			
	    			if(highlight_effect) {
	    				context.getSource().sendFeedback(Text.literal(StringArgumentType.getString(context, "Effect") + " is now highlighted."));
	    			}else {
	    				context.getSource().sendFeedback(Text.literal(StringArgumentType.getString(context, "Effect") + " is no longer highlighted."));
	    			}
	    			
	    			return 1;
	    		}
	    ))));
	    
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfToggleTitle")
	    		.then(argument("List", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.listSUGGESTION_PROVIDER)
	    		.then(argument("Title", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.titleSUGGESTION_PROVIDER)
	    		.executes(context -> {
	    			context.getSource().sendFeedback(Text.literal(parseEditTitle(StringArgumentType.getString(context, "List").toLowerCase(),StringArgumentType.getString(context, "Title").toLowerCase())));
	    			return 1;
	    		}
	    )))));
		
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfEditItem")
	    		.then(argument("List", StringArgumentType.string())
	    				.suggests(EditItemSuggestions.listSUGGESTION_PROVIDER)
	    		.then(argument("Item", StringArgumentType.string())
	    				.suggests(EditItemSuggestions.itemSUGGESTION_PROVIDER)
	    		.then(argument("Edit Type",StringArgumentType.string())
	    				.suggests(EditItemSuggestions.editTypeSUGGESTION_PROVIDER)
	    		.then(argument("Input",StringArgumentType.string())
	    		.executes(context -> {
	    			context.getSource().sendFeedback(Text.literal(parseEditItem(StringArgumentType.getString(context, "List").toLowerCase(),StringArgumentType.getString(context, "Item").toLowerCase(),StringArgumentType.getString(context, "Edit Type").toLowerCase(),StringArgumentType.getString(context, "Input").toLowerCase())));
	    			return 1;
	    		}
	    )))))));
		
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfCurConvert")
	    		.then(argument("HyperFrom", StringArgumentType.string())
	    				.suggests(CurrencyConverterSuggestions.hyperSUGGESTION_PROVIDER)
	    				.then(argument("CompTo", StringArgumentType.string())
	    						.suggests(CurrencyConverterSuggestions.compressedSUGGESTION_PROVIDER)
	    						.executes(context -> {
	    							context.getSource().sendFeedback(Text.literal(bestConversion(StringArgumentType.getString(context, "HyperFrom"),StringArgumentType.getString(context, "CompTo"))));
	    							return 1;
	    						}
	            )))));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfReloadList")
	    		.then(argument("List", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.listSUGGESTION_PROVIDER)
	    		.executes(context -> {
	    			
	    			String givenList = StringArgumentType.getString(context, "List").toLowerCase();
	    			
	    			if(!givenList.equals("private") && !givenList.equals("tcl")) {
						context.getSource().sendFeedback(Text.literal("Not a vaild List."));
						return 1;
					}
	    			
	    			context.getSource().sendFeedback(Text.literal(givenList + " List Relaoded."));
	    			if(givenList.equals("private")) {
	    				DwarfHighlighterModMenuIntegration.setcheckListValue(true);
	    			}else {
	    				DwarfHighlighterModMenuIntegration.setcheckTCLListValue(true);
	    			}
	    			DwarfHighlighterModMenuIntegration.reloadLists();
	    			return 1;
	    		}
	    ))));
		
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfSaveList")
	    		.then(argument("List", StringArgumentType.string())
	    				.suggests(EditTitleSuggestions.listSUGGESTION_PROVIDER)
				.executes(context -> {
					String givenList = StringArgumentType.getString(context, "List").toLowerCase();
					
					if(!givenList.equals("private") && !givenList.equals("tcl")) {
						context.getSource().sendFeedback(Text.literal("Not a vaild List."));
						return 1;
					}
					
					context.getSource().sendFeedback(Text.literal(givenList + " List Saved."));
					DwarfHighlighterModMenuIntegration.updateList(givenList.equals("private"));
					return 1;
				}
        ))));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfReloadMaxs")
				.executes(context -> {
					charm_maxes.reloadCharmMaxes();
					if(cached_charm != null) cached_charm = null;
					context.getSource().sendFeedback(Text.literal("Reloaded Maxes for Charms"));
					return 1;
				}
        )));

	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfToggleContainerChecker")
				.executes(context -> {
					
					if(!DwarfHighlighterModMenuIntegration.getToggleValue()) {
						
						context.getSource().sendFeedback(Text.literal("Enabled Mod."));
						DwarfHighlighterModMenuIntegration.setToggleValue(true);
						
					}else {
						
						context.getSource().sendFeedback(Text.literal("Disabled Mod."));
						DwarfHighlighterModMenuIntegration.setToggleValue(false);
						
					}
					return 1;
				}
        )));
	    /*
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfDevCommand")
				.executes(context -> {
					
					boss_bar_uuid = UUID.randomUUID();
					
					((DwarfHighlightBossBarHudAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().put(boss_bar_uuid, new ClientBossBar(boss_bar_uuid, Text.literal("Test Bar"), 0.5f, BossBar.Color.BLUE, BossBar.Style.NOTCHED_10, false, false, false));
					//add_boss_bar = true;
					return 1;
				}
        )));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfDevCommand2")
				.executes(context -> {
					((DwarfHighlightBossBarHudAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().remove(boss_bar_uuid);
					return 1;
				}
        )));
	    
	    ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("DwarfDevCommandGrabTags")
				.executes(context -> {
					String output_str = "Player Objectives: \n";
					for(String tag : MinecraftClient.getInstance().player.getScoreboard().getObjectiveNames()){
						output_str = output_str + tag + "\n";
					}
					LOGGER.info(output_str);
					return 1;
				}
        )));
	    */
	    
	}
	
	
	private static void test_and_create_files_dirs()
	{
		new File(top_level_dir).mkdirs();
	    new File(charm_dir).mkdirs();
	    new File(barrel_checkpoints_dir).mkdirs();
	    
		try {
			
			File test_if_config = new File(config_file);
			
			if(test_if_config.createNewFile()) {
				
				DwarfHighlightMod.LOGGER.info("Created Config File.");
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		} 
	    
		try {
			
			File test_if_config = new File(private_rare_list_file);
			
			if(test_if_config.createNewFile()) {
				
				DwarfHighlightMod.LOGGER.info("Created List File.");
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		try {
			
			File test_if_config = new File(tcl_rare_list_file);
			
			if(test_if_config.createNewFile()) {
				
				DwarfHighlightMod.LOGGER.info("Created TCL List File.");
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		//Fishing
		try {
			
			File test_if_config = new File(fishing_log_file);
			
			if(test_if_config.createNewFile()) {
				
				DwarfHighlightMod.LOGGER.info("Created Fishing Log.");
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		try {
			
			File test_if_config = new File(fishing_minigames_log_file);
			
			if(test_if_config.createNewFile()) {
				
				DwarfHighlightMod.LOGGER.info("Created Fishing Minigames Log.");
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		try {
			
			File charm_weight_json = new File(jsonCharmWeightFile);
			
			if(charm_weight_json.createNewFile()) {
				
				DwarfHighlightMod.LOGGER.info("Created Json Charm Weights file.");
				
				if(new File(baseCharmWeightFile).isFile()) {
					
					try {
						
						JsonObject json_temp_weights = new JsonObject();
						JsonObject json_default_weights = new JsonObject();
						
						json_temp_weights.addProperty("enabled", "default");
						json_default_weights.addProperty("cutoff", 0.0);
						
						//json_weights.add("default", json_default_weights);
						
						FileReader fr = new FileReader(DwarfHighlightMod.baseCharmWeightFile);
						BufferedReader reader = new BufferedReader(fr);
						String input_line = "";
						
						while ((input_line = reader.readLine()) != null) {
							
							JsonObject json_effect_object = new JsonObject();
							
							if(input_line.length() == 0) { continue; }	
							
							String[] effect_an_value = input_line.split(",");
							
							if(effect_an_value.length != 2) { continue;}
							if(effect_an_value[0].length() == 0 ||effect_an_value[1].length() == 0) {continue;}
							
							Double highlight_effect = 0.0;
							
							if(effect_an_value[0].startsWith("!")) {
								highlight_effect = 1.0;
								effect_an_value[0] = effect_an_value[0].substring(1);
							}
							
							if(charm_maxes.charm_maxes.containsKey(effect_an_value[0])) {
								
								json_effect_object.addProperty("highlight", highlight_effect == 1.0);
								json_effect_object.addProperty("weight", Double.parseDouble(effect_an_value[1]));
								
								json_default_weights.add(effect_an_value[0], json_effect_object);

							}else {
								//DwarfHighlightMod.LOGGER.warn("Found unknown effect in charm weights file! Effect: " + effect_an_value[0]);
							}
							
							
						}
						
						json_temp_weights.add("default", json_default_weights);
						
						reader.close();
						fr.close();
						
						FileWriter fileWriter = new FileWriter(charm_weight_json);
						
						fileWriter.write(json_temp_weights.toString());
						fileWriter.flush();
						
						fileWriter.close();
						
						json_weights = json_temp_weights.deepCopy();
				
					} catch (FileNotFoundException e) {
				
					System.out.println("File Not Found.");
				
					} catch (IOException e) {
				
					e.printStackTrace();
				
					}
					
				}else {
					try {
						JsonObject json_temp_weights = new JsonObject();
						JsonObject json_default_weights = new JsonObject();
					
						json_temp_weights.addProperty("enabled", "default");
						json_default_weights.addProperty("cutoff", 0.0);
					
						json_temp_weights.add("default", json_default_weights);
					
						FileWriter fileWriter = new FileWriter(charm_weight_json);
					
						fileWriter.write(json_temp_weights.toString());
						fileWriter.flush();
					
						fileWriter.close();
						
						json_weights = json_temp_weights.deepCopy();
			
					} catch (FileNotFoundException e) {
			
						System.out.println("File Not Found.");
			
					} catch (IOException e) {
			
						e.printStackTrace();
			
					}
					
				}
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}		
		
		//Checkpointing
		File test_for_json = new File(containers_checkpointing);
		
		if(test_for_json.exists()) {
		
			try {
			
				//https://gist.github.com/madonnelly/1371597
				JsonObject test_obj = JsonParser.parseString(Files.readString(Paths.get(containers_checkpointing))).getAsJsonObject();
			
				for(String barrel_coords :  test_obj.keySet()) {
					if(barrel_coords.equals("log_file") || barrel_coords.equals("parse_token")) {
						check_containers.put(barrel_coords, test_obj.getAsJsonPrimitive(barrel_coords).getAsString());
					}else{
						check_containers.put(barrel_coords, test_obj.getAsJsonObject(barrel_coords).get("label").getAsString());
					}
				}
			
			} catch (IOException e) {
			
				DwarfHighlightMod.LOGGER.error("Could not find or read json file for barrel checking.");
			
			}
		}
		
		//Reading in charms
		
		try {
			
			File test_for_charm_uuids = new File(unique_charm_uuids_file);
			
			if(test_for_charm_uuids.exists()) {
				
				FileReader fr = new FileReader(unique_charm_uuids_file);
				BufferedReader reader = new BufferedReader(fr);
				String input_line = "";
				
				while ((input_line = reader.readLine()) != null) {
					
					if(input_line.length() == 0) continue;
					
					unique_zenith_charms.put(Long.parseLong(input_line),true);
					
				}
				
				reader.close();
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		// Charm Override
		try {
			File test_for_charm_effects = new File(charm_effects_maxes);
			
			if(test_for_charm_effects.exists()) {
				
				FileReader fr = new FileReader(charm_effects_maxes);
				BufferedReader reader = new BufferedReader(fr);
				String input_line = "";
				
				if((input_line = reader.readLine()) == null) {
					FileWriter fileWriter = new FileWriter(charm_effects_maxes);
					
					fileWriter.write(CharmMaxs.get_version);
					fileWriter.flush();
					
					fileWriter.close();
				}else if(!input_line.equals(CharmMaxs.get_version)) {
					FileWriter fileWriter = new FileWriter(charm_effects_maxes);
					
					fileWriter.write(CharmMaxs.get_version);
					fileWriter.flush();
					
					fileWriter.close();
				}
				
				reader.close();
				
			}else {
				if(test_for_charm_effects.createNewFile()) {
					FileWriter fileWriter = new FileWriter(charm_effects_maxes);
					
					fileWriter.write(CharmMaxs.get_version);
					fileWriter.flush();
					
					fileWriter.close();
				}
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
			
	}
	
	
	/*private static void updateCharmMaxes(String effect, Double weight) {
		
		force_reload_list = true;
		
		charm_maxes.charm_maxes.get(effect).set(1, weight);
		
		FileWriter temp_file_writer; 
    	
		String base_file_name = baseCharmWeightFile;
		String temp_file_name= baseCharmWeightFile + "temp";
		
        File temp_file = new File(temp_file_name);
        File old_file = new File(baseCharmWeightFile);
                	
        try {
            			
        	if(temp_file.createNewFile()) {
            				
            	//DwarfHighlightMod.LOGGER.info("Created Temp List File.");
            				
            }
            			
        } catch (IOException e) {
            			
            e.printStackTrace();
            			
        }	            		
            		
        try {
			BufferedReader reader = Files.newBufferedReader(Paths.get(base_file_name));
			temp_file_writer = new FileWriter(temp_file_name);
	        			
	        BufferedWriter temp_write = new BufferedWriter(temp_file_writer);
	        			
	        String input_line = "";
	        			
	        boolean item_saved = false;
	        			
	        while ((input_line = reader.readLine()) != null) {
	        	
	        	if(input_line.length() == 0) continue;
	        	
	        	input_line = input_line.toLowerCase();	
	        				
	        	if(!item_saved && (input_line.startsWith(effect)||input_line.startsWith("!"+effect))) {
	        		
		        	String write_line = "";
	        		
	        		if(charm_maxes.charm_maxes.containsKey(effect) && charm_maxes.charm_maxes.get(effect).get(2) == 1.0) {
	        			write_line = "!";
	        		}
	        		
	        		if(weight != 1.0 || write_line.length() == 1) {
	        			write_line = write_line + effect + "," + weight;
	        		}
	        		
	        		item_saved = true;
	        		
	        		if(write_line.length() == 0) continue;
	        		
        			temp_write.write(write_line);
        			temp_write.newLine();
        			
	        		continue;
	        					
	        	}else {		
	        		temp_write.write(input_line);
	        		temp_write.newLine();
	        		continue;
	        					
	        	}
	        						        				
	        }
	        			
	        if(!item_saved) {
    			
	        	String write_line = "";
        		
        		if(charm_maxes.charm_maxes.containsKey(effect) && charm_maxes.charm_maxes.get(effect).get(2) == 1.0) {
        			write_line = "!";
        		}
        		
        		if(weight != 1.0 || write_line.length() == 1) {
        			write_line = write_line + effect + "," + weight;
        		}
        		
    			temp_write.write(write_line);
        		
        		item_saved = true;
	        }
	        			
	        temp_write.close();
	        reader.close();
	        temp_file_writer.close();
	        			
	        old_file.delete();
	        temp_file.renameTo(old_file);
	        			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		
	}*/
	
	public static void saveCharmJson() {
		File charm_weight_json = new File(jsonCharmWeightFile);
		
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(charm_weight_json);
			fileWriter.write(json_weights.toString());
			fileWriter.flush();
			
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void updateCharmMaxes(String effect, Double weight) {
		
		if(!json_weights.getAsJsonObject(json_weights.get("enabled").getAsString()).has(effect)) {
			
			JsonObject temp_object = new JsonObject();
			temp_object.addProperty("weight", DwarfHighlighterModMenuIntegration.default_effect_weight);
			temp_object.addProperty("highlight", false);
			
			json_weights.getAsJsonObject(json_weights.get("enabled").getAsString()).add(effect, temp_object);
		}
		
		json_weights.getAsJsonObject(json_weights.get("enabled").getAsString()).getAsJsonObject(effect).remove("weight");
		json_weights.getAsJsonObject(json_weights.get("enabled").getAsString()).getAsJsonObject(effect).addProperty("weight", weight);
	
		
		if(weight == DwarfHighlighterModMenuIntegration.default_effect_weight && !json_weights.getAsJsonObject(json_weights.get("enabled").getAsString()).getAsJsonObject(effect).get("highlight").getAsBoolean()) {
			json_weights.getAsJsonObject(json_weights.get("enabled").getAsString()).remove(effect);
		}
		
		force_reload_list = true;
		
		//charm_maxes.charm_maxes.get(effect).set(1, weight);
		
		saveCharmJson();
	}	
	
	@SuppressWarnings("resource")
	public static void outputAllCharms(ItemStack item) 
	{
		
		String charm_nbt = "";
			
		Long depths_uuid = item.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID");
					
		String item_name = item.getNbt().getCompound("plain").getCompound("display").getString("Name");
		
		if(item_name.length() == 0) {
			
			String prase_item_name = item.getNbt().getCompound("display").getString("Name");
			item_name = prase_item_name.substring(prase_item_name.indexOf("text")+7, prase_item_name.indexOf("\"",prase_item_name.indexOf("text")+9));
		}
		
		int charm_power = item.getNbt().getCompound("Monumenta").getInt("CharmPower");
		NbtCompound monumenta_tailed = item.getNbt().getCompound("Monumenta").getCompound("PlayerModified");
		
		monumenta_tailed.putString("ItemName", item_name);
		monumenta_tailed.putInt("CharmPower", charm_power);
		monumenta_tailed.put("CharmText", item.getNbt().getCompound("Monumenta").get("CharmText"));
						
		charm_nbt = monumenta_tailed.asString();
			
		DwarfHighlightMod.unique_zenith_charms.put(depths_uuid,true);
		
		if (show_charm_grab_msg) MinecraftClient.getInstance().player.sendMessage(Text.of("Yoinked " + item_name + " charm."));
		
		// if(charm_nbt.isEmpty()) return;
		
		// https://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
		try(FileWriter fw = new FileWriter(DwarfHighlightMod.charm_file, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
					out.print("\n"+charm_nbt);
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
		
		// https://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
		try(FileWriter fw = new FileWriter(DwarfHighlightMod.unique_charm_uuids_file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
			{
				out.print("\n"+depths_uuid);
			} catch (IOException e) {
				//exception handling left as an exercise for the reader
			}

	}
		
	
	//Charm parser. Given a charm's nbt returns the info we want (Charm power, Charm Name, [Effect, Roll, Rarity])
	private static void convertToCharm(ItemStack stack) {
		Charm new_charm = new Charm(stack.getNbt().getCompound("plain").getCompound("display").getString("Name"), stack.getNbt().getCompound("Monumenta").getInt("CharmPower"), stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID"), charm_maxes, stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getInt("DEPTHS_CHARM_RARITY"));
    	
    	//HashMap<String, String> stupidSetGet = new HashMap<String,String>();
    	
    	//List <String> effectLines = stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").get("CharmText");
    	
    	String abomination_string = stack.getNbt().getCompound("Monumenta").get("CharmText").asString();
    	
    	List <String> all_the_effects = new ArrayList<String>();
    	
    	int next_effect_indx = -1;
    	
    	while((next_effect_indx = abomination_string.indexOf("text")) != -1) {
    		all_the_effects.add(abomination_string.substring(next_effect_indx+7, abomination_string.indexOf('"',next_effect_indx+7)));
    		abomination_string = abomination_string.substring(next_effect_indx+5);
    	}
    	
    	Set<String> hidden_stats = stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getKeys();
    	
    	int effect_count = 0;
    	
    	//Stupid Set not having a .get() GRRRRR
    	for(String key_value : hidden_stats) {
    		
    		//stupidSetGet.put(key_value, key_value);
    		
    		if(key_value.contains("DEPTHS_CHARM_ROLLS")) {
    			
    			int roll_number = Integer.parseInt(key_value.substring(18));
    			
    			if(roll_number > effect_count) effect_count = roll_number;
    		}
    	}
    	
    	
    	for(int i = 1; i < effect_count+1; i++) {
    		
    		String effect_name = stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getString("DEPTHS_CHARM_EFFECT"+i);
    		
    		String effect_rarity = "";
    		
    		if( i == 1) {
    			effect_rarity = new_charm.convertRarity(stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getInt("DEPTHS_CHARM_RARITY"));
    		}else {
    			effect_rarity = stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getString("DEPTHS_CHARM_ACTIONS"+(i-1));
    		}
    		
    		String effect_roll = stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getDouble("DEPTHS_CHARM_ROLLS"+i) +"";
    		
    		String effect_modifier = "";
    		
    		for (String eachEffect : all_the_effects) {
    			if(eachEffect.contains(effect_name)) {
    				effect_modifier = eachEffect;
    				break;
    			}
    		}
    		
    		all_the_effects.remove(effect_modifier);
    		
    		new_charm.addEffect(effect_name, effect_rarity, effect_modifier, effect_roll);
    		
    		//LOGGER.info(effect_name);
    		
    	}
    	
    	cached_charm = new_charm;
    	
    	//LOGGER.info(new_charm.asString());
    	
	}
	
	
	private static MutableText generateNumberTextLine(String effect_name, Boolean get_roll_value) {
		
		effect_name = effect_name.toLowerCase();
		
		String modifier_roll = "";
		
		double item_roll = 0.0;
		
		if(get_roll_value) {
			modifier_roll = cached_charm.findRoll(effect_name);
		}else {
			modifier_roll = cached_charm.effects.get(effect_name).get(4);
		}
		
		if(!modifier_roll.contains("??")) item_roll = Double.parseDouble(modifier_roll);
		
		Formatting color = Formatting.GREEN;
		
		boolean low_roll = false;
		boolean high_roll = false;
		
		if (cached_charm.findRoll(effect_name).equals("??") || modifier_roll.contains("??")) {
			modifier_roll = " [" + modifier_roll + "%]";
			return Text.literal(modifier_roll).formatted(Formatting.GRAY);
		}
		
		if (item_roll < 5) {
			low_roll = true;
			color = Formatting.DARK_RED;
		} else if(item_roll < 33.5) {
			color = Formatting.RED;
		} else if(item_roll < 66.5) {
			color = Formatting.YELLOW;
		} else if(item_roll < 95) {
			color = Formatting.GREEN;
		} else {
			high_roll = true;
			color = Formatting.DARK_PURPLE;
		}
		
		if(!low_roll && !high_roll) {
			modifier_roll = "[" + modifier_roll + "%]";
			return Text.literal(modifier_roll).formatted(color);
		}else if (high_roll){
			return Text.literal("").append(Text.literal("[").formatted(color).formatted(Formatting.OBFUSCATED)).append(Text.literal(modifier_roll+"%").formatted(color).formatted(Formatting.BOLD)).append(Text.literal("]").formatted(color).formatted(Formatting.OBFUSCATED));
		}else if(low_roll) {
			modifier_roll = "[" + modifier_roll + "%]";
			return Text.literal(modifier_roll).formatted(color).formatted(Formatting.BOLD);
		}
		
		return null;
		
	}
	
	
	private static MutableText generateTotalTextLine(String appraisalShown, Double appraisalValue) {
		
		boolean low_roll = false;
		boolean high_roll = false;
		
		Formatting color = Formatting.DARK_GRAY;
		
		if(appraisalValue < 0) {
			color = Formatting.DARK_RED;
			low_roll = true;
		}else if(appraisalValue < .75) {
			color = Formatting.RED;
		}else if(appraisalValue < 1) {
			color = Formatting.YELLOW;
		}else if(appraisalValue < 1.5) {
			color = Formatting.GREEN;
		}else if(appraisalValue <= 2) {
			color = Formatting.DARK_GREEN;
		}else{
			color = Formatting.DARK_PURPLE;
			high_roll = true;
		}
		
		if(low_roll) {
			return Text.literal("["+appraisalShown+"]").formatted(color).formatted(Formatting.BOLD);
		}else if(high_roll) {
			return Text.literal("").append(Text.literal("[").formatted(color).formatted(Formatting.OBFUSCATED)).append(Text.literal(appraisalShown).formatted(color).formatted(Formatting.BOLD)).append(Text.literal("]").formatted(color).formatted(Formatting.OBFUSCATED));
		}else {
			return Text.literal("["+appraisalShown+"]").formatted(color);
		}
	}
	
	
	private static void generateNewLines(List<Text> lines) {
		
		boolean foundCharmSlot = false;
		//boolean addLine = false;		
		List<Text> temp_list = new ArrayList<Text>();
		
		int num_of_effects = cached_charm.effects.size();
		
		String enabled_profile = json_weights.get("enabled").getAsString();
		
		for(Text line : lines) {
			
			if(!foundCharmSlot) {
				
				temp_list.add(line);
				
				if(line.toString().contains("When in Charm Slot:")) {
					foundCharmSlot = true;
				}
				continue;
			}
			
			/*if(!addLine) {
				temp_list.add(line);
				line.getString();
			}*/
			
			if(num_of_effects == 0 ) {
				temp_list.add(line);
			}else {
				String effect_string = line.getString();
				//Style effectStyle = line.getStyle();
				
				String effect_name = line.getString().substring(line.getString().indexOf(" ")+1);
				
				boolean highlight_effect = false;
				
				if(json_weights.getAsJsonObject(enabled_profile).get(effect_name.toLowerCase()) != null) {
					highlight_effect = json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(effect_name.toLowerCase()).get("highlight").getAsBoolean();
				}
				
				double charm_weight = DwarfHighlighterModMenuIntegration.default_effect_weight;
				
				MutableText the_new_effect = Text.literal("");
				//String lowerCaseEffectName = effect_name.toLowerCase();
				
				if(json_weights.getAsJsonObject(enabled_profile).get(effect_name.toLowerCase()) != null) {
					charm_weight = json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(effect_name.toLowerCase()).get("weight").getAsDouble();
				}
				
				double abs_charm_weight = Math.abs(charm_weight);
				
				if(charm_weight == -1.0) {
					the_new_effect.append(Text.literal("* ").formatted(Formatting.DARK_RED));
				}else if(charm_weight == 0) {
					the_new_effect.append(Text.literal("* ").formatted(Formatting.RED));
				}else if(abs_charm_weight < 1.0) {
					the_new_effect.append(Text.literal("* ").formatted(Formatting.YELLOW));
				}else if(abs_charm_weight == 1.0) {
					the_new_effect.append(Text.literal("* ").formatted(Formatting.WHITE));
				}else if(abs_charm_weight < 1.5) {
					the_new_effect.append(Text.literal("* ").formatted(Formatting.GREEN));
				}else if(abs_charm_weight >= 1.5) {
					the_new_effect.append(Text.literal("* ").formatted(Formatting.DARK_PURPLE).formatted(Formatting.OBFUSCATED));
				}
				
				if(highlight_effect) {
					the_new_effect.formatted(Formatting.UNDERLINE);
				}
				
				if(highlight_effect) {
					the_new_effect.append(Text.literal(effect_string+" ").setStyle(line.getStyle()).formatted(Formatting.UNDERLINE).append(generateNumberTextLine(effect_name,false).formatted(Formatting.UNDERLINE)));
				}else {
					the_new_effect.append(Text.literal(effect_string+" ").setStyle(line.getStyle()).append(generateNumberTextLine(effect_name,false)));
				}
				
				if(show_roll_value) {
					
					if(highlight_effect) {
						the_new_effect.append(Text.literal(" | ").formatted(Formatting.WHITE).formatted(Formatting.UNDERLINE)).append(generateNumberTextLine(effect_name,true).formatted(Formatting.UNDERLINE));
					}else {
						the_new_effect.append(Text.literal(" | ").formatted(Formatting.WHITE)).append(generateNumberTextLine(effect_name,true));
					}
				}
				
				temp_list.add(the_new_effect);
				
				num_of_effects--;
			}
			
		}
		
		String total_appraise_value = "??";
		String total_appraise_value_v3 = "??";
		
		/*
		Less than zero, dark red
		Zero to .75 red
		.75 to 1  yellow
		1 to 1.5 green
		1.5 to 2 dark green
		2+ twisted
		*/
		
		if(cached_charm.apprasiable){
			
			//double totalApprasialValuedouble = cached_charm.apprasiedScore/((double)cached_charm.power);
			
			//total_appraise_value = String.format("%.2f", totalApprasialValuedouble).replace(',', '.');
			
			if(show_all_profiles) {
				
				cached_charm.generateV2_5Score(false);
				cached_charm.generateV3Scores();
				
				total_appraise_value = String.format("%.2f", cached_charm.apprasied_v2_5_score).replace(',','.');
				
				MutableText the_new_effect = Text.literal("").append(generateTotalTextLine(total_appraise_value, cached_charm.apprasied_v2_5_score));
				
				for(int i = 0; i < cached_charm.appraised_v3_names.size(); i++) {
					total_appraise_value_v3 = String.format("%.2f", cached_charm.appraised_v3_scores.get(i)).replace(',','.');
					the_new_effect.append(Text.literal(" | ").formatted(Formatting.RESET).formatted(Formatting.WHITE));
					the_new_effect.append(generateTotalTextLine(total_appraise_value_v3, cached_charm.appraised_v3_scores.get(i)));
					the_new_effect.append(Text.literal(" ("+cached_charm.appraised_v3_names.get(i)+")").formatted(Formatting.RESET).formatted(Formatting.WHITE));
				}
				
				temp_list.add(1,the_new_effect);
				
			}else {
				cached_charm.generateV2_5Score(false);
				cached_charm.generateV2_5Score(true);
			
				total_appraise_value = String.format("%.2f", cached_charm.apprasied_v2_5_score).replace(',','.');
				total_appraise_value_v3 = String.format("%.2f", cached_charm.appraised_v3_score).replace(',','.');
			
				MutableText the_new_effect = Text.literal("").append(generateTotalTextLine(total_appraise_value, cached_charm.apprasied_v2_5_score));
			
				temp_list.add(1,the_new_effect.append(Text.literal(" | ").formatted(Formatting.RESET).formatted(Formatting.WHITE)).append(generateTotalTextLine(total_appraise_value_v3, cached_charm.appraised_v3_score)));
			}
			//temp_list.add(1,Text.literal("["+total_appraise_value+"]").formatted(Formatting.WHITE));
		}else {
			temp_list.add(1,Text.literal("[??] | [??]").formatted(Formatting.GRAY));
		}
		
		gen_text_list.clear();
		
		for(Text temp_text : temp_list) {
			gen_text_list.add(temp_text);
		}
		
	}
	
	
	/*private static Text generateRainbow(String modifier_roll, String effect_string, Style effectStyle) {
		
		MutableText newEffectString = Text.literal(effect_string + " ").setStyle(effectStyle);
		
		//rainbowSetup
		for (int i = 0; i < modifier_roll.length(); i++) {
			
			newEffectString.append(modifier_roll.charAt(i)+"").formatted(rainbowSetup[(rainbowCounter+i)%10]).formatted(Formatting.BOLD);
			
		}
		
		rainbowCounter++;
		
		rainbowCounter = rainbowCounter%10;
		
		return newEffectString;
	}*/
	
	
	@SuppressWarnings("resource")
	@Override
	public void onInitialize() 
	{
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution	    
		
		//if(!MinecraftClient.getInstance().getSession().getUsername().equals("minerdwarf222")) { int a = 5/0;}
		
		initializeCommands();
		
	    UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
	    	
	    	if(!world.isClient) return ActionResult.PASS;
	    	
	    	last_right_clicked =  hitResult.getBlockPos();
            
	    	anti_monu_flag = true;
	    	
            return ActionResult.PASS;
        });
	    
	    //ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
	    
	    	// Market Browser - Bazaar
	    //	DwarfHighlightMod.LOGGER.info(screen.getTitle().getString());
	    	
	    //});
	    
	    ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
	    	
	    	 //MinecraftClient client = MinecraftClient.getInstance();
	    	 
	    	 /*if(client.world.isClient && client.currentScreen.getTitle().getString().equals("Market Browser - Bazaar")) { 
	    		 
	    		 test_stack = stack;
	    		 
	    		 if (!bazaar_text.isEmpty()) {
	    			 lines.clear();
	    		 	for (Text t : bazaar_text) {
	    			 	lines.add(t);
	    		 	}
	    		 }
			}*/
	    	
	    	if(stack.getNbt() == null) return;
	    	
	    	/*if(show_charm_overflow && !stack.getNbt().contains("Monumenta") && stack.getNbt().contains("plain")
	    			&& stack.getNbt().getCompound("plain").contains("display")
	    			&& stack.getNbt().getCompound("plain").getCompound("display").contains("Name")
	    			&& stack.getNbt().getCompound("plain").getCompound("display").getString("Name").equals("Charm Effect Summary")) {
	    		
	    		if (update_charm_summary_lines == 2) {
	    			
	    			int offset = 0;
	    			
	    			if(lines.get(1).getString().equals("These Charms are currently disabled!")){
	    				offset = 1;
	    			}
	    			
	    			for(String key : charm_summary_overflow.keySet()) {
	    				
	    				if(charm_summary_overflow.get(key) == null){
	    					update_charm_summary_lines = 0;
	    					break;
	    				}
	    				
	    				int indx = charm_summary_overflow.get(key) + offset;
	    				
	    				String clear_key = key.substring(key.indexOf("|")+1);
	    				
	    				lines.set(indx, lines.get(indx).copy().append(Text.literal(clear_key).formatted(Formatting.GREEN)));
	    			}
	    			
	    			return;
	    		}else if(update_charm_summary_lines == 1) {
	    			return;
	    		}
	    		
	    		charm_summary_overflow.clear();
	    		charm_summary_maxes.clear();
	    		charm_summary_is_percent.clear();
	    		
	    		int offset = 0;
    			
    			if(lines.get(1).getString().equals("These Charms are currently disabled!")){
    				offset = -1;
    			}
	    		
	    		for(int i = 0; i < lines.size(); i++) {
	    			// LOGGER.warn(line.toString()+"\n");
	    			
	    			if(lines.get(i).getString().contains("(MAX)")) {
	    				String effect_line = lines.get(i).getString();
	    				String effect_name = effect_line.split(" : ")[0].replace("%", "").toLowerCase();
	    				String effect_value = effect_line.split(" : ")[1].replace(" (MAX)", "");
	    				
	    				boolean percent_value = effect_value.contains("%");
	    				
	    				effect_value = effect_value.replace("%", "").replace(",", ".");
	    				
	    				double effect_value_i = Double.parseDouble(effect_value);
	    				
	    				//LOGGER.warn("Adding to maxes: " + effect_name);
	    				
	    				charm_summary_maxes.put(effect_name, effect_value_i);   	
	    				charm_summary_is_percent.put(effect_name, percent_value);
	    				charm_summary_overflow.put(effect_name, i+offset);
	    				
	    			}
	    			
	    		}
	    		
	    		update_charm_summary_lines = 1;
	    		return;
	    	}
	    	
	    	update_charm_summary_lines = 0;
	    	*/
	    	if(!stack.getNbt().contains("Monumenta")) {
	    		cached_charm = null;
	    		return;
	    	}
	    	
	    	if(!stack.getNbt().getCompound("Monumenta").getString("Tier").equals("zenithcharm")){
	    		cached_charm = null;
	    		return;
	    	}
	    	
	    	if(!show_tool_tip) {
	    		
	    		if(!grab_tool_tip || unique_zenith_charms.containsKey(stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID"))) return;
		    	
		    	outputAllCharms(stack);
	    	}
	    	
	    	// It is a charm. LET US DO Sorcery!!
	    	
	    	if(cached_charm == null || cached_charm.UUID != stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID")
	    			|| (cached_charm.UUID != stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID") && cached_charm.rarity != stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getInt("DEPTHS_CHARM_RARITY"))) {
	    		convertToCharm(stack);
	    		generateNewLines(lines);
	    	}else if(((show_roll_value ^ old_roll_value) || force_reload_list) && cached_charm != null && cached_charm.UUID == stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID")) {
	    		convertToCharm(stack);
	    		old_roll_value = show_roll_value;
	    		force_reload_list = false;
	    		generateNewLines(lines);	    		
	    	}
	    	
	    	lines.clear();
	    	
	    	for(Text line_to_add : gen_text_list) {
	    		lines.add(line_to_add);
	    	}
	    	
	    	
	    	if(!grab_tool_tip || unique_zenith_charms.containsKey(stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID"))) return;
	    	
	    	if(have_pop_sounds) {
	    		
	    		if(changed_pop_sounds && cached_charm.apprasiable && cached_charm.apprasied_v2_5_score >= 2.0) {
	    			MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_LLAMA_CHEST, 1.0f, 0.8f);
	    		}else {
	    			MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_LLAMA_CHEST, 1.0f, 0.8f);
	    		}
	    	}
	    	
	    	outputAllCharms(stack);
	    	 
	    });
	    
	    test_and_create_files_dirs();
	    
	    grabGitUUIDList();
		
	    DwarfHighlighterModMenuIntegration.reloadConfig();
		
	}
}
