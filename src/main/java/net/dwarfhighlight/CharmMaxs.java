package net.dwarfhighlight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CharmMaxs {

	public HashMap<String, List<Double>> charm_maxes = new HashMap<String, List<Double>>();
	public HashMap<String, String> effects_to_ability = new HashMap<String, String>();
	
	// Ability Name is key
	// Value is list of integer [trigger, tree]
	// not active slot then 0.
	public HashMap<String, List<Integer>> ability_props = new HashMap<String, List<Integer>>();
	
	private String file_path = "";
	
	public static String get_version = "1.2.2";
	
	public CharmMaxs(String top_level_dir) {
		
		// Add a file parser.
		file_path = top_level_dir+"/charm_maximums";
		addAbilities();
		generateEffectToAbility();
		addBuiltInMaxes();	
		readFromFile();
		readWeightsFromFile();
	}
	
	// TODO: Add that automatically removes all default_weight and not highlighted effects.
	public void readWeightsFromFile () {
		
		File json_weights_file = new File(DwarfHighlightMod.jsonCharmWeightFile);
		
		if(!json_weights_file.isFile()) {
			return;			
		}
		
		try {
			
			JsonObject test_obj = JsonParser.parseString(Files.readString(Paths.get(DwarfHighlightMod.jsonCharmWeightFile))).getAsJsonObject();
			
			DwarfHighlightMod.json_weights = test_obj.deepCopy();
		
		} catch (IOException e) {
		
			DwarfHighlightMod.LOGGER.error("Could not find or read json file for charm profiles.");
		
		}
		
	}

	public void reloadCharmWeights() {
		
		charm_maxes.clear();
		
		addBuiltInMaxes();
		readFromFile();
	}
	
	public void readFromFile () {
		
		File test_if_config = new File(file_path);
		
		if(!test_if_config.exists()) {
			
			return;
			
		}
		
		try {
			
			FileReader fr = new FileReader(file_path);
			BufferedReader reader = new BufferedReader(fr);
			String input_line = "";
			
			boolean first_line = true;			
			
			while ((input_line = reader.readLine()) != null) {
				
				if(input_line.length() == 0) { continue; }
				
				// Checking if file is up to date to the mod version. If it isn't then ignore.
				if(first_line && !get_version.equals(input_line)) {
					break;
				}else {
					first_line = false;
				}				
				
				input_line = input_line.toLowerCase();
				
				//System.out.println(input_line);
				
				String[] effect_an_value = input_line.split(", ");
				
				/*
				 * New form:
				 * [Type], [Fields]
				 * Type = Some number that represents what the override is.
				 * 0 = Updating existing effect's charm maximum
				 * 1 = Adding an effect to an ability
				 * 2 = Creating an ability
				 * 3 = Removing an effect
				 * 4 = Removing an ability
				 * 5 = Changing ability trigger
				 * 6 = Changing ability tree
				 * 7 = Renaming ability
				 * 8 = Rename Effect
				 * 
				 * Fields:
				 * 0: Effect Name, New Value
				 * 1: Effect Name, Ability Name
				 * 2: Ability Name, Trigger, Tree
				 * 3: Effect Name
				 * 4: Ability Name
				 * 5: Ability Name, New Trigger
				 * 6: Ability Name, New Tree
				 * 7: Ability Name, New Ability Name
				 * 8: Effect Name, New Effect Name
				 */
				
				int over_len = effect_an_value.length;
				
				if(over_len <= 1 || over_len > 4) {
					continue;
				}
				
				switch(effect_an_value[0]) {
				case("0"):
					if(!charm_maxes.containsKey(effect_an_value[1])) {
						DwarfHighlightMod.LOGGER.warn("\"" + input_line + "\" the effect \"" + effect_an_value[1] + "\" does not exist in memory.");
						continue;
					}
					
					if (over_len != 3) {
						DwarfHighlightMod.LOGGER.warn("\"" + input_line + "\" there are not 3 args (0, effect name, new value)");
						continue;
					}
					charm_maxes.replace(effect_an_value[1],Arrays.asList(Double.parseDouble(effect_an_value[2])));
					break;
					
				case("1"):

					if(over_len != 3 || !ability_props.containsKey(effect_an_value[2])) continue;
					
					charm_maxes.put(effect_an_value[1],Arrays.asList(1.0));
					effects_to_ability.put(effect_an_value[1], effect_an_value[2]);
					break;
					
				case("2"):
					if(over_len != 4) continue;
					ability_props.put(effect_an_value[1], Arrays.asList(Integer.parseInt(effect_an_value[2]), Integer.parseInt(effect_an_value[3])));
					break;
					
				case("3"):
					if(!charm_maxes.containsKey(effect_an_value[1]) || over_len != 2) continue;
					charm_maxes.remove(effect_an_value[1]);
					effects_to_ability.remove(effect_an_value[1]);
					break;
					
				case("4"):
					if(!ability_props.containsKey(effect_an_value[1]) || over_len != 2) continue;
					ability_props.remove(effect_an_value[1]);
					
					List<String> ability_effects = new ArrayList<String>();
					
					for (String effect : effects_to_ability.keySet()) {
						if(effects_to_ability.get(effect).equals(effect_an_value[1])) {
							ability_effects.add(effect);
						}
					}
					
					for (String effect : ability_effects) {
						effects_to_ability.remove(effect);
						charm_maxes.remove(effect);
					}
					
					break;
					
				case("5"):
					if(!ability_props.containsKey(effect_an_value[1]) || over_len != 3) continue;
					ability_props.replace(effect_an_value[1], Arrays.asList(Integer.parseInt(effect_an_value[2]), ability_props.get(effect_an_value[1]).get(1)));
					break;
					
				case("6"):
					if(!ability_props.containsKey(effect_an_value[1]) || over_len != 3) continue;
					ability_props.replace(effect_an_value[1], Arrays.asList(ability_props.get(effect_an_value[1]).get(0), Integer.parseInt(effect_an_value[2])));
					break;
					
				case("7"):
					if(!ability_props.containsKey(effect_an_value[1]) || over_len != 3) continue;
					
					ability_props.put(effect_an_value[2], ability_props.get(effect_an_value[1]));
					ability_props.remove(effect_an_value[1]);
					
					for(String effect_name : effects_to_ability.keySet()) {
						effects_to_ability.replace(effect_name, effect_an_value[1], effect_an_value[2]);
					}
					
					break;
					
				case("8"):
					if(!charm_maxes.containsKey(effect_an_value[1]) || over_len != 3) continue;
					charm_maxes.put(effect_an_value[2], charm_maxes.get(effect_an_value[1]));
					charm_maxes.remove(effect_an_value[1]);
					
					effects_to_ability.put(effect_an_value[2], effects_to_ability.get(effect_an_value[2]));
					effects_to_ability.remove(effect_an_value[1]);
					break;
					
				}	
				
			}
	
			reader.close();
			fr.close();
	
		} catch (FileNotFoundException e) {
	
			DwarfHighlightMod.LOGGER.warn("Charm override file was not found.");
	
		} catch (IOException e) {
	
		e.printStackTrace();
	
		}
		
	}
	
	
	public void reloadCharmMaxes () {
		charm_maxes.clear();
		
		addBuiltInMaxes();
		readFromFile();
		
	}
	
	
	public void addBuiltInMaxes () {
		// "effect name", list(charm max,1.0,0.0)
		// "effect name", list(charm max,weight,highlight)
		
		charm_maxes.put("advancing shadows cooldown", Arrays.asList(-18.0));
		charm_maxes.put("advancing shadows damage multiplier", Arrays.asList(20.0));
		charm_maxes.put("advancing shadows duration", Arrays.asList(2.25));
		charm_maxes.put("advancing shadows range", Arrays.asList(80.0));
		
		charm_maxes.put("aeroblast cooldown", Arrays.asList(-18.0));
		charm_maxes.put("aeroblast damage", Arrays.asList(35.0));
		charm_maxes.put("aeroblast size", Arrays.asList(70.0));
		charm_maxes.put("aeroblast knockback", Arrays.asList(50.0));
		charm_maxes.put("aeroblast speed duration", Arrays.asList(3.5));
		charm_maxes.put("aeroblast speed amplifier", Arrays.asList(20.0));
		charm_maxes.put("aeromancy mob damage multiplier", Arrays.asList(8.0));
		charm_maxes.put("aeromancy player damage multiplier", Arrays.asList(8.0));
		
		charm_maxes.put("apocalypse cooldown", Arrays.asList(-18.0));
		charm_maxes.put("apocalypse damage", Arrays.asList(35.0));
		charm_maxes.put("apocalypse healing", Arrays.asList(30.0));
		charm_maxes.put("apocalypse max absorption", Arrays.asList(50.0));
		charm_maxes.put("apocalypse radius", Arrays.asList(35.0));
		
		charm_maxes.put("avalanche cooldown", Arrays.asList(-18.0));
		charm_maxes.put("avalanche damage", Arrays.asList(35.0));
		charm_maxes.put("avalanche range", Arrays.asList(56.0));
		charm_maxes.put("avalanche root duration", Arrays.asList(1.5));
		
		charm_maxes.put("blade flurry cooldown", Arrays.asList(-18.0));
		charm_maxes.put("blade flurry damage", Arrays.asList(35.0));
		charm_maxes.put("blade flurry radius", Arrays.asList(35.0));
		charm_maxes.put("blade flurry silence duration", Arrays.asList(1.75));
		
		charm_maxes.put("bottled sunlight cooldown", Arrays.asList(-18.0));
		charm_maxes.put("bottled sunlight absorption health", Arrays.asList(6.0));
		charm_maxes.put("bottled sunlight absorption duration", Arrays.asList(12.0));
		charm_maxes.put("bottled sunlight bottle velocity", Arrays.asList(70.0));
		
		charm_maxes.put("bramble shell damage", Arrays.asList(35.0));
		
		charm_maxes.put("brutalize damage", Arrays.asList(35.0));
		charm_maxes.put("brutalize radius", Arrays.asList(40.0));
		
		charm_maxes.put("bulwark cooldown", Arrays.asList(-18.0));
		
		charm_maxes.put("chaos dagger cooldown", Arrays.asList(-18.0));
		charm_maxes.put("chaos dagger damage multiplier", Arrays.asList(40.0));
		charm_maxes.put("chaos dagger stealth duration", Arrays.asList(1.75));
		charm_maxes.put("chaos dagger stun duration", Arrays.asList(1.75));
		charm_maxes.put("chaos dagger velocity", Arrays.asList(70.0));
		
		charm_maxes.put("cloak of shadows cooldown", Arrays.asList(-18.0));
		charm_maxes.put("cloak of shadows damage multiplier", Arrays.asList(40.0));
		charm_maxes.put("cloak of shadows radius", Arrays.asList(35.0));
		charm_maxes.put("cloak of shadows stealth duration", Arrays.asList(1.75));
		charm_maxes.put("cloak of shadows weaken amplifier", Arrays.asList(20.0));
		charm_maxes.put("cloak of shadows weaken duration", Arrays.asList(3.0));
		
		charm_maxes.put("crushing earth cooldown", Arrays.asList(-18.0));
		charm_maxes.put("crushing earth damage", Arrays.asList(35.0));
		charm_maxes.put("crushing earth range", Arrays.asList(80.0));
		charm_maxes.put("crushing earth stun duration", Arrays.asList(1.5));
		
		charm_maxes.put("cryobox absorption duration", Arrays.asList(12.0));
		charm_maxes.put("cryobox absorption health", Arrays.asList(6.0));
		charm_maxes.put("cryobox cooldown", Arrays.asList(-18.0));
		charm_maxes.put("cryobox frozen duration", Arrays.asList(4.0));
		charm_maxes.put("cryobox ice duration", Arrays.asList(8.0));
		
		charm_maxes.put("dark combos vulnerability amplifier", Arrays.asList(15.0));
		charm_maxes.put("dark combos vulnerability duration", Arrays.asList(1.75));
		charm_maxes.put("dark combos hit requirement", Arrays.asList(-1.0));
		
		charm_maxes.put("deadly strike damage multiplier", Arrays.asList(7.0));
		
		charm_maxes.put("dethroner boss damage multiplier", Arrays.asList(8.0));
		charm_maxes.put("dethroner elite damage multiplier", Arrays.asList(8.0));
		
		charm_maxes.put("detonation damage", Arrays.asList(35.0));
		charm_maxes.put("detonation death radius", Arrays.asList(70.0));
		charm_maxes.put("detonation damage radius", Arrays.asList(40.0));
		
		//TODO: Update divine beam size max;
		charm_maxes.put("divine beam cooldown", Arrays.asList(-18.0));
		charm_maxes.put("divine beam healing", Arrays.asList(30.0));
		charm_maxes.put("divine beam stun duration", Arrays.asList(1.5));
		charm_maxes.put("divine beam max targets bonus", Arrays.asList(3.0));		
		charm_maxes.put("divine beam cooldown reduction", Arrays.asList(4.0));
		charm_maxes.put("divine beam max absorption", Arrays.asList(4.0));
		charm_maxes.put("divine beam absorption duration", Arrays.asList(7.0));
		charm_maxes.put("divine beam size", Arrays.asList(7.0));
		
		charm_maxes.put("dodging cooldown", Arrays.asList(-18.0));
		
		charm_maxes.put("dummy decoy aggro radius", Arrays.asList(70.0));
		charm_maxes.put("dummy decoy cooldown", Arrays.asList(-18.0));
		charm_maxes.put("dummy decoy health", Arrays.asList(60.0));
		charm_maxes.put("dummy decoy stun duration", Arrays.asList(1.75));
		charm_maxes.put("dummy decoy stun radius", Arrays.asList(70.0));
		charm_maxes.put("dummy decoy max life duration", Arrays.asList(4.0));
		
		charm_maxes.put("earthen combos buff duration", Arrays.asList(3.5));
		charm_maxes.put("earthen combos resistance amplifier", Arrays.asList(12.0));
		charm_maxes.put("earthen combos root duration", Arrays.asList(1.5));
		charm_maxes.put("earthen combos hit requirement", Arrays.asList(-1.0));
		
		charm_maxes.put("earthen wrath cooldown", Arrays.asList(-19.0));
		charm_maxes.put("earthen wrath damage reduction", Arrays.asList(12.0));
		charm_maxes.put("earthen wrath damage reflected", Arrays.asList(60.0));
		charm_maxes.put("earthen wrath radius", Arrays.asList(40.0));
		charm_maxes.put("earthen wrath duration", Arrays.asList(2.0));
		charm_maxes.put("earthen wrath transfer radius", Arrays.asList(30.0));
		
		charm_maxes.put("earthquake cooldown", Arrays.asList(-18.0));
		charm_maxes.put("earthquake damage", Arrays.asList(35.0));
		charm_maxes.put("earthquake knockback", Arrays.asList(50.0));
		charm_maxes.put("earthquake radius", Arrays.asList(35.0));
		charm_maxes.put("earthquake silence duration", Arrays.asList(3.0));
		
		charm_maxes.put("enlightenment experience multiplier", Arrays.asList(60.0));
		charm_maxes.put("enlightenment rarity increase chance", Arrays.asList(6.0));
		
		charm_maxes.put("entrench radius", Arrays.asList(35.0));
		charm_maxes.put("entrench root duration", Arrays.asList(1.5));
		
		charm_maxes.put("escape artist cooldown", Arrays.asList(-18.0));
		charm_maxes.put("escape artist max teleport distance", Arrays.asList(80.0));
		charm_maxes.put("escape artist stealth duration", Arrays.asList(1.75));
		charm_maxes.put("escape artist stun radius", Arrays.asList(70.0));
		charm_maxes.put("escape artist stun duration", Arrays.asList(1.75));
		
		charm_maxes.put("eternal savior absorption", Arrays.asList(6.0));
		charm_maxes.put("eternal savior absorption duration", Arrays.asList(12.0));
		charm_maxes.put("eternal savior cooldown", Arrays.asList(-18.0));
		charm_maxes.put("eternal savior healing", Arrays.asList(60.0));
		charm_maxes.put("eternal savior radius", Arrays.asList(70.0));
		charm_maxes.put("eternal savior stun duration", Arrays.asList(1.5));
		
		charm_maxes.put("fireball cooldown", Arrays.asList(-18.0));
		charm_maxes.put("fireball damage", Arrays.asList(35.0));
		charm_maxes.put("fireball fire duration", Arrays.asList(6.0));
		charm_maxes.put("fireball radius", Arrays.asList(35.0));
		charm_maxes.put("fireball velocity", Arrays.asList(60.0));
		
		charm_maxes.put("firework blast cooldown", Arrays.asList(-18.0));
		charm_maxes.put("firework blast damage", Arrays.asList(35.0));
		charm_maxes.put("firework blast damage cap", Arrays.asList(40.0));
		charm_maxes.put("firework blast damage per block", Arrays.asList(2.0));
		charm_maxes.put("firework blast radius", Arrays.asList(35.0));
		
		charm_maxes.put("flame spirit damage", Arrays.asList(35.0));
		charm_maxes.put("flame spirit fire duration", Arrays.asList(6.0));
		charm_maxes.put("flame spirit radius", Arrays.asList(35.0));
		charm_maxes.put("flame spirit duration", Arrays.asList(2.0));
		
		charm_maxes.put("flamestrike cooldown", Arrays.asList(-18.0));
		charm_maxes.put("flamestrike damage", Arrays.asList(35.0));
		charm_maxes.put("flamestrike fire duration", Arrays.asList(6.0));
		charm_maxes.put("flamestrike range", Arrays.asList(80.0));
		charm_maxes.put("flamestrike cone angle", Arrays.asList(50.0));
		charm_maxes.put("flamestrike knockback", Arrays.asList(50.0));
		
		charm_maxes.put("focused combos bleed amplifier", Arrays.asList(20.0));
		charm_maxes.put("focused combos bleed duration", Arrays.asList(1.75));
		charm_maxes.put("focused combos damage multiplier", Arrays.asList(35.0));
		charm_maxes.put("focused combos hit requirement", Arrays.asList(-1.0));
		
		charm_maxes.put("frigid combos damage", Arrays.asList(35.0));
		charm_maxes.put("frigid combos radius", Arrays.asList(35.0));
		charm_maxes.put("frigid combos slowness amplifier", Arrays.asList(20.0));
		charm_maxes.put("frigid combos slowness duration", Arrays.asList(1.75));
		charm_maxes.put("frigid combos hit requirement", Arrays.asList(-1.0));
		
		charm_maxes.put("frost nova cooldown", Arrays.asList(-18.0));
		charm_maxes.put("frost nova damage", Arrays.asList(35.0));
		charm_maxes.put("frost nova ice duration", Arrays.asList(8.0));
		charm_maxes.put("frost nova radius", Arrays.asList(48.0));
		charm_maxes.put("frost nova slow duration", Arrays.asList(3.0));
		charm_maxes.put("frost nova slowness amplifier", Arrays.asList(20.0));
		
		charm_maxes.put("frozen domain duration", Arrays.asList(3.5));
		charm_maxes.put("frozen domain speed amplifier", Arrays.asList(15.0));
		charm_maxes.put("frozen domain healing", Arrays.asList(80.0));
		
		charm_maxes.put("gravity bomb damage", Arrays.asList(35.0));
		charm_maxes.put("gravity bomb radius", Arrays.asList(35.0));
		charm_maxes.put("gravity bomb cooldown", Arrays.asList(18.0));
		
		charm_maxes.put("guarding bolt cooldown", Arrays.asList(-18.0));
		charm_maxes.put("guarding bolt damage", Arrays.asList(35.0));
		charm_maxes.put("guarding bolt radius", Arrays.asList(35.0));
		charm_maxes.put("guarding bolt stun duration", Arrays.asList(1.75));
		charm_maxes.put("guarding bolt cast range", Arrays.asList(80.0));
		
		charm_maxes.put("howling winds cast range", Arrays.asList(80.0));
		charm_maxes.put("howling winds cooldown", Arrays.asList(-18.0));
		charm_maxes.put("howling winds duration", Arrays.asList(3.5));
		charm_maxes.put("howling winds radius", Arrays.asList(35.0));
		charm_maxes.put("howling winds vulnerability amplifier", Arrays.asList(9.0));
		charm_maxes.put("howling winds velocity", Arrays.asList(20.0));
		
		charm_maxes.put("ice barrier cast range", Arrays.asList(80.0));
		charm_maxes.put("ice barrier cooldown", Arrays.asList(-18.0));
		charm_maxes.put("ice barrier damage", Arrays.asList(35.0));
		charm_maxes.put("ice barrier ice duration", Arrays.asList(8.0));
		charm_maxes.put("ice barrier max length", Arrays.asList(80.0));
		
		charm_maxes.put("ice lance cooldown", Arrays.asList(-18.0));
		charm_maxes.put("ice lance damage", Arrays.asList(35.0));
		charm_maxes.put("ice lance debuff amplifier", Arrays.asList(20.0));
		charm_maxes.put("ice lance debuff duration", Arrays.asList(1.75));
		charm_maxes.put("ice lance ice duration", Arrays.asList(8.0));
		charm_maxes.put("ice lance range", Arrays.asList(80.0));
		
		charm_maxes.put("icebreaker debuff damage multiplier", Arrays.asList(8.0));
		charm_maxes.put("icebreaker ice damage multiplier", Arrays.asList(8.0));
		
		charm_maxes.put("igneous rune cooldown", Arrays.asList(-18.0));
		charm_maxes.put("igneous rune damage", Arrays.asList(35.0));
		charm_maxes.put("igneous rune fire duration", Arrays.asList(6.0));
		charm_maxes.put("igneous rune buff amplifier", Arrays.asList(12.0));
		charm_maxes.put("igneous rune radius", Arrays.asList(70.0));
		charm_maxes.put("igneous rune buff duration", Arrays.asList(3.5));
		charm_maxes.put("igneous rune arming time", Arrays.asList(-1.0));
		
		charm_maxes.put("iron grip cooldown", Arrays.asList(-18.0));
		charm_maxes.put("iron grip damage", Arrays.asList(35.0));
		charm_maxes.put("iron grip radius", Arrays.asList(25.0));
		charm_maxes.put("iron grip cast range", Arrays.asList(80.0));
		charm_maxes.put("iron grip resistance amplifier", Arrays.asList(8.0));
		charm_maxes.put("iron grip resistance duration", Arrays.asList(6.0));
		charm_maxes.put("iron grip root duration", Arrays.asList(30.0));
		
		charm_maxes.put("last breath cooldown", Arrays.asList(-18.0));
		charm_maxes.put("last breath cooldown reduction", Arrays.asList(35.0));
		charm_maxes.put("last breath radius", Arrays.asList(35.0));
		charm_maxes.put("last breath speed amplifier", Arrays.asList(20.0));
		charm_maxes.put("last breath speed duration", Arrays.asList(3.5));
		charm_maxes.put("last breath resistance duration", Arrays.asList(1.25));
		
		charm_maxes.put("lightning bottle damage", Arrays.asList(20.0));
		charm_maxes.put("lightning bottle debuff duration", Arrays.asList(3.0));
		charm_maxes.put("lightning bottle radius", Arrays.asList(35.0));
		charm_maxes.put("lightning bottle slowness amplifier", Arrays.asList(10.0));
		charm_maxes.put("lightning bottle vulnerability amplifier", Arrays.asList(10.0));
		charm_maxes.put("lightning bottle kill threshold", Arrays.asList(-1.0));
		charm_maxes.put("lightning bottle kills per bottle", Arrays.asList(-1.0));
		charm_maxes.put("lightning bottle max stacks", Arrays.asList(7.0));
		
		charm_maxes.put("metalmancy cooldown", Arrays.asList(-18.0));
		charm_maxes.put("metalmancy damage", Arrays.asList(35.0));
		charm_maxes.put("metalmancy duration", Arrays.asList(6.0));
		
		charm_maxes.put("one with the wind resistance amplifier", Arrays.asList(6.0));
		charm_maxes.put("one with the wind speed amplifier", Arrays.asList(20.0));
		charm_maxes.put("one with the wind range", Arrays.asList(-2.0));
		
		charm_maxes.put("permafrost ice bonus duration", Arrays.asList(6.0));
		charm_maxes.put("permafrost ice duration", Arrays.asList(8.0));
		charm_maxes.put("permafrost radius", Arrays.asList(35.0));
		charm_maxes.put("permafrost trail duration", Arrays.asList(8.0));
		charm_maxes.put("permafrost trail ice duration", Arrays.asList(8.0));
		
		charm_maxes.put("phantom force weaken amplifier", Arrays.asList(30.0));
		charm_maxes.put("phantom force movement speed", Arrays.asList(30.0));
		charm_maxes.put("phantom force damage", Arrays.asList(35.0));
		charm_maxes.put("phantom force weaken duration", Arrays.asList(8.0));
		charm_maxes.put("phantom force vex duration", Arrays.asList(8.0));
		charm_maxes.put("phantom force spawn count", Arrays.asList(5.0));
		
		charm_maxes.put("piercing cold cooldown", Arrays.asList(-18.0));
		charm_maxes.put("piercing cold damage", Arrays.asList(35.0));
		charm_maxes.put("piercing cold ice duration", Arrays.asList(8.0));
		
		charm_maxes.put("precision strike damage", Arrays.asList(35.0));
		charm_maxes.put("precision strike max stacks", Arrays.asList(1.0));
		charm_maxes.put("precision strike range requirement", Arrays.asList(-35.0));
		
		charm_maxes.put("primordial mastery damage multiplier", Arrays.asList(7.0));
		
		charm_maxes.put("projectile mastery damage multiplier", Arrays.asList(7.0));
		
		charm_maxes.put("pyroblast cooldown", Arrays.asList(-18.0));
		charm_maxes.put("pyroblast damage", Arrays.asList(35.0));
		charm_maxes.put("pyroblast fire duration", Arrays.asList(6.0));
		charm_maxes.put("pyroblast radius", Arrays.asList(35.0));
		
		charm_maxes.put("pyromania damage per mob", Arrays.asList(3.5));
		charm_maxes.put("pyromania radius", Arrays.asList(35.0));
		
		charm_maxes.put("radiant blessing buff duration", Arrays.asList(3.0));
		charm_maxes.put("radiant blessing cooldown", Arrays.asList(-19.0));
		charm_maxes.put("radiant blessing radius", Arrays.asList(35.0));
		charm_maxes.put("radiant blessing resistance amplifier", Arrays.asList(8.0));
		charm_maxes.put("radiant blessing strength amplifier", Arrays.asList(8.0));
		
		charm_maxes.put("rapid fire cooldown", Arrays.asList(-18.0));
		charm_maxes.put("rapid fire damage", Arrays.asList(35.0));
		charm_maxes.put("rapid fire arrows", Arrays.asList(6.0));
		charm_maxes.put("rapid fire firerate", Arrays.asList(50.0));
		
		charm_maxes.put("rejuvenation healing", Arrays.asList(80.0));
		charm_maxes.put("rejuvenation radius", Arrays.asList(35.0));
		
		charm_maxes.put("restoring draft healing", Arrays.asList(95.0));
		charm_maxes.put("restoring draft block cap", Arrays.asList(17.0));
		
		charm_maxes.put("scrapshot cooldown", Arrays.asList(-18.0));
		charm_maxes.put("scrapshot damage", Arrays.asList(35.0));
		charm_maxes.put("scrapshot range", Arrays.asList(80.0));
		charm_maxes.put("scrapshot shrapnel cone angle", Arrays.asList(50.0));
		charm_maxes.put("scrapshot recoil velocity", Arrays.asList(70.0));
		
		charm_maxes.put("shadow slam damage", Arrays.asList(35.0));
		charm_maxes.put("shadow slam radius", Arrays.asList(35.0));
		
		charm_maxes.put("sharpshooter damage per stack", Arrays.asList(2.25));
		charm_maxes.put("sharpshooter max stacks", Arrays.asList(2.0));
		charm_maxes.put("sharpshooter decay timer", Arrays.asList(1.25));
		charm_maxes.put("sharpshooter damage multiplier", Arrays.asList(20.0));
		
		charm_maxes.put("sidearm cooldown", Arrays.asList(-18.0));
		charm_maxes.put("sidearm damage", Arrays.asList(35.0));
		charm_maxes.put("sidearm range", Arrays.asList(80.0));
		charm_maxes.put("sidearm kill cooldown reduction", Arrays.asList(33.0));
		
		charm_maxes.put("soothing combos buff duration", Arrays.asList(3.5));
		charm_maxes.put("soothing combos range", Arrays.asList(80.0));
		charm_maxes.put("soothing combos speed amplifier", Arrays.asList(20.0));
		charm_maxes.put("soothing combos hit requirement", Arrays.asList(-1.0));
		charm_maxes.put("soothing combos haste level", Arrays.asList(1.0));
		charm_maxes.put("soothing combos healing", Arrays.asList(30.0));
		
		charm_maxes.put("skyhook cooldown", Arrays.asList(-18.0));
		charm_maxes.put("skyhook cooldown reduction per block", Arrays.asList(1.0));
		
		charm_maxes.put("spark of inspiration cooldown", Arrays.asList(-18.0));
		charm_maxes.put("spark of inspiration cast range", Arrays.asList(30.0));
		charm_maxes.put("spark of inspiration cooldown reduction rate", Arrays.asList(10.0));
		charm_maxes.put("spark of inspiration strength amplifier", Arrays.asList(20.0));
		charm_maxes.put("spark of inspiration buff duration", Arrays.asList(3.5));
		charm_maxes.put("spark of inspiration resistance duration", Arrays.asList(10.0));
		
		charm_maxes.put("split arrow damage", Arrays.asList(35.0));
		charm_maxes.put("split arrow range", Arrays.asList(80.0));
		charm_maxes.put("split arrow bounces", Arrays.asList(1.0));
		
		charm_maxes.put("steel stallion cooldown", Arrays.asList(-18.0));
		charm_maxes.put("steel stallion duration", Arrays.asList(8.0));
		charm_maxes.put("steel stallion health", Arrays.asList(60.0));
		charm_maxes.put("steel stallion horse speed", Arrays.asList(20.0));
		charm_maxes.put("steel stallion jump strength", Arrays.asList(0.35));
		
		charm_maxes.put("stone skin cooldown", Arrays.asList(-18.0));
		charm_maxes.put("stone skin duration", Arrays.asList(3.5));
		charm_maxes.put("stone skin knockback resistance", Arrays.asList(3.0));
		charm_maxes.put("stone skin resistance amplifier", Arrays.asList(12.0));
		
		charm_maxes.put("sundrops buff duration", Arrays.asList(3.5));
		charm_maxes.put("sundrops drop chance", Arrays.asList(20.0));
		charm_maxes.put("sundrops linger time", Arrays.asList(7.0));
		charm_maxes.put("sundrops speed amplifier", Arrays.asList(20.0));
		charm_maxes.put("sundrops resistance amplifier", Arrays.asList(14.0));
		
		charm_maxes.put("taunt absorption duration", Arrays.asList(6.0));
		charm_maxes.put("taunt absorption per mob", Arrays.asList(0.6));
		charm_maxes.put("taunt cooldown", Arrays.asList(-18.0));
		charm_maxes.put("taunt range", Arrays.asList(80.0));
		charm_maxes.put("taunt max absorption mobs", Arrays.asList(5.0));
		charm_maxes.put("taunt damage bonus", Arrays.asList(50.0));
		
		//TODO: Update knockback max.
		charm_maxes.put("thundercloud form cooldown", Arrays.asList(-18.0));
		charm_maxes.put("thundercloud form damage", Arrays.asList(35.0));
		charm_maxes.put("thundercloud form radius", Arrays.asList(35.0));
		charm_maxes.put("thundercloud form flight speed", Arrays.asList(50.0));
		charm_maxes.put("thundercloud form flight duration", Arrays.asList(5.0));
		charm_maxes.put("thundercloud form knockback", Arrays.asList(5.0));
		
		charm_maxes.put("totem of salvation absorption duration", Arrays.asList(5.0));
		charm_maxes.put("totem of salvation cooldown", Arrays.asList(-18.0));
		charm_maxes.put("totem of salvation duration", Arrays.asList(7.0));
		charm_maxes.put("totem of salvation healing", Arrays.asList(60.0));
		charm_maxes.put("totem of salvation radius", Arrays.asList(35.0));
		charm_maxes.put("totem of salvation max absorption", Arrays.asList(3.0));
		
		charm_maxes.put("toughness max health", Arrays.asList(13.0));
		
		//TODO: Update volcanic combos cooldown
		charm_maxes.put("volcanic combos damage", Arrays.asList(35.0));
		charm_maxes.put("volcanic combos fire duration", Arrays.asList(6.0));
		charm_maxes.put("volcanic combos radius", Arrays.asList(35.0));
		charm_maxes.put("volcanic combos hit requirement", Arrays.asList(-1.0));
		charm_maxes.put("volcanic combos cooldown", Arrays.asList(-1.0));
		
		charm_maxes.put("volcanic meteor cooldown", Arrays.asList(-18.0));
		charm_maxes.put("volcanic meteor damage", Arrays.asList(35.0));
		charm_maxes.put("volcanic meteor fire duration", Arrays.asList(6.0));
		charm_maxes.put("volcanic meteor radius", Arrays.asList(35.0));

		charm_maxes.put("volley arrows", Arrays.asList(13.0));
		charm_maxes.put("volley damage multiplier", Arrays.asList(20.0));
		charm_maxes.put("volley piercing", Arrays.asList(1.0));
		charm_maxes.put("volley cooldown", Arrays.asList(-23.0));
		
		charm_maxes.put("ward of light cooldown", Arrays.asList(-18.0));
		charm_maxes.put("ward of light heal radius", Arrays.asList(70.0));
		charm_maxes.put("ward of light healing", Arrays.asList(30.0));
		charm_maxes.put("ward of light cone angle", Arrays.asList(50.0));
		
		charm_maxes.put("whirlwind radius", Arrays.asList(35.0));
		charm_maxes.put("whirlwind speed amplifier", Arrays.asList(20.0));
		charm_maxes.put("whirlwind speed duration", Arrays.asList(3.5));
		charm_maxes.put("whirlwind knockback", Arrays.asList(20.0));
		
		charm_maxes.put("windswept combos pull strength", Arrays.asList(30.0));
		charm_maxes.put("windswept combos cooldown reduction", Arrays.asList(6.0));
		charm_maxes.put("windswept combos hit requirement", Arrays.asList(-1.0));
		charm_maxes.put("windswept combos radius", Arrays.asList(70.0));
		
		charm_maxes.put("wind walk cooldown", Arrays.asList(-18.0));
		charm_maxes.put("wind walk levitation duration", Arrays.asList(1.75));
		charm_maxes.put("wind walk stun duration", Arrays.asList(1.5));
		charm_maxes.put("wind walk vulnerability amplifier", Arrays.asList(20.0));
		charm_maxes.put("wind walk vulnerability duration", Arrays.asList(3.0));
		charm_maxes.put("wind walk velocity", Arrays.asList(70.0));
	}
	
	
	private void generateEffectToAbility() {
		// "Effect name", "Ability name"
		// Probably a better way, but good enough.
		
		effects_to_ability.put("advancing shadows cooldown", "advancing shadows");
		effects_to_ability.put("advancing shadows damage multiplier", "advancing shadows");
		effects_to_ability.put("advancing shadows duration", "advancing shadows");
		effects_to_ability.put("advancing shadows range", "advancing shadows");
		
		effects_to_ability.put("aeroblast cooldown", "aeroblast");
		effects_to_ability.put("aeroblast damage", "aeroblast");
		effects_to_ability.put("aeroblast size", "aeroblast");
		effects_to_ability.put("aeroblast knockback", "aeroblast");
		effects_to_ability.put("aeroblast speed duration", "aeroblast");
		effects_to_ability.put("aeroblast speed amplifier", "aeroblast");
		
		effects_to_ability.put("aeromancy mob damage multiplier", "aeromancy");
		effects_to_ability.put("aeromancy player damage multiplier", "aeromancy");
		
		effects_to_ability.put("apocalypse cooldown", "apocalypse");
		effects_to_ability.put("apocalypse damage", "apocalypse");
		effects_to_ability.put("apocalypse healing", "apocalypse");
		effects_to_ability.put("apocalypse radius", "apocalypse");
		effects_to_ability.put("apocalypse max absorption", "apocalypse");
		
		effects_to_ability.put("avalanche cooldown", "avalanche");
		effects_to_ability.put("avalanche damage", "avalanche");
		effects_to_ability.put("avalanche range", "avalanche");
		effects_to_ability.put("avalanche root duration", "avalanche");
		
		effects_to_ability.put("blade flurry cooldown", "blade flurry");
		effects_to_ability.put("blade flurry damage", "blade flurry");
		effects_to_ability.put("blade flurry radius", "blade flurry");
		effects_to_ability.put("blade flurry silence duration", "blade flurry");
		
		effects_to_ability.put("bottled sunlight absorption duration", "bottled sunlight");
		effects_to_ability.put("bottled sunlight absorption health", "bottled sunlight");
		effects_to_ability.put("bottled sunlight cooldown", "bottled sunlight");
		effects_to_ability.put("bottled sunlight bottle velocity", "bottled sunlight");
		
		effects_to_ability.put("bramble shell damage", "bramble shell");
		
		effects_to_ability.put("brutalize damage", "brutalize");
		effects_to_ability.put("brutalize radius", "brutalize");
		
		effects_to_ability.put("bulwark cooldown", "bulwark");
		
		effects_to_ability.put("chaos dagger cooldown", "chaos dagger");
		effects_to_ability.put("chaos dagger damage multiplier", "chaos dagger");
		effects_to_ability.put("chaos dagger stealth duration", "chaos dagger");
		effects_to_ability.put("chaos dagger stun duration", "chaos dagger");
		effects_to_ability.put("chaos dagger velocity", "chaos dagger");
		
		effects_to_ability.put("cloak of shadows cooldown", "cloak of shadows");
		effects_to_ability.put("cloak of shadows damage multiplier", "cloak of shadows");
		effects_to_ability.put("cloak of shadows radius", "cloak of shadows");
		effects_to_ability.put("cloak of shadows stealth duration", "cloak of shadows");
		effects_to_ability.put("cloak of shadows weaken amplifier", "cloak of shadows");
		effects_to_ability.put("cloak of shadows weaken duration", "cloak of shadows");
		
		effects_to_ability.put("crushing earth cooldown", "crushing earth");
		effects_to_ability.put("crushing earth damage", "crushing earth");
		effects_to_ability.put("crushing earth range", "crushing earth");
		effects_to_ability.put("crushing earth stun duration", "crushing earth");
		
		effects_to_ability.put("cryobox absorption duration", "cryobox");
		effects_to_ability.put("cryobox absorption health", "cryobox");
		effects_to_ability.put("cryobox cooldown", "cryobox");
		effects_to_ability.put("cryobox frozen duration", "cryobox");
		effects_to_ability.put("cryobox ice duration", "cryobox");
		
		effects_to_ability.put("dark combos vulnerability amplifier", "dark combos");
		effects_to_ability.put("dark combos vulnerability duration", "dark combos");
		effects_to_ability.put("dark combos vulnerability hit requirement", "dark combos");
		effects_to_ability.put("dark combos hit requirement", "dark combos");
		
		effects_to_ability.put("deadly strike damage multiplier", "deadly strike");
		
		effects_to_ability.put("dethroner boss damage multiplier", "dethroner");
		effects_to_ability.put("dethroner elite damage multiplier", "dethroner");
		
		effects_to_ability.put("detonation damage", "detonation");
		effects_to_ability.put("detonation death radius", "detonation");
		effects_to_ability.put("detonation damage radius", "detonation");
		
		effects_to_ability.put("divine beam cooldown", "divine beam");
		effects_to_ability.put("divine beam cooldown reduction", "divine beam");
		effects_to_ability.put("divine beam healing", "divine beam");
		effects_to_ability.put("divine beam max targets bonus", "divine beam");
		effects_to_ability.put("divine beam absorption duration", "divine beam");
		effects_to_ability.put("divine beam stun duration", "divine beam");
		effects_to_ability.put("divine beam max absorption", "divine beam");
		effects_to_ability.put("divine beam size", "divine beam");
		
		effects_to_ability.put("dodging cooldown", "dodging");
		
		effects_to_ability.put("dummy decoy aggro radius", "dummy decoy");
		effects_to_ability.put("dummy decoy cooldown", "dummy decoy");
		effects_to_ability.put("dummy decoy health", "dummy decoy");
		effects_to_ability.put("dummy decoy stun duration", "dummy decoy");
		effects_to_ability.put("dummy decoy stun radius", "dummy decoy");
		effects_to_ability.put("dummy decoy max life duration", "dummy decoy");
		
		effects_to_ability.put("earthen combos buff duration", "earthen combos");
		effects_to_ability.put("earthen combos resistance amplifier", "earthen combos");
		effects_to_ability.put("earthen combos root duration", "earthen combos");
		effects_to_ability.put("earthen combos hit requirement", "earthen combos");
		
		effects_to_ability.put("earthen wrath cooldown", "earthen wrath");
		effects_to_ability.put("earthen wrath damage reduction", "earthen wrath");
		effects_to_ability.put("earthen wrath damage reflected", "earthen wrath");
		effects_to_ability.put("earthen wrath radius", "earthen wrath");
		effects_to_ability.put("earthen wrath transfer radius", "earthen wrath");
		effects_to_ability.put("earthen wrath duration", "earthen wrath");
		
		effects_to_ability.put("earthquake cooldown", "earthquake");
		effects_to_ability.put("earthquake damage", "earthquake");
		effects_to_ability.put("earthquake radius", "earthquake");
		effects_to_ability.put("earthquake silence duration", "earthquake");
		effects_to_ability.put("earthquake knockback", "earthquake");
		
		effects_to_ability.put("eternal savior stun duration", "eternal savior");
		effects_to_ability.put("eternal savior absorption", "eternal savior");
		effects_to_ability.put("eternal savior absorption duration", "eternal savior");
		effects_to_ability.put("eternal savior cooldown", "eternal savior");
		effects_to_ability.put("eternal savior healing", "eternal savior");
		effects_to_ability.put("eternal savior radius", "eternal savior");
		
		effects_to_ability.put("enlightenment experience multiplier", "enlightenment");
		effects_to_ability.put("enlightenment rarity increase chance", "enlightenment");
		
		effects_to_ability.put("entrench radius", "entrench");
		effects_to_ability.put("entrench root duration", "entrench");
		
		effects_to_ability.put("escape artist stun duration", "escape artist");
		effects_to_ability.put("escape artist cooldown", "escape artist");
		effects_to_ability.put("escape artist max teleport distance", "escape artist");
		effects_to_ability.put("escape artist stealth duration", "escape artist");
		effects_to_ability.put("escape artist stun radius", "escape artist");
		
		effects_to_ability.put("fireball cooldown", "fireball");
		effects_to_ability.put("fireball damage", "fireball");
		effects_to_ability.put("fireball fire duration", "fireball");
		effects_to_ability.put("fireball radius", "fireball");
		effects_to_ability.put("fireball range", "fireball");
		
		effects_to_ability.put("firework blast cooldown", "firework blast");
		effects_to_ability.put("firework blast damage", "firework blast");
		effects_to_ability.put("firework blast damage cap", "firework blast");
		effects_to_ability.put("firework blast damage per block", "firework blast");
		effects_to_ability.put("firework blast radius", "firework blast");
		
		effects_to_ability.put("flame spirit damage", "flame spirit");
		effects_to_ability.put("flame spirit fire duration", "flame spirit");
		effects_to_ability.put("flame spirit radius", "flame spirit");
		effects_to_ability.put("flame spirit duration", "flame spirit");
		
		effects_to_ability.put("flamestrike cooldown", "flamestrike");
		effects_to_ability.put("flamestrike damage", "flamestrike");
		effects_to_ability.put("flamestrike fire duration", "flamestrike");
		effects_to_ability.put("flamestrike cone angle", "flamestrike");
		effects_to_ability.put("flamestrike knockback", "flamestrike");
		effects_to_ability.put("flamestrike range", "flamestrike");
		
		effects_to_ability.put("focused combos bleed amplifier", "focused combos");
		effects_to_ability.put("focused combos bleed duration", "focused combos");
		effects_to_ability.put("focused combos damage multiplier", "focused combos");
		effects_to_ability.put("focused combos hit requirement", "focused combos");
		
		effects_to_ability.put("frigid combos damage", "frigid combos");
		effects_to_ability.put("frigid combos radius", "frigid combos");
		effects_to_ability.put("frigid combos slowness amplifier", "frigid combos");
		effects_to_ability.put("frigid combos slowness duration", "frigid combos");
		effects_to_ability.put("frigid combos hit requirement", "frigid combos");
		
		effects_to_ability.put("frost nova cooldown", "frost nova");
		effects_to_ability.put("frost nova damage", "frost nova");
		effects_to_ability.put("frost nova ice duration", "frost nova");
		effects_to_ability.put("frost nova radius", "frost nova");
		effects_to_ability.put("frost nova slow duration", "frost nova");
		effects_to_ability.put("frost nova slowness amplifier", "frost nova");
		
		effects_to_ability.put("frozen domain duration", "frozen domain");
		effects_to_ability.put("frozen domain speed amplifier", "frozen domain");
		effects_to_ability.put("frozen domain healing", "frozen domain");
		
		effects_to_ability.put("gravity bomb damage", "gravity bomb");
		effects_to_ability.put("gravity bomb radius", "gravity bomb");
		effects_to_ability.put("gravity bomb cooldown", "gravity bomb");
		
		effects_to_ability.put("guarding bolt cooldown", "guarding bolt");
		effects_to_ability.put("guarding bolt damage", "guarding bolt");
		effects_to_ability.put("guarding bolt radius", "guarding bolt");
		effects_to_ability.put("guarding bolt stun duration", "guarding bolt");
		effects_to_ability.put("guarding bolt cast range", "guarding bolt");
		
		effects_to_ability.put("howling winds cast range", "howling winds");
		effects_to_ability.put("howling winds cooldown", "howling winds");
		effects_to_ability.put("howling winds duration", "howling winds");
		effects_to_ability.put("howling winds radius", "howling winds");
		effects_to_ability.put("howling winds velocity", "howling winds");
		effects_to_ability.put("howling winds vulnerability amplifier", "howling winds");
		
		effects_to_ability.put("ice barrier cast range", "ice barrier");
		effects_to_ability.put("ice barrier cooldown", "ice barrier");
		effects_to_ability.put("ice barrier damage", "ice barrier");
		effects_to_ability.put("ice barrier ice duration", "ice barrier");
		effects_to_ability.put("ice barrier max length", "ice barrier");
		
		effects_to_ability.put("ice lance cooldown", "ice lance");
		effects_to_ability.put("ice lance damage", "ice lance");
		effects_to_ability.put("ice lance debuff amplifier", "ice lance");
		effects_to_ability.put("ice lance debuff duration", "ice lance");
		effects_to_ability.put("ice lance ice duration", "ice lance");
		effects_to_ability.put("ice lance range", "ice lance");
		
		effects_to_ability.put("icebreaker debuff damage multiplier", "icebreaker");
		effects_to_ability.put("icebreaker ice damage multiplier", "icebreaker");
		
		effects_to_ability.put("igneous rune cooldown", "igneous rune");
		effects_to_ability.put("igneous rune damage", "igneous rune");
		effects_to_ability.put("igneous rune fire duration", "igneous rune");
		effects_to_ability.put("igneous rune buff amplifier", "igneous rune");
		effects_to_ability.put("igneous rune radius", "igneous rune");
		effects_to_ability.put("igneous rune buff duration", "igneous rune");
		effects_to_ability.put("igneous rune arming time", "igneous rune");
		
		effects_to_ability.put("iron grip cooldown", "iron grip");
		effects_to_ability.put("iron grip damage", "iron grip");
		effects_to_ability.put("iron grip radius", "iron grip");
		effects_to_ability.put("iron grip cast range", "iron grip");
		effects_to_ability.put("iron grip resistance amplifier", "iron grip");
		effects_to_ability.put("iron grip resistance duration", "iron grip");
		effects_to_ability.put("iron grip root duration", "iron grip");
		
		effects_to_ability.put("last breath cooldown", "last breath");
		effects_to_ability.put("last breath cooldown reduction", "last breath");
		effects_to_ability.put("last breath radius", "last breath");
		effects_to_ability.put("last breath speed amplifier", "last breath");
		effects_to_ability.put("last breath speed duration", "last breath");
		effects_to_ability.put("last breath resistance duration", "last breath");
		
		effects_to_ability.put("lightning bottle damage", "lightning bottle");
		effects_to_ability.put("lightning bottle debuff duration", "lightning bottle");
		effects_to_ability.put("lightning bottle radius", "lightning bottle");
		effects_to_ability.put("lightning bottle slowness amplifier", "lightning bottle");
		effects_to_ability.put("lightning bottle vulnerability amplifier", "lightning bottle");
		effects_to_ability.put("lightning bottle kill threshold", "lightning bottle");
		effects_to_ability.put("lightning bottle kills per bottle", "lightning bottle");
		effects_to_ability.put("lightning bottle max stacks", "lightning bottle");
		
		effects_to_ability.put("metalmancy cooldown", "metalmancy");
		effects_to_ability.put("metalmancy damage", "metalmancy");
		effects_to_ability.put("metalmancy duration", "metalmancy");
		
		effects_to_ability.put("one with the wind resistance amplifier", "one with the wind");
		effects_to_ability.put("one with the wind speed amplifier", "one with the wind");
		effects_to_ability.put("one with the wind range", "one with the wind");
		
		effects_to_ability.put("permafrost ice bonus duration", "permafrost");
		effects_to_ability.put("permafrost ice duration", "permafrost");
		effects_to_ability.put("permafrost radius", "permafrost");
		effects_to_ability.put("permafrost trail duration", "permafrost");
		effects_to_ability.put("permafrost trail ice duration", "permafrost");
		
		effects_to_ability.put("phantom force weaken amplifier", "phantom force");
		effects_to_ability.put("phantom force movement speed", "phantom force");
		effects_to_ability.put("phantom force damage", "phantom force");
		effects_to_ability.put("phantom force weaken duration", "phantom force");
		effects_to_ability.put("phantom force vex duration", "phantom force");
		effects_to_ability.put("phantom force spawn count", "phantom force");
		
		effects_to_ability.put("piercing cold cooldown", "piercing cold");
		effects_to_ability.put("piercing cold damage", "piercing cold");
		effects_to_ability.put("piercing cold ice duration", "piercing cold");
		
		effects_to_ability.put("precision strike damage", "precision strike");
		effects_to_ability.put("precision strike max stacks", "precision strike");
		effects_to_ability.put("precision strike range requirement", "precision strike");
		
		effects_to_ability.put("primordial mastery damage multiplier", "primordial mastery");
		
		effects_to_ability.put("projectile mastery damage multiplier", "projectile mastery");
		
		effects_to_ability.put("pyroblast cooldown", "pyroblast");
		effects_to_ability.put("pyroblast damage", "pyroblast");
		effects_to_ability.put("pyroblast fire duration", "pyroblast");
		effects_to_ability.put("pyroblast radius", "pyroblast");
		
		effects_to_ability.put("pyromania damage per mob", "pyromania");
		effects_to_ability.put("pyromania radius", "pyromania");
		
		effects_to_ability.put("radiant blessing buff duration", "radiant blessing");
		effects_to_ability.put("radiant blessing cooldown", "radiant blessing");
		effects_to_ability.put("radiant blessing radius", "radiant blessing");
		effects_to_ability.put("radiant blessing resistance amplifier", "radiant blessing");
		effects_to_ability.put("radiant blessing strength amplifier", "radiant blessing");
		
		effects_to_ability.put("rapid fire cooldown", "rapid fire");
		effects_to_ability.put("rapid fire damage", "rapid fire");
		effects_to_ability.put("rapid fire arrows", "rapid fire");
		effects_to_ability.put("rapid fire firerate", "rapid fire");
		
		effects_to_ability.put("rejuvenation healing", "rejuvenation");
		effects_to_ability.put("rejuvenation radius", "rejuvenation");
		
		effects_to_ability.put("restoring draft healing", "restoring draft");
		effects_to_ability.put("restoring draft block cap", "restoring draft");
		
		effects_to_ability.put("scrapshot cooldown", "scrapshot");
		effects_to_ability.put("scrapshot damage", "scrapshot");
		effects_to_ability.put("scrapshot range", "scrapshot");
		effects_to_ability.put("scrapshot shrapnel cone angle", "scrapshot");
		effects_to_ability.put("scrapshot recoil velocity", "scrapshot");
		
		effects_to_ability.put("shadow slam damage", "shadow slam");
		effects_to_ability.put("shadow slam radius", "shadow slam");
		
		effects_to_ability.put("sharpshooter decay timer", "sharpshooter");
		effects_to_ability.put("sharpshooter max stacks", "sharpshooter");
		effects_to_ability.put("sharpshooter damage per stack", "sharpshooter");
		effects_to_ability.put("sharpshooter damage multiplier", "sharpshooter");
		
		effects_to_ability.put("sidearm cooldown", "sidearm");
		effects_to_ability.put("sidearm damage", "sidearm");
		effects_to_ability.put("sidearm kill cooldown reduction", "sidearm");
		effects_to_ability.put("sidearm range", "sidearm");
		
		effects_to_ability.put("skyhook cooldown", "skyhook");
		effects_to_ability.put("skyhook cooldown reduction per block", "skyhook");
		
		effects_to_ability.put("soothing combos buff duration", "soothing combos");
		effects_to_ability.put("soothing combos range", "soothing combos");
		effects_to_ability.put("soothing combos speed amplifier", "soothing combos");
		effects_to_ability.put("soothing combos hit requirement", "soothing combos");
		effects_to_ability.put("soothing combos healing", "soothing combos");
		effects_to_ability.put("soothing combos haste level", "soothing combos");
		
		effects_to_ability.put("spark of inspiration cooldown", "spark of inspiration");
		effects_to_ability.put("spark of inspiration cast range", "spark of inspiration");				
		effects_to_ability.put("spark of inspiration cooldown reduction rate", "spark of inspiration");	
		effects_to_ability.put("spark of inspiration strength amplifier", "spark of inspiration");		
		effects_to_ability.put("spark of inspiration buff duration", "spark of inspiration");				
		effects_to_ability.put("spark of inspiration resistance duration", "spark of inspiration");
		
		effects_to_ability.put("split arrow damage", "split arrow");
		effects_to_ability.put("split arrow range", "split arrow");
		effects_to_ability.put("split arrow bounces", "split arrow");
		
		effects_to_ability.put("steel stallion cooldown", "steel stallion");
		effects_to_ability.put("steel stallion duration", "steel stallion");
		effects_to_ability.put("steel stallion health", "steel stallion");
		effects_to_ability.put("steel stallion horse speed", "steel stallion");
		effects_to_ability.put("steel stallion jump strength", "steel stallion");
		
		effects_to_ability.put("stone skin cooldown", "stone skin");
		effects_to_ability.put("stone skin duration", "stone skin");
		effects_to_ability.put("stone skin knockback resistance", "stone skin");
		effects_to_ability.put("stone skin resistance amplifier", "stone skin");
		
		effects_to_ability.put("sundrops buff duration", "sundrops");
		effects_to_ability.put("sundrops drop chance", "sundrops");
		effects_to_ability.put("sundrops linger time", "sundrops");
		effects_to_ability.put("sundrops resistance amplifier", "sundrops");
		effects_to_ability.put("sundrops speed amplifier", "sundrops");
		
		effects_to_ability.put("taunt absorption duration", "taunt");
		effects_to_ability.put("taunt absorption per mob", "taunt");
		effects_to_ability.put("taunt cooldown", "taunt");
		effects_to_ability.put("taunt range", "taunt");
		effects_to_ability.put("taunt max absorption mobs", "taunt");
		effects_to_ability.put("taunt damage bonus", "taunt");
		
		effects_to_ability.put("thundercloud form cooldown", "thundercloud form");
		effects_to_ability.put("thundercloud form damage", "thundercloud form");
		effects_to_ability.put("thundercloud form radius", "thundercloud form");
		effects_to_ability.put("thundercloud form flight speed", "thundercloud form");
		effects_to_ability.put("thundercloud form flight duration", "thundercloud form");
		effects_to_ability.put("thundercloud form knockback", "thundercloud form");
		
		effects_to_ability.put("totem of salvation absorption duration", "totem of salvation");
		effects_to_ability.put("totem of salvation cooldown", "totem of salvation");
		effects_to_ability.put("totem of salvation duration", "totem of salvation");
		effects_to_ability.put("totem of salvation healing", "totem of salvation");
		effects_to_ability.put("totem of salvation radius", "totem of salvation");
		effects_to_ability.put("totem of salvation max absorption", "totem of salvation");
		
		effects_to_ability.put("toughness max health", "toughness");
		
		effects_to_ability.put("volcanic combos damage", "volcanic combos");
		effects_to_ability.put("volcanic combos fire duration", "volcanic combos");
		effects_to_ability.put("volcanic combos radius", "volcanic combos");
		effects_to_ability.put("volcanic combos hit requirement", "volcanic combos");
		effects_to_ability.put("volcanic combos cooldown", "volcanic combos");
		
		effects_to_ability.put("volcanic meteor cooldown", "volcanic meteor");
		effects_to_ability.put("volcanic meteor damage", "volcanic meteor");
		effects_to_ability.put("volcanic meteor fire duration", "volcanic meteor");
		effects_to_ability.put("volcanic meteor radius", "volcanic meteor");
		
		effects_to_ability.put("volley arrows", "volley");
		effects_to_ability.put("volley damage multiplier", "volley");
		effects_to_ability.put("volley piercing", "volley");
		effects_to_ability.put("volley cooldown", "volley");
		
		effects_to_ability.put("ward of light cooldown", "ward of light");
		effects_to_ability.put("ward of light heal radius", "ward of light");
		effects_to_ability.put("ward of light healing", "ward of light");
		effects_to_ability.put("ward of light cone angle", "ward of light");
		
		effects_to_ability.put("windswept combos pull strength", "windswept combos");
		effects_to_ability.put("windswept combos cooldown reduction", "windswept combos");
		effects_to_ability.put("windswept combos hit requirement", "windswept combos");
		effects_to_ability.put("windswept combos radius", "windswept combos");
		
		effects_to_ability.put("whirlwind radius", "whirlwind");
		effects_to_ability.put("whirlwind speed amplifier", "whirlwind");
		effects_to_ability.put("whirlwind speed duration", "whirlwind");
		effects_to_ability.put("whirlwind knockback", "whirlwind");
		
		effects_to_ability.put("wind walk cooldown", "wind walk");
		effects_to_ability.put("wind walk levitation duration", "wind walk");
		effects_to_ability.put("wind walk stun duration", "wind walk");
		effects_to_ability.put("wind walk vulnerability amplifier", "wind walk");
		effects_to_ability.put("wind walk vulnerability duration", "wind walk");
		effects_to_ability.put("wind walk velocity", "wind walk");
	}
	
	
	/*
	 * ([ability name], Arrays.asList([trigger], [tree]))
	 * 
	 * Triggers:
	 * 0 = passives
	 * 1 = Right Click
	 * 2 = Shift + Bow?
	 * 3 = Shift + Left Click
	 * 4 = Lifeline
	 * 5 = Spawner Break
	 * 6 = Swap
	 * 7 = Shift + Right Click
	 * 8 = Combos
	 * 
	 * Trees:
	 * 0 = Dawn
	 * 1 = Wind
	 * 2 = Earth
	 * 3 = Frost
	 * 4 = Shadow
	 * 5 = Steel
	 * 6 = Flame
	 */
	private void addAbilities() {
		
		// Dawnbringer
		ability_props.put("enlightenment", Arrays.asList(0, 0));
		ability_props.put("lightning bottle", Arrays.asList(0, 0));
		ability_props.put("rejuvenation", Arrays.asList(0, 0));
		ability_props.put("ward of light", Arrays.asList(1, 0));
		ability_props.put("divine beam", Arrays.asList(2, 0));
		ability_props.put("radiant blessing", Arrays.asList(3, 0));
		ability_props.put("eternal savior", Arrays.asList(4, 0));
		ability_props.put("sundrops", Arrays.asList(5, 0));
		ability_props.put("totem of salvation", Arrays.asList(6, 0));
		ability_props.put("spark of inspiration", Arrays.asList(6, 0));
		ability_props.put("bottled sunlight", Arrays.asList(7, 0));
		ability_props.put("soothing combos", Arrays.asList(8, 0));
		
		// Windwalker
		ability_props.put("restoring draft", Arrays.asList(0, 1));
		ability_props.put("one with the wind", Arrays.asList(0, 1));
		ability_props.put("dodging", Arrays.asList(0, 1));
		ability_props.put("aeromancy", Arrays.asList(0, 1));
		ability_props.put("wind walk", Arrays.asList(1, 1));
		ability_props.put("skyhook", Arrays.asList(2, 1));
		ability_props.put("guarding bolt", Arrays.asList(3, 1));
		ability_props.put("last breath", Arrays.asList(4, 1));
		ability_props.put("whirlwind", Arrays.asList(5, 1));
		ability_props.put("howling winds", Arrays.asList(6, 1));
		ability_props.put("thundercloud form", Arrays.asList(6, 1));
		ability_props.put("aeroblast", Arrays.asList(7, 1));		
		ability_props.put("windswept combos", Arrays.asList(8, 1));

		// Earth
		ability_props.put("bulwark", Arrays.asList(0, 2));
		ability_props.put("bramble shell", Arrays.asList(0, 2));
		ability_props.put("toughness", Arrays.asList(0, 2));
		ability_props.put("crushing earth", Arrays.asList(1, 2));
		ability_props.put("earthquake", Arrays.asList(2, 2));
		ability_props.put("taunt", Arrays.asList(3, 2));
		ability_props.put("entrench", Arrays.asList(5, 2));
		ability_props.put("earthen wrath", Arrays.asList(6, 2));
		ability_props.put("stone skin", Arrays.asList(7, 2));
		ability_props.put("iron grip", Arrays.asList(7, 2));
		ability_props.put("earthen combos", Arrays.asList(8, 2));
		
		// Frost
		ability_props.put("frozen domain", Arrays.asList(0, 3));
		ability_props.put("icebreaker", Arrays.asList(0, 3));
		ability_props.put("ice lance", Arrays.asList(1, 3));
		ability_props.put("piercing cold", Arrays.asList(2, 3));
		ability_props.put("frost nova", Arrays.asList(3, 3));
		ability_props.put("cryobox", Arrays.asList(4, 3));
		ability_props.put("permafrost", Arrays.asList(5, 3));
		ability_props.put("avalanche", Arrays.asList(6, 3));
		ability_props.put("ice barrier", Arrays.asList(7, 3));
		ability_props.put("frigid combos", Arrays.asList(8, 3));		
		
		// Shadowdancer
		ability_props.put("shadow slam", Arrays.asList(0, 4));
		ability_props.put("deadly strike", Arrays.asList(0, 4));
		ability_props.put("dethroner", Arrays.asList(0, 4));
		ability_props.put("brutalize", Arrays.asList(0, 4));
		ability_props.put("advancing shadows", Arrays.asList(1, 4));
		ability_props.put("dummy decoy", Arrays.asList(2, 4));
		ability_props.put("cloak of shadows", Arrays.asList(3, 4));
		ability_props.put("escape artist", Arrays.asList(4, 4));
		ability_props.put("phantom force", Arrays.asList(5, 4));
		ability_props.put("chaos dagger", Arrays.asList(6, 4));
		ability_props.put("blade flurry", Arrays.asList(7, 4));
		ability_props.put("dark combos", Arrays.asList(8, 4));
		
		// Steel
		ability_props.put("split arrow", Arrays.asList(0, 5));
		ability_props.put("sharpshooter", Arrays.asList(0, 5));
		ability_props.put("projectile mastery", Arrays.asList(0, 5));
		ability_props.put("rapid fire", Arrays.asList(0, 5));
		ability_props.put("sidearm", Arrays.asList(1, 5));
		ability_props.put("volley", Arrays.asList(2, 5));
		ability_props.put("scrapshot", Arrays.asList(3, 5));
		ability_props.put("steel stallion", Arrays.asList(4, 5));
		ability_props.put("precision strike", Arrays.asList(5, 5));
		ability_props.put("metalmancy", Arrays.asList(6, 5));
		ability_props.put("gravity bomb", Arrays.asList(6, 5));
		ability_props.put("firework blast", Arrays.asList(7, 5));
		ability_props.put("focused combos", Arrays.asList(8, 5));
		
		// Flame
		ability_props.put("detonation", Arrays.asList(0, 6));
		ability_props.put("pyromania", Arrays.asList(0, 6));
		ability_props.put("primordial mastery", Arrays.asList(0, 6));
		ability_props.put("fireball", Arrays.asList(1, 6));
		ability_props.put("pyroblast", Arrays.asList(2, 6));
		ability_props.put("igneous rune", Arrays.asList(3, 6));
		ability_props.put("apocalypse", Arrays.asList(4, 6));
		ability_props.put("flame spirit", Arrays.asList(5, 6));
		ability_props.put("volcanic meteor", Arrays.asList(6, 6));
		ability_props.put("flamestrike", Arrays.asList(7, 6));
		ability_props.put("volcanic combos", Arrays.asList(8, 6));
	}
	
}
