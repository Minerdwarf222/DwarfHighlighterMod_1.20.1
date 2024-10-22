package net.dwarfhighlight;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class DwarfHighlighterModMenuIntegration implements ModMenuApi {

	private static boolean current_value = false;
	private static boolean check_list_value = false;
	private static boolean check_chests_value = false;
	private static boolean check_tcl_list_value = false;
	
	private static Boolean check_zenith_charms = false;
	
	public static HashMap<String, Boolean> private_list_titles = new HashMap<String, Boolean>();
	public static HashMap<String, Boolean> guild_list_titles = new HashMap<String, Boolean>();
	
	private static int[] convert_currency = {64,0,0,0,64,0,0,0,64};
	
	//TODO: Put this everywhere.
	public static double default_effect_weight = 0.0;
	
	public static Boolean getToggleZenithValue () 
	{
		
		return check_zenith_charms;
		
	}
	
	
	public static void setToggleZenithValue (boolean value) 
	{
		
		check_zenith_charms = value;
		
	}
	
	
	public static Boolean getToggleValue () 
	{
		
		return current_value;
		
	}
	
	
	public static void setToggleValue (boolean value) 
	{
		
		current_value = value;
		
	}
	
	
	public static Boolean getcheckListValue () 
	{
		
		return check_list_value;
		
	}
	
	
	public static void setcheckListValue (boolean value) 
	{
		
		check_list_value = value;
		
	}
	
	
	public static Boolean getcheckTCLListValue () 
	{
		
		return check_tcl_list_value;
		
	}
	
	
	public static void setcheckTCLListValue (boolean value) 
	{
		
		check_tcl_list_value = value;
		
	}
	
	
	public static Boolean getcheckChestsValue () 
	{
		
		return check_chests_value;
		
	}
	
	
	public static String getTypeOfList (boolean for_private_list) 
	{
		
		if(for_private_list) {
			
			return "p";
			
		}else {
			
			return "g";
			
		}
		
	}
	
	
	public static void updateList(boolean for_private_list) {
		
		String temp_filename = "";
		String filename = "";
		FileWriter temp_file_writer; 
		
		HashMap<String, List<String>> list_being_updated;
		
		if(for_private_list) {
			temp_filename = DwarfHighlightMod.top_level_dir + "DwarfHighlighterList_temp.txt";
			filename = DwarfHighlightMod.private_rare_list_file;
			check_list_value = true;
			list_being_updated = DwarfHighlightMod.needed_personal_items;
		}else {
			temp_filename = DwarfHighlightMod.top_level_dir + "DwarfHighlighterTCLList_temp.txt";
			filename = DwarfHighlightMod.tcl_rare_list_file;
			check_tcl_list_value = true;
			list_being_updated = DwarfHighlightMod.needed_tcl_items;
		}
		
		File temp_file = new File(temp_filename);
		File old_file = new File(filename);
		
		try {
			
			if(temp_file.createNewFile()) {
				
				DwarfHighlightMod.LOGGER.info("Created Temp List File.");
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
		try {
			BufferedReader reader = Files.newBufferedReader(Paths.get(filename));
			
			temp_file_writer = new FileWriter(temp_filename);
			
			BufferedWriter temp_write = new BufferedWriter(temp_file_writer);
			
			String input_line = "";
			
			String current_title = "";
			
			boolean ignore_title = false;
			
			List<String> items_in_title = new ArrayList<String>();
			
			while ((input_line = reader.readLine()) != null) {
				
				//System.out.println(input_line);
				
				//Just write line if comment, skip, or empty
				if(input_line.length()==0) { 
					if(items_in_title.size() == 0) {
						temp_write.newLine();
						continue;
					}
					for(String item_name : items_in_title) {
									
						int first_space = item_name.indexOf(' ');
										
						if(first_space!=-1) {
							String potential_region = item_name.substring(0, first_space);
							switch(potential_region) {
											
								case ("ring"):
										item_name = "3" + item_name.substring(first_space);
										break;
												
								case ("isles"):
									item_name = "2" + item_name.substring(first_space);
									break;
												
								case ("valley"):
									item_name = "1" + item_name.substring(first_space);
									break;
												
								default:
									break;
											
							}
												
						}
									
						if(Integer.parseInt(list_being_updated.get(item_name).get(0))<=0) {
							temp_write.write(item_name);
						}else {
							temp_write.write(item_name + ";" + list_being_updated.get(item_name).get(0));
						}
						temp_write.newLine();
					}
				}
				
				if(input_line.charAt(0) == '#' || input_line.charAt(0) == '*') { temp_write.write(input_line); temp_write.newLine(); continue;}
				
				input_line = input_line.toLowerCase();
				
				//Check what current title state is and write it.
				if(input_line.charAt(0) == '!' || input_line.charAt(0) == '%') {
					
					if(input_line.charAt(0) == '!') ignore_title = false;
					if(input_line.charAt(0) == '%') ignore_title = true;
					
					String title_name = input_line.substring(1);
					current_title = title_name;
					if(for_private_list && private_list_titles.containsKey(title_name)) {
						if(private_list_titles.get(title_name)) {
							temp_write.write("%"+title_name);
							temp_write.newLine();
						}else {
							temp_write.write("!"+title_name);
							temp_write.newLine();
						}
					}else if(!for_private_list && guild_list_titles.containsKey(title_name)) {
						if(guild_list_titles.get(title_name)) {
							temp_write.write("%"+title_name);
							temp_write.newLine();
						}else {
							temp_write.write("!"+title_name);
							temp_write.newLine();
						}
					}else {
						temp_write.write(input_line);
						temp_write.newLine();
						continue;
					}
					
					items_in_title.clear();
					
					for(String item_name : list_being_updated.keySet()){
						if(list_being_updated.get(item_name).contains(current_title)) {
							items_in_title.add(item_name);
						}
					}
					continue;
				}
				
				if(ignore_title) {
					temp_write.write(input_line);
					temp_write.newLine();
					continue;
				}
				
				String old_item = input_line;
				
				switch(input_line.charAt(0)) {
				
				case('3'):
					
					input_line = "ring" + input_line.substring(1); //Monumenta -> Region
					break;
					
				case('2'):
					
					input_line = "isles" + input_line.substring(1);
					break;
					
				case('1'):
					
					input_line = "valley" + input_line.substring(1);
					break;
					
				default:
					
					break;
					
				}
				
				int semicolon_index = input_line.indexOf(';');
				
				if (semicolon_index == -1) {
					if(!list_being_updated.containsKey(input_line)) {
						System.out.println("EY");
						continue;
					}else {
						if(Integer.parseInt(list_being_updated.get(input_line).get(0))<=0) {
							temp_write.write(old_item);
						}else {
							temp_write.write(old_item + ";" + list_being_updated.get(input_line).get(0));
						}
						temp_write.newLine();
						items_in_title.remove(input_line);
					}
				} else {
					
					String itemname = input_line.substring(0, semicolon_index);
					
					int semicolon_index_old = old_item.indexOf(';');
					String old_item_name = old_item.substring(0, semicolon_index_old);
					
					if(!list_being_updated.containsKey(itemname)) {
						continue;
					}else {
						if(Integer.parseInt(list_being_updated.get(itemname).get(0))<=0) {
							temp_write.write(old_item_name);
						}else {
							temp_write.write(old_item_name + ";" + list_being_updated.get(itemname).get(0));
						}
						temp_write.newLine();
						items_in_title.remove(itemname);
					}
				}
				
			}
			
			temp_write.close();
			reader.close();
			temp_file_writer.close();
			
		} catch (FileNotFoundException e) {
			
			DwarfHighlightMod.LOGGER.error(filename + " File Not Found.");
			
		} catch (IOException e) {
			
			DwarfHighlightMod.LOGGER.error("Some IOException.");
			e.printStackTrace();
			
		}
		
		old_file.delete();
		temp_file.renameTo(old_file);
		reloadLists();
	}
	
	
	private static void runConfigupdate(boolean for_private_list) {
		
		String filename = "";
		
		if(for_private_list) {
			
			setcheckListValue(true);
			filename = DwarfHighlightMod.private_rare_list_file;
			private_list_titles.clear();
			
		}else {
			
			setcheckTCLListValue(true);
			filename = DwarfHighlightMod.tcl_rare_list_file;
			guild_list_titles.clear();
			
		}
		
		boolean skip_lines = false;
		List<List<String>> needed_items_update = new ArrayList<List<String>>();
		
		try {
			
			BufferedReader reader = Files.newBufferedReader(Paths.get(filename));
			String input_line = "";
			
			String current_title = "";
			
			while ((input_line = reader.readLine()) != null) {

				if(input_line.length()==0) {skip_lines = false; continue;}
				if(input_line.charAt(0)=='#') {continue;}
				
				input_line = input_line.toLowerCase();
				
				if(input_line.charAt(0) == '!' || input_line.charAt(0)=='%') {
					
					boolean skip_title = input_line.charAt(0) == '%';
					
					current_title = input_line.substring(1);
					
					if(for_private_list) {
						private_list_titles.put(current_title, skip_title);
					}else {
						guild_list_titles.put(current_title, skip_title);
					}
					
					skip_lines = skip_title;
					
					continue;
				}
				
				if(skip_lines) continue;
				
				if(input_line.charAt(0)=='*') skip_lines = true;
				
				switch(input_line.charAt(0)) {
				
				case('3'):
					
					input_line = "ring" + input_line.substring(1); //Monumenta -> Region
					break;
					
				case('2'):
					
					input_line = "isles" + input_line.substring(1);
					break;
					
				case('1'):
					
					input_line = "valley" + input_line.substring(1);
					break;
					
				default:
					
					break;
					
				}
				
				int semicolon_index = input_line.indexOf(';');
				
				if (semicolon_index == -1) {
					
					List<String> temp_list = new ArrayList<String>();
					
					temp_list.add(input_line);
					temp_list.add("-1");
					temp_list.add(getTypeOfList(for_private_list));
					temp_list.add(current_title);
					
					needed_items_update.add(temp_list);
					
					continue;
					
				}else {
					
					String item_name = input_line.substring(0,semicolon_index);
					
					if(input_line.length() == semicolon_index + 1) {
						
						if (item_name.isEmpty()) continue;
						
						List<String> temp_list = new ArrayList<String>();
						
						temp_list.add(item_name);
						temp_list.add("-1");
						temp_list.add(current_title);
						temp_list.add(getTypeOfList(for_private_list));
						
						needed_items_update.add(temp_list);
						continue;
						
					}
					
					String check_qty = input_line.substring(semicolon_index+1);
					String found_qty = "";
					
					for (int i = 0; i <check_qty.length(); i++) {
						
						String testing_char = check_qty.substring(i,i+1);
						
						if(testing_char.matches("\\d")) {
							
							found_qty = found_qty + testing_char;
							
						}else {
							
							break;
							
						}
						
					}
					
					if(found_qty.isEmpty()) {
						
						if(input_line.length() == semicolon_index + 1) {
							
							if (item_name.isEmpty()) continue;
							
							List<String> temp_list = new ArrayList<String>();
							
							temp_list.add(item_name);
							temp_list.add("-1");
							temp_list.add(getTypeOfList(for_private_list));
							temp_list.add(current_title);
							
							needed_items_update.add(temp_list);
							
							continue;
							
						}
						
					}
					
					List<String> temp_list = new ArrayList<String>();
					
					temp_list.add(item_name);
					temp_list.add(""+Integer.parseInt(found_qty));
					temp_list.add(getTypeOfList(for_private_list));
					temp_list.add(current_title);
					
					needed_items_update.add(temp_list);
				}
			}
			
			reader.close();
			
			if(for_private_list) {
				
				DwarfHighlightMod.setneededPersonalItems(needed_items_update);
				setcheckListValue(false);
				
			}else {
				
				DwarfHighlightMod.setneededTCLItems(needed_items_update);
				setcheckTCLListValue(false);
				
			}
		} catch (FileNotFoundException e) {
			
			DwarfHighlightMod.LOGGER.error(filename + " File Not Found.");
			
		} catch (IOException e) {
			
			DwarfHighlightMod.LOGGER.error("Some IOException.");
			e.printStackTrace();
			
		}
		
		reloadLists();
	}
	
	
	public static String conversionComparer(int going_from, int going_to, int inter_trade) {
		
		String[] currency_dicitionary = {"Hxp", "Hcs", "Har"};
		String[] currency_dictionary_comp = {"cxp", "ccs", "ar"};
		
		// 0, 1, 2
		// 3, 4, 5
		// 6, 7, 8
		
	    //r1valuedivider = int(hxp_ccs) + int(hxp_ar)
	    //r1value = r1valuedivider / 2
		//r1valuemiddle = r1value /64
		
	    /*#HXP to ccs. Conventinally and Unconventinally (COMPLETED)
	    print('HXP to CCS: ')
	    print('1 HXP = ' + str(r1value))
	   
	    r1tor2thing = float(r2valuemiddle) * float(hxp_ccs)
	   
	    print('Conventional: '+str(hxp_ccs) + (' (') +str(r1tor2thing)+(')') )
	    r1r2unconventionalmiddle = float(hxp_ar) / 64
	    r1r2unconventional = float(r1r2unconventionalmiddle) * float(har_ccs)
	   
	    r1tor2thing2 = float(r2valuemiddle) * float(r1r2unconventional)
	   
	    print('Unconventinal: ' + str(r1r2unconventional) + (' (') +str(r1tor2thing2)+(')') )
	    print('Reinard Strand: '+str(strand_cxp_tax))*/
		
		//int tovaluedivider = convert_currency[going_to*3 + going_from] + convert_currency[going_to*3 + inter_trade];
		//double tovalue = (double)(tovaluedivider)/2.0;
		//double tovaluemiddle = tovalue/64;
		
		double to_conventional = (double)(convert_currency[going_from*3+going_to]);
		double to_unconventional_middle = (double)(convert_currency[going_from*3+inter_trade])/64.0;
		double to_unconventional = to_unconventional_middle * (double)(convert_currency[inter_trade*3+going_to]);
		
		if(to_conventional >= to_unconventional) {
			return currency_dicitionary[going_from] + " -> " + currency_dictionary_comp[going_to] +" (" + to_conventional + ").";
		}else {
			return currency_dicitionary[going_from] + " -> " + currency_dictionary_comp[inter_trade] +"; " + currency_dicitionary[inter_trade] + " -> "+ currency_dictionary_comp[going_to] +" (" + to_unconventional + ").";
		}
	}
	
	
	private void updateConfigFile() {
		
		List<String> lines = new ArrayList<String>();
		
		for(int i : convert_currency) {
			lines.add(""+i);
		}
		
		String menu_config = (current_value?"1":"0")
			+ (DwarfHighlightMod.container_checkpointing?"1":"0")
			+ (DwarfHighlightMod.show_roll_value?"1":"0")
			+ (check_zenith_charms?"1":"0")
			+ (DwarfHighlightMod.grab_tool_tip?"1":"0")
			+ (DwarfHighlightMod.grab_from_chat?"1":"0")
			+ (DwarfHighlightMod.show_tool_tip?"1":"0")
			+ (DwarfHighlightMod.have_pop_sounds?"1":"0")
			+ (DwarfHighlightMod.show_charm_grab_msg?"1":"0")
			+ (DwarfHighlightMod.changed_pop_sounds?"1":"0")
			+ (DwarfHighlightMod.show_chat_message_if_highlighted_items?"1":"0")
			+ (DwarfHighlightMod.show_sirius?"1":"0")
			+ (DwarfHighlightMod.show_charm_overflow?"1":"0")
			+ (DwarfHighlightMod.show_paradox?"1":"0")
			+ (DwarfHighlightMod.show_all_profiles?"1":"0");
		
		lines.add(menu_config);
		
		lines.add((Charm.balance_div[0]+"|"+Charm.balance_div[1]+"|"+Charm.balance_div[2]+"|"+Charm.balance_div[3]+"|"+Charm.balance_div[4]).replace(",", "."));
		
		lines.add((""+default_effect_weight).replace(",", "."));
		
		Path file = Paths.get(DwarfHighlightMod.config_file);
		
		try {
			Files.write(file, lines);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void reloadConfig() {
		
		try {
			
			BufferedReader reader = Files.newBufferedReader(Paths.get(DwarfHighlightMod.config_file));
			String input_line = "";
			int counter = 0;
					
			while ((input_line = reader.readLine()) != null) {
				
				/*if(!isInteger(input_line)) {
					DwarfHighlightMod.LOGGER.error("DwarfHighlighterConfig.txt has a non-int. "+input_line);
					break;
				}*/
				
				if(counter < 9) convert_currency[counter] = Integer.parseInt(input_line);
				
				if(counter == 9) {
					
					//Binary string where 0 = false, 1 = true;
					switch(input_line.length()) {
					case(15):
						DwarfHighlightMod.show_all_profiles = input_line.charAt(14) == '1';
					case(14):
						DwarfHighlightMod.show_paradox = input_line.charAt(13) == '1';
					case(13):
						DwarfHighlightMod.show_charm_overflow = input_line.charAt(12) == '1';
					case(12):
						DwarfHighlightMod.show_sirius = input_line.charAt(11) == '1';
					case(11):
						DwarfHighlightMod.show_chat_message_if_highlighted_items = input_line.charAt(10) == '1';
					case(10):
						DwarfHighlightMod.changed_pop_sounds = input_line.charAt(9) == '1';
					case(9):
						DwarfHighlightMod.show_charm_grab_msg = input_line.charAt(8) == '1';
					case(8):
						DwarfHighlightMod.have_pop_sounds = input_line.charAt(7) == '1';
					case(7):
						DwarfHighlightMod.show_tool_tip = input_line.charAt(6) == '1';
					case(6):
						DwarfHighlightMod.grab_from_chat = input_line.charAt(5) == '1';
					case(5):
						DwarfHighlightMod.grab_tool_tip = input_line.charAt(4) == '1';
					case(4):
						check_zenith_charms = input_line.charAt(3) == '1';
					case(3):
						DwarfHighlightMod.show_roll_value = input_line.charAt(2) == '1';
					case(2):
						DwarfHighlightMod.container_checkpointing = input_line.charAt(1) == '1';
					case(1):
						current_value = input_line.charAt(0) == '1';
					}
					
				}
				
				if(counter == 10) {
					// Should be a string (double)|(double)|(double)|(double)|(double)
					
					if(input_line.isEmpty()) {
						counter++;
						continue;
					}
					
					String[] power_vals = input_line.split("\\|");
					
					//DwarfHighlightMod.LOGGER.warn(input_line);
					
					/*for (String test : power_vals) {
						DwarfHighlightMod.LOGGER.warn(test);
					}*/
					
					Charm.balance_div[0] = Double.valueOf(power_vals[0]);
					Charm.balance_div[1] = Double.valueOf(power_vals[1]);
					Charm.balance_div[2] = Double.valueOf(power_vals[2]);
					Charm.balance_div[3] = Double.valueOf(power_vals[3]);
					Charm.balance_div[4] = Double.valueOf(power_vals[4]);
					
				}
				
				if(counter == 11) {
					if(input_line.isEmpty()) {
						counter++;
						continue;
					}
					default_effect_weight = Double.valueOf(input_line);
				}
				
				counter++;
			}
			
		} catch (FileNotFoundException e) {
			
			DwarfHighlightMod.LOGGER.error("DwarfHighlighterConfig.txt" + " File Not Found.");
			
		} catch (IOException e) {
			
			DwarfHighlightMod.LOGGER.error("Some IOException.");
			e.printStackTrace();
			
		}
		
	}
	
	public static void reloadLists() {
		if(getToggleValue()) {
			boolean update_list = false;
			
			if(getcheckListValue()) {
				
				update_list = true;
				runConfigupdate(true);
				
			}
			
			if(getcheckTCLListValue()) {
				
				update_list = true;
				runConfigupdate(false);
				
			}
			
			if(update_list) {
				
				DwarfHighlightMod.mergeItemLists();
				
			}
			
		}
		check_list_value = false;
		check_tcl_list_value = false;
	}
	
	
	public static void updateEffectHighlight(String effect, boolean highlight) {
		
		//DwarfHighlightMod.charm_maxes.charm_maxes.get(effect).set(2, highlight_double);
		
		if(!DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).has(effect)) {
			
			JsonObject temp_object = new JsonObject();
			temp_object.addProperty("weight", default_effect_weight);
			temp_object.addProperty("highlight", false);
			
			DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).add(effect, temp_object);
		}
		
		DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).getAsJsonObject(effect).remove("highlight");
		DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).getAsJsonObject(effect).addProperty("highlight", highlight);
		
		if(DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).getAsJsonObject(effect).get("weight").getAsDouble() == default_effect_weight && (!highlight)) {
			DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).remove(effect);
		}
	}
	
	
	public static void updateEffectWeight(String effect, Double weight) {
		
		if(!DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).has(effect)) {
			
			JsonObject temp_object = new JsonObject();
			temp_object.addProperty("weight", default_effect_weight);
			temp_object.addProperty("highlight", false);
			
			DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).add(effect, temp_object);
		}
		
		DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).getAsJsonObject(effect).remove("weight");
		DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).getAsJsonObject(effect).addProperty("weight", weight);
	
		
		if(weight == default_effect_weight && !DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).getAsJsonObject(effect).get("highlight").getAsBoolean()) {
			DwarfHighlightMod.json_weights.getAsJsonObject(DwarfHighlightMod.json_weights.get("enabled").getAsString()).remove(effect);
		}
		
		//DwarfHighlightMod.charm_maxes.charm_maxes.get(effect).set(1, weight);
	}
	
	
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return parent -> {
			ConfigBuilder builder = ConfigBuilder.create()
					.setParentScreen(parent)
					.setTitle(Text.of("Dwarf's Highlighter Config"));
			
			builder.setSavingRunnable(() -> {
				
				DwarfHighlightMod.saveCharmJson();
				
				updateConfigFile();
				
				reloadLists();
				
			});
			
			
			ConfigEntryBuilder entry_builder = builder.entryBuilder();
			
			// Zenith Charm specific 
			ConfigCategory general = builder.getOrCreateCategory(Text.of("Zenith Charm"));	
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Flat Rolls: "), DwarfHighlightMod.show_roll_value)
					.setDefaultValue(false)
					.setTooltip(Text.of("Switches charm tooltips to be based on the actual roll instead of max."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.show_roll_value = new_value)
					.build());
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Show Tooltip Changes: "), DwarfHighlightMod.show_tool_tip)
					.setDefaultValue(false)
					.setTooltip(Text.of("Switches charm tooltips to show the additional information."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.show_tool_tip = new_value)
					.build());
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Zcharm Chest/Barrel Grab: "), check_zenith_charms)
					.setDefaultValue(false)
					.setTooltip(Text.of("Outputs Zenith Charms in NBT format from barrels & chests if toggled."))
					.setSaveConsumer(new_value -> check_zenith_charms = new_value)
					.build());
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Zcharm Tooltips Grab: "), DwarfHighlightMod.grab_tool_tip)
					.setDefaultValue(false)
					.setTooltip(Text.of("Turns on grabbing charm data via tooltips."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.grab_tool_tip = new_value)
					.build());
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Zcharm Chat Grab: "), DwarfHighlightMod.grab_from_chat)
					.setDefaultValue(false)
					.setTooltip(Text.of("Turns on grabbing charm data from chat."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.grab_from_chat = new_value)
					.build());
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Pop Sounds: "), DwarfHighlightMod.have_pop_sounds)
					.setDefaultValue(true)
					.setTooltip(Text.of("Turns on/off pop sound when grabbing a charm."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.have_pop_sounds = new_value)
					.build());
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Charm Chat Message: "),  DwarfHighlightMod.show_charm_grab_msg)
					.setDefaultValue(true)
					.setTooltip(Text.of("Turns on/off the chat message when you grab a charm."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.show_charm_grab_msg = new_value)
					.build());
			
			general.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Changed Pop Sound: "), DwarfHighlightMod.changed_pop_sounds)
					.setDefaultValue(false)
					.setTooltip(Text.of("Turns on/off the cha-ching sound for charms with 2.0 or higher apprasial value."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.changed_pop_sounds = new_value)
					.build());
			
			// Charm config for values
			ConfigCategory charm_config = builder.getOrCreateCategory(Text.of("Charm Values"));
			
			charm_config.addEntry(entry_builder.startDoubleField(Text.of("Change weight of charms with power 1: "), Charm.balance_div[0])
					.setDefaultValue(1.0)
					.setTooltip(Text.of("The value a charm is divided by if it has a power of 1."))
					.setSaveConsumer(new_value -> Charm.balance_div[0] = new_value)
					.build());
			
			charm_config.addEntry(entry_builder.startDoubleField(Text.of("Change weight of charms with power 2: "), Charm.balance_div[1])
					.setDefaultValue(2.0)
					.setTooltip(Text.of("The value a charm is divided by if it has a power of 2."))
					.setSaveConsumer(new_value -> Charm.balance_div[1] = new_value)
					.build());
			
			charm_config.addEntry(entry_builder.startDoubleField(Text.of("Change weight of charms with power 3: "), Charm.balance_div[2])
					.setDefaultValue(3.0)
					.setTooltip(Text.of("The value a charm is divided by if it has a power of 3."))
					.setSaveConsumer(new_value -> Charm.balance_div[2] = new_value)
					.build());
			
			charm_config.addEntry(entry_builder.startDoubleField(Text.of("Change weight of charms with power 4: "), Charm.balance_div[3])
					.setDefaultValue(4.0)
					.setTooltip(Text.of("The value a charm is divided by if it has a power of 4."))
					.setSaveConsumer(new_value -> Charm.balance_div[3] = new_value)
					.build());
			
			charm_config.addEntry(entry_builder.startDoubleField(Text.of("Change weight of charms with power 5: "), Charm.balance_div[4])
					.setDefaultValue(5.0)
					.setTooltip(Text.of("The value a charm is divided by if it has a power of 5."))
					.setSaveConsumer(new_value -> Charm.balance_div[4] = new_value)
					.build());
			
			// Charm Profile stuff
			ConfigCategory charm_profile_settings = builder.getOrCreateCategory(Text.of("Charm Profile"));	
			
			charm_profile_settings.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Showing Profile values: "), DwarfHighlightMod.show_all_profiles)
					.setDefaultValue(false)
					.setTooltip(Text.of("Switches charm tooltip to show every profile above their cutoff values in descending order."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.show_all_profiles = new_value)
					.build());
			
			charm_profile_settings.addEntry(entry_builder.startDoubleField(Text.of("Default effect weight: "), default_effect_weight)
					.setDefaultValue(DwarfHighlighterModMenuIntegration.default_effect_weight)
					.setTooltip(Text.of("Sets the default weight for effects.."))
					.setSaveConsumer(new_value -> default_effect_weight = new_value)
					.build());
			
			for(String profile_name : DwarfHighlightMod.json_weights.keySet()) {
				if (DwarfHighlightMod.json_weights.get(profile_name).isJsonPrimitive()) continue;
				
				charm_profile_settings.addEntry(entry_builder.startDoubleField(Text.of("Cutoff value for " + profile_name + " : "), DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).get("cutoff").getAsDouble())
						.setDefaultValue(0.0)
						.setTooltip(Text.of("Sets the mininum value to show this profile's weighted sum."))
						.setSaveConsumer(new_value -> DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).addProperty("cutoff", new_value))
						.build());	
			}
			
			if(DwarfHighlightMod.json_weights == null || DwarfHighlightMod.json_weights.get("enabled") == null) {
				JsonObject test_obj;
				try {
					test_obj = JsonParser.parseString(Files.readString(Paths.get(DwarfHighlightMod.jsonCharmWeightFile))).getAsJsonObject();
					DwarfHighlightMod.json_weights = test_obj.deepCopy();
				} catch (JsonSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			String enabled_profile = DwarfHighlightMod.json_weights.get("enabled").getAsString();
			
			for(String effect : DwarfHighlightMod.charm_maxes.charm_maxes.keySet()) {
				
				if(DwarfHighlightMod.json_weights.getAsJsonObject(enabled_profile).has(effect)) {
					
					JsonObject profile_effect = DwarfHighlightMod.json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(effect);
					
					charm_profile_settings.addEntry(entry_builder.startDoubleField(Text.of(effect + "'s weight: "), profile_effect.get("weight").getAsDouble())
							.setDefaultValue(DwarfHighlighterModMenuIntegration.default_effect_weight)
							.setTooltip(Text.of("Sets the weight for this effect for the enabled profile ("+enabled_profile+")."))
							.setSaveConsumer(new_value -> updateEffectWeight(effect, new_value))
							.build());
					
					charm_profile_settings.addEntry(entry_builder.startBooleanToggle(Text.of(effect + "'s highlight: "), profile_effect.get("highlight").getAsBoolean())
							.setDefaultValue(false)
							.setTooltip(Text.of("Toggles the effect being highlighted for the enabled profile ("+enabled_profile+")."))
							.setSaveConsumer(new_value -> updateEffectHighlight(effect, new_value))
							.build());
				}else {
					charm_profile_settings.addEntry(entry_builder.startDoubleField(Text.of(effect + "'s weight: "), DwarfHighlighterModMenuIntegration.default_effect_weight)
							.setDefaultValue(DwarfHighlighterModMenuIntegration.default_effect_weight)
							.setTooltip(Text.of("Sets the weight for this effect for the enabled profile ("+enabled_profile+")."))
							.setSaveConsumer(new_value -> updateEffectWeight(effect, new_value))
							.build());
					
					charm_profile_settings.addEntry(entry_builder.startBooleanToggle(Text.of(effect + "'s highlight: "), false)
							.setDefaultValue(false)
							.setTooltip(Text.of("Toggles the effect being highlighted for the enabled profile ("+enabled_profile+")."))
							.setSaveConsumer(new_value -> updateEffectHighlight(effect, new_value))
							.build());
				}
				
			}
			
			// Container Checker Toggles
			ConfigCategory container_checker = builder.getOrCreateCategory(Text.of("Container Checker"));	
			
			container_checker.addEntry(entry_builder.startBooleanToggle(Text.of("Turn on item finder: "), current_value)
					.setDefaultValue(false)
					.setTooltip(Text.of("Turns on the chest scanner."))
					.setSaveConsumer(new_value -> current_value = new_value)
					.build());
			
			container_checker.addEntry(entry_builder.startBooleanToggle(Text.of("Reload Personal List: "), check_list_value)
					.setDefaultValue(false)
					.setTooltip(Text.of("Reloads Personal list when saved."))
					.setSaveConsumer(new_value -> check_list_value = new_value)
					.build());
			
			container_checker.addEntry(entry_builder.startBooleanToggle(Text.of("Reload TCL List: "), check_list_value)
					.setDefaultValue(false)
					.setTooltip(Text.of("Reloads TCL list when saved."))
					.setSaveConsumer(new_value -> check_tcl_list_value = new_value)
					.build());
			
			container_checker.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Items Chat Message: "), DwarfHighlightMod.show_chat_message_if_highlighted_items)
					.setDefaultValue(true)
					.setTooltip(Text.of("Toggles on/off the chat message listing every item with in container / needed."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.show_chat_message_if_highlighted_items = new_value)
					.build());
			
			container_checker.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Checkpointing: "), DwarfHighlightMod.container_checkpointing)
					.setDefaultValue(false)
					.setTooltip(Text.of("Toggles checkpointing."))
					.setSaveConsumer(new_value -> DwarfHighlightMod.container_checkpointing = new_value)
					.build());
			
			// Boss Bar Toggles
			ConfigCategory boss_bar_cat = builder.getOrCreateCategory(Text.of("Boss Bars"));	
			
			boss_bar_cat.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Sirius: "), DwarfHighlightMod.show_sirius)
					.setDefaultValue(false)
					.setTooltip(Text.of("Toggles the changes to Sirius Boss Bars."))
					.setSaveConsumer(new_value ->  DwarfHighlightMod.show_sirius = new_value)
					.build());
			
			boss_bar_cat.addEntry(entry_builder.startBooleanToggle(Text.of("Toggle Paradox Tracker: "), DwarfHighlightMod.show_paradox)
					.setDefaultValue(false)
					.setTooltip(Text.of("Toggles the add Paradox Boss Bars."))
					.setSaveConsumer(new_value ->  DwarfHighlightMod.show_paradox = new_value)
					.build());
			
			// Currency Converter Toggles
			ConfigCategory currency_conv = builder.getOrCreateCategory(Text.of("Currency Convertor"));	
			
			// Left Barrel
			
			currency_conv.addEntry(entry_builder.startIntField(Text.of("Har -> ccs: "), convert_currency[7])
					.setDefaultValue(0)
					.setTooltip(Text.of("Stonkco's current 1 Hxp -> X Ccs trade."))
					.setSaveConsumer(new_value -> convert_currency[7] = new_value)
					.build());
			
			currency_conv.addEntry(entry_builder.startIntField(Text.of("Hcs -> ar: "), convert_currency[5])
					.setDefaultValue(0)
					.setTooltip(Text.of("Stonkco's current 1 Hcs -> X Ar trade."))
					.setSaveConsumer(new_value -> convert_currency[5] = new_value)
					.build());
			
			// Middle Barrel
			
			currency_conv.addEntry(entry_builder.startIntField(Text.of("Hcs -> cxp: "), convert_currency[3])
					.setDefaultValue(0)
					.setTooltip(Text.of("Stonkco's current 1 Hcs -> X Cxp trade."))
					.setSaveConsumer(new_value -> convert_currency[3] = new_value)
					.build());
			
			currency_conv.addEntry(entry_builder.startIntField(Text.of("Hxp -> ccs: "), convert_currency[1])
					.setDefaultValue(0)
					.setTooltip(Text.of("Stonkco's current 1 Hxp -> X Ccs trade."))
					.setSaveConsumer(new_value -> convert_currency[1] = new_value)
					.build());
			
			// Right Barrel
			
			currency_conv.addEntry(entry_builder.startIntField(Text.of("Har -> cxp: "), convert_currency[6])
					.setDefaultValue(0)
					.setTooltip(Text.of("Stonkco's current 1 Har -> X Cxp trade."))
					.setSaveConsumer(new_value -> convert_currency[6] = new_value)
					.build());
			
			currency_conv.addEntry(entry_builder.startIntField(Text.of("Hxp -> ar: "), convert_currency[2])
					.setDefaultValue(0)
					.setTooltip(Text.of("Stonkco's current 1 Hxp -> X Ar trade."))
					.setSaveConsumer(new_value -> convert_currency[2] = new_value)
					.build());
			
			Screen screen = builder.build();
			return screen;
		};
	}
	
}
