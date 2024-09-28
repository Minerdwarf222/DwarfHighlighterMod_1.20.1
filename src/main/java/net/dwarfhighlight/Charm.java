package net.dwarfhighlight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Charm {

	public String name;
	public int power;
	public long UUID;
	public double appraised_score;	
	public boolean apprasiable;
	public double apprasied_v2_5_score;
	public double appraised_v3_score;
	public int rarity;
	
	public List<Double> appraised_v3_scores = new ArrayList<Double>();
	public List<String> appraised_v3_names = new ArrayList<String>();
	
	public static double[] balance_div = new double[] {1.0, 2.0, 3.0, 4.0, 5.0};
	
	private CharmMaxs charm_info;
	
	//Ordered Effects by #
	//EffectName: EffectRarity, EffectModifier, EffectRoll, NormalizedValue, NormalizedV3Value
	public HashMap<String,List<String>> effects = new HashMap<String,List<String>>();
	
	public Charm (String name, int power, long UUID, CharmMaxs charm_info, int rarity) {
		this.name = name;
		this.power = power;
		this.UUID = UUID;
		appraised_score = 0.0;
		apprasied_v2_5_score = 0.0;
		appraised_v3_score = 0.0;
		apprasiable = true;
		this.rarity = rarity;
		
		this.charm_info = charm_info;
		
	}
	
	
	public void generateV2_5Score(boolean calc_v3) {
		/*
		 * 0 = passives
		 * 1 - 8 = actual active triggers
		 * 
		 * String = effect_name of highest desired for that trigger
		 * 
		 * HashMap<Integer, String> sorted_by_triggers = new HashMap<Integer, String>();
		 * 
		 * Compares every effect for a single trigger and sets the desireValue to 0 for every effect not max for that trigger.
		 */
		
		int slot_to_pull_from = 0;
		
		if(calc_v3) {
			appraised_v3_score = 0.0;
			slot_to_pull_from = 5;
		}else {
			apprasied_v2_5_score = 0.0;
			slot_to_pull_from = 4;
		}
		
		 HashMap<Integer, HashMap<String, Double>> sorted_by_triggers = new HashMap<Integer, HashMap<String, Double>>();
		 HashMap<String, List<String>> ability_to_effect = new HashMap<String, List<String>>();
		 HashMap<String, Double> transformed_values = new HashMap<String, Double>();
		
		 for (String effect : effects.keySet()){
		  
			effect = effect.toLowerCase();
			
			if(!charm_info.effects_to_ability.containsKey(effect)) continue;
			
			String ability_name = charm_info.effects_to_ability.get(effect);
			
			if(!charm_info.ability_props.containsKey(ability_name)) continue;
			
		 	int effect_trigger = charm_info.ability_props.get(ability_name).get(0);
		 	
		 	if(effect_trigger == 0) {transformed_values.put(effect, Double.parseDouble(effects.get(effect).get(slot_to_pull_from))); continue;}
		 
		 	if(ability_to_effect.containsKey(ability_name)) {
		 		ability_to_effect.get(ability_name).add(effect);
		 	}else {
		 		List<String> temp_list = new ArrayList<String>();
		 		temp_list.add(effect);
		 		ability_to_effect.put(ability_name, temp_list);
		 	}
		 	
		 	if(sorted_by_triggers.containsKey(effect_trigger)){
		 		
		 		if(sorted_by_triggers.get(effect_trigger).containsKey(ability_name)) {
		 			Double new_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
			 		Double old_value = sorted_by_triggers.get(effect_trigger).get(ability_name);
			 		
			 		sorted_by_triggers.get(effect_trigger).put(ability_name, old_value + new_value);
			 		
		 		}else {
		 			
		 			Double new_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
		 			
		 			sorted_by_triggers.get(effect_trigger).put(ability_name, new_value);
		 			
		 		}
		 		
		 
		 	}else{
		 		
		 		HashMap <String, Double> temp_insert = new HashMap<String, Double>();
		 		
		 		Double new_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
		 		
		 		temp_insert.put(ability_name, new_value);
		 		
		 		sorted_by_triggers.put(effect_trigger, temp_insert);		
		 
		 	}
		  
		  }
		 
		 
		 for(int trigger : sorted_by_triggers.keySet()) {
			 
			 // Find the highest valued ability.
			 String max_ability = "";
			 
			 for(String ability : sorted_by_triggers.get(trigger).keySet()) {
				 
				 if(max_ability.equals("")) {
					 max_ability = ability;
					 continue;
				 }else {
					 if(sorted_by_triggers.get(trigger).get(ability) > sorted_by_triggers.get(trigger).get(max_ability)) {
						 max_ability = ability;
					 }
				 }
			 }
			 
			 //Set desireValue of all the effects of non-max ability to 0.
			 for(String ability : sorted_by_triggers.get(trigger).keySet()) {
				 
				 if(ability.equals(max_ability)) {
					 for (String effect : ability_to_effect.get(ability)) {
				 		Double new_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
						 transformed_values.put(effect, new_value);
					 }
					 continue;
				 }
				 
				 for(String effect : ability_to_effect.get(ability)) {
					 transformed_values.put(effect, 0.0);
				 }
				 
			 }
			 
		 }
		 
		  
		  HashMap<Integer, Double> sum_per_tree = new HashMap<Integer, Double>();
		  
		  for (String effect : effects.keySet()){
		  
			effect = effect.toLowerCase();
			
			if(!charm_info.effects_to_ability.containsKey(effect)) continue;
			if(!charm_info.ability_props.containsKey(charm_info.effects_to_ability.get(effect))) continue;
			
		 	int effectTree = charm_info.ability_props.get(charm_info.effects_to_ability.get(effect)).get(1);
		  		
		 	if(sum_per_tree.containsKey(effectTree)){
		  			
		 		sum_per_tree.replace(effectTree, sum_per_tree.get(effectTree)+transformed_values.get(effect));
		  
		 	}else{
		  
		 		sum_per_tree.put(effectTree, transformed_values.get(effect));
		  	
		 	}
		  
		 }
		  
		 List<Integer> sort_trees = new ArrayList<Integer>();
		  
		 for (int tree : sum_per_tree.keySet()){
		  
		 	boolean addedTree = false;
		  
		 	if(sort_trees.size() == 0) { sort_trees.add(tree); continue; }
		 
		 	for(int i = 0; i < sort_trees.size(); i++){
		  
		 		if(sum_per_tree.get(sort_trees.get(i)) < sum_per_tree.get(tree)){
		  
		 			sort_trees.add(i, tree);
		 			addedTree = true;
		 			break;
		  
		 		}
		  
		 	}
		  
		 	if(addedTree) {continue;}
		  
		 	sort_trees.add(tree);
		  
		 }
		 
		 if(calc_v3) {
			 
			 for (int i = 0; i < sort_trees.size(); i++){
				  
				 appraised_v3_score = appraised_v3_score + sum_per_tree.get(sort_trees.get(i))*(1-(0.1*i));
		  
			 }
		  
			 appraised_v3_score = appraised_v3_score/(double)(balance_div[power-1]);
			 appraised_v3_score = appraised_v3_score/100.0;
			 
		 }else {
		 
			 for (int i = 0; i < sort_trees.size(); i++){
		  
				 apprasied_v2_5_score = apprasied_v2_5_score + sum_per_tree.get(sort_trees.get(i))*(1-(0.1*i));
		  
			 }
		  
			 apprasied_v2_5_score = apprasied_v2_5_score/(double)(balance_div[power-1]);
			 apprasied_v2_5_score = apprasied_v2_5_score/100.0;
		 }
		 		
	}
	
	
	public void generateV3Scores() {
		
		appraised_v3_names.clear();
		appraised_v3_scores.clear();
		
		for(String profile_name : DwarfHighlightMod.json_weights.keySet()) {
			
			if(DwarfHighlightMod.json_weights.get(profile_name).isJsonPrimitive()) {
				continue;
			}
			
			double profile_v3_score = generateV3Score(profile_name);
			
			if(profile_v3_score < DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).get("cutoff").getAsDouble()) {
				continue;
			}
			
			if(appraised_v3_scores.isEmpty()) {
				appraised_v3_names.add(profile_name);
				appraised_v3_scores.add(profile_v3_score);
				continue;
			}
			
			int temp_indx = appraised_v3_scores.size();
			
			for(int i = 0; i < appraised_v3_scores.size(); ++i) {
				
				if(appraised_v3_scores.get(i) < profile_v3_score) {
					temp_indx = i;
					break;
				}
				
			}
			
			appraised_v3_names.add(temp_indx, profile_name);
			appraised_v3_scores.add(temp_indx, profile_v3_score);
			
		}
		
	}
	
	
	private double generateV3Score(String profile_name) {		
		/*
		 * 0 = passives
		 * 1 - 8 = actual active triggers
		 * 
		 * String = effect_name of highest desired for that trigger
		 * 
		 * HashMap<Integer, String> sorted_by_triggers = new HashMap<Integer, String>();
		 * 
		 * Compares every effect for a single trigger and sets the desireValue to 0 for every effect not max for that trigger.
		 */
		
		int slot_to_pull_from = 0;
		appraised_v3_score = 0.0;
		slot_to_pull_from = 4;
		
		 HashMap<Integer, HashMap<String, Double>> sorted_by_triggers = new HashMap<Integer, HashMap<String, Double>>();
		 HashMap<String, List<String>> ability_to_effect = new HashMap<String, List<String>>();
		 HashMap<String, Double> transformed_values = new HashMap<String, Double>();
		
		 for (String effect : effects.keySet()){
		  
			effect = effect.toLowerCase();
			
			if(!charm_info.effects_to_ability.containsKey(effect)) continue;
			
			String ability_name = charm_info.effects_to_ability.get(effect);
			
			if(!charm_info.ability_props.containsKey(ability_name)) continue;
			
		 	int effect_trigger = charm_info.ability_props.get(ability_name).get(0);
		 	
		 	if(effect_trigger == 0) {
		 		Double temp_effect_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
		 		Double temp_effect_weight = DwarfHighlighterModMenuIntegration.default_effect_weight;
		 		
		 		if(DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).has(effect)) {
		 			temp_effect_weight = DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).getAsJsonObject(effect).get("weight").getAsDouble();
		 		}
		 		
		 		temp_effect_value = temp_effect_value*temp_effect_weight;
		 		
		 		transformed_values.put(effect, temp_effect_value); 
		 		continue;
		 	}
		 
		 	if(ability_to_effect.containsKey(ability_name)) {
		 		ability_to_effect.get(ability_name).add(effect);
		 	}else {
		 		List<String> temp_list = new ArrayList<String>();
		 		temp_list.add(effect);
		 		ability_to_effect.put(ability_name, temp_list);
		 	}
		 	
		 	if(sorted_by_triggers.containsKey(effect_trigger)){
		 		
		 		if(sorted_by_triggers.get(effect_trigger).containsKey(ability_name)) {
		 			Double new_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
			 		Double old_value = sorted_by_triggers.get(effect_trigger).get(ability_name);
			 		Double temp_effect_weight = DwarfHighlighterModMenuIntegration.default_effect_weight;
			 		
			 		if(DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).has(effect)) {
			 			temp_effect_weight = DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).getAsJsonObject(effect).get("weight").getAsDouble();
			 		}
			 		
			 		new_value = new_value*temp_effect_weight;
			 		
			 		sorted_by_triggers.get(effect_trigger).put(ability_name, old_value + new_value);
			 		
		 		}else {
		 			
		 			Double new_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
		 			Double temp_effect_weight = DwarfHighlighterModMenuIntegration.default_effect_weight;
			 		
			 		if(DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).has(effect)) {
			 			temp_effect_weight = DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).getAsJsonObject(effect).get("weight").getAsDouble();
			 		}
			 		
			 		new_value = new_value*temp_effect_weight;
		 			
		 			sorted_by_triggers.get(effect_trigger).put(ability_name, new_value);
		 			
		 		}
		 		
		 
		 	}else{
		 		
		 		HashMap <String, Double> temp_insert = new HashMap<String, Double>();
		 		
		 		Double new_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
		 		Double temp_effect_weight = DwarfHighlighterModMenuIntegration.default_effect_weight;
		 		
		 		if(DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).has(effect)) {
		 			temp_effect_weight = DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).getAsJsonObject(effect).get("weight").getAsDouble();
		 		}
		 		
		 		new_value = new_value*temp_effect_weight;
		 		
		 		temp_insert.put(ability_name, new_value);
		 		
		 		sorted_by_triggers.put(effect_trigger, temp_insert);		
		 
		 	}
		  
		  }
		 
		 
		 for(int trigger : sorted_by_triggers.keySet()) {
			 
			 // Find the highest valued ability.
			 String max_ability = "";
			 
			 for(String ability : sorted_by_triggers.get(trigger).keySet()) {
				 
				 if(max_ability.equals("")) {
					 max_ability = ability;
					 continue;
				 }else {
					 if(sorted_by_triggers.get(trigger).get(ability) > sorted_by_triggers.get(trigger).get(max_ability)) {
						 max_ability = ability;
					 }
				 }
			 }
			 
			 //Set desireValue of all the effects of non-max ability to 0.
			 for(String ability : sorted_by_triggers.get(trigger).keySet()) {
				 
				 if(ability.equals(max_ability)) {
					 for (String effect : ability_to_effect.get(ability)) {
				 		Double temp_effect_value = Double.parseDouble(effects.get(effect).get(slot_to_pull_from));
				 		Double temp_effect_weight = DwarfHighlighterModMenuIntegration.default_effect_weight;
				 		
				 		if(DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).has(effect)) {
				 			temp_effect_weight = DwarfHighlightMod.json_weights.getAsJsonObject(profile_name).getAsJsonObject(effect).get("weight").getAsDouble();
				 		}
				 		
				 		temp_effect_value = temp_effect_value*temp_effect_weight;
						transformed_values.put(effect, temp_effect_value);
					 }
					 continue;
				 }
				 
				 for(String effect : ability_to_effect.get(ability)) {
					 transformed_values.put(effect, 0.0);
				 }
				 
			 }
			 
		 }
		 
		  
		  HashMap<Integer, Double> sum_per_tree = new HashMap<Integer, Double>();
		  
		  for (String effect : effects.keySet()){
		  
			effect = effect.toLowerCase();
			
			if(!charm_info.effects_to_ability.containsKey(effect)) continue;
			if(!charm_info.ability_props.containsKey(charm_info.effects_to_ability.get(effect))) continue;
			
		 	int effectTree = charm_info.ability_props.get(charm_info.effects_to_ability.get(effect)).get(1);
		  		
		 	if(sum_per_tree.containsKey(effectTree)){
		  			
		 		sum_per_tree.replace(effectTree, sum_per_tree.get(effectTree)+transformed_values.get(effect));
		  
		 	}else{
		  
		 		sum_per_tree.put(effectTree, transformed_values.get(effect));
		  	
		 	}
		  
		 }
		  
		 List<Integer> sort_trees = new ArrayList<Integer>();
		  
		 for (int tree : sum_per_tree.keySet()){
		  
		 	boolean addedTree = false;
		  
		 	if(sort_trees.size() == 0) { sort_trees.add(tree); continue; }
		 
		 	for(int i = 0; i < sort_trees.size(); i++){
		  
		 		if(sum_per_tree.get(sort_trees.get(i)) < sum_per_tree.get(tree)){
		  
		 			sort_trees.add(i, tree);
		 			addedTree = true;
		 			break;
		  
		 		}
		  
		 	}
		  
		 	if(addedTree) {continue;}
		  
		 	sort_trees.add(tree);
		  
		 }
		 
		 for (int i = 0; i < sort_trees.size(); i++){
			  
			 appraised_v3_score = appraised_v3_score + sum_per_tree.get(sort_trees.get(i))*(1-(0.1*i));
	  
		 }
	  
		 appraised_v3_score = appraised_v3_score/(double)(balance_div[power-1]);
		 appraised_v3_score = appraised_v3_score/100.0;
		 
		 return appraised_v3_score;
		 		
	}
	
	
	public void updateCharmMaxes (CharmMaxs charm_info) {
		this.charm_info = charm_info;
	}
	
	
	public String convertRarity(int rarity) {
		
		switch(rarity) {
		case (0):
			return "Common";
		case(1):
			return "Uncommon";
		case(2):
			return "Rare";
		case(3):
			return "Epic";
		case(4):
			return "Legendary";
		default:
			return "";
		}
		
	}
	
	
	public String findRoll(String some_text) {
		
		if(effects.containsKey(some_text)) {
			return effects.get(some_text).get(3);
		}
		
		return "??";
	}
	
	
	public double findModifier(String some_text) {

		if(effects.containsKey(some_text)) {
			return Double.parseDouble(effects.get(some_text).get(2));
		}
		
		return 0.0;
	}
	
	
	/*public String asString() {
		
		String charmAsString = "";		
		
		charmAsString = name + " " + power;
		
		for(List<String> effect : effects) {
			charmAsString = charmAsString + ";" + effect.get(0) + " " + effect.get(1) + " " + effect.get(2) + " " +effect.get(3);
		}
		
		return(charmAsString);
		
	}*/
	
	
	public void addEffect(String effect_name, String effectRarity, String effect_modifier, String effectRoll) {
		
		List<String> effect = new ArrayList<String>();
		
		effect_name = effect_name.toLowerCase();
		
		effect.add(effect_name);
		effect.add(effectRarity);
		
		//Assume the effect modifier is given in form "+/- value% (Effect Name)
		int effect_modifier_indx_end = effect_modifier.indexOf(' ');
		
		if (effect_modifier.charAt(effect_modifier_indx_end-1) == '%') { effect_modifier_indx_end -= 1; }
		
		effect.add(effect_modifier.substring(0,effect_modifier_indx_end));
		
		double actual_effect_roll = Double.parseDouble(effectRoll);
		
		String enabled_profile = DwarfHighlightMod.json_weights.get("enabled").getAsString();
		
		if(!charm_info.charm_maxes.containsKey(effect_name)) {
			
			effect.add(String.format("%.1f", actual_effect_roll*100).replace(',', '.'));
			
			effect.add("??");
			
			apprasiable = false;
			
		}else {
			
			// TM with the inconsistent effect modifiers :sob:
			if(!effectRarity.startsWith("Negative") && (charm_info.charm_maxes.get(effect_name).get(0) < 0)) {
				actual_effect_roll = 1 - actual_effect_roll;
			}
			
			if(effectRarity.startsWith("Negative") && charm_info.charm_maxes.get(effect_name).get(0) > 0) {
				actual_effect_roll = 1 - actual_effect_roll;
			}
			
			effect.add(String.format("%.1f", actual_effect_roll*100).replace(',', '.'));
			
			double normalized_value = Double.parseDouble(effect_modifier.substring(0,effect_modifier_indx_end))/charm_info.charm_maxes.get(effect_name).get(0);
		
			String str_normalized_value = String.format("%.1f", normalized_value*100);
			str_normalized_value = str_normalized_value.replace(',', '.');
		
			appraised_score += normalized_value;
		
			effect.add(str_normalized_value);
			
			double effect_weight = DwarfHighlighterModMenuIntegration.default_effect_weight;
			
			if(!(DwarfHighlightMod.json_weights.getAsJsonObject(enabled_profile).get(effect_name) == null)) {
				effect_weight = DwarfHighlightMod.json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(effect_name).get("weight").getAsDouble();
			}
			
			effect.add(String.format("%.1f", normalized_value*effect_weight*100).replace(',', '.'));
		}
		
		effects.put(effect_name,effect);
		
		//effect.add(effect_modifier.substring(0,effect.))
		
	}
	
}
