package net.dwarfhighlight.mixin;

import net.dwarfhighlight.DwarfHighlightMod;
import net.dwarfhighlight.DwarfHighlighterModMenuIntegration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerInventory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.datafixers.util.Pair;

@Mixin(HandledScreen.class)
public class DwarfHighlightMixin {
	
	// If a charm has roll > .99 then state in chat.
	
	private HashMap<String, Pair<Integer, Integer>> found_items = new HashMap<String, Pair<Integer, Integer>>();
	private HashMap<String, String> nameTranslate = new HashMap<String, String>();
	
	@SuppressWarnings("rawtypes")
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawForeground(Lnet/minecraft/client/gui/DrawContext;II)V"))
	private void renderHighlightCharms(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
	{
		HandledScreen this_object = (HandledScreen<?>)(Object)this;
		ScreenHandler container = this_object.getScreenHandler();
		
		// User's Charms
		
		/*if(!DwarfHighlightMod.show_charm_overflow && DwarfHighlightMod.update_charm_summary_lines == 1) {
			DwarfHighlightMod.update_charm_summary_lines = 0;
		}
		
		if(DwarfHighlightMod.update_charm_summary_lines == 1) {
			//DwarfHighlightMod.LOGGER.warn(title_old);
			
			if(this_object.getTitle().getString().endsWith("'s Charms")) {
				HashMap<String, Double> changed_values = new HashMap<String, Double>();
				
				// Charm slots are 45 - 51
				// Charm Effect Summary is 9
				for(int k = 45; k < 52; ++k){
					
					if(container.slots.size() < k) {
						continue;
					}
					
					Slot slot = container.slots.get(k);
					if(!slot.isEnabled()) continue;
					if(!slot.hasStack()) continue;
					
					ItemStack item = slot.getStack();
					
					if(item.getNbt() == null) break;
					if(!item.getNbt().contains("Monumenta")) continue;
	    			if(!item.getNbt().getCompound("Monumenta").contains("Tier")) continue;
	    			if(!item.getNbt().getCompound("Monumenta").getString("Tier").equals("zenithcharm")) continue;
					
	    			// This whole part is getting the modifier value
	    			String abomination_string = item.getNbt().getCompound("Monumenta").get("CharmText").asString().toLowerCase();
	    	    	
	    	    	List <String> all_the_effects = new ArrayList<String>();
	    	    	
	    	    	int next_effect_indx = -1;
	    	    	
	    	    	while((next_effect_indx = abomination_string.indexOf("text")) != -1) {
	    	    		all_the_effects.add(abomination_string.substring(next_effect_indx+7, abomination_string.indexOf('"',next_effect_indx+7)));
	    	    		abomination_string = abomination_string.substring(next_effect_indx+5);
	    	    	}
	    	    	
	    	    	// Now all_the_effects is a list of every effect in the form "+/-Value(%) [Effect Name]"
	    	    	for (String effect : all_the_effects) {
	    	    		String effect_name = effect.substring(effect.indexOf(' ')+1);
	    	    		String effect_value = effect.substring(0, effect.indexOf(' '));
	    	    		
	    	    		//DwarfHighlightMod.LOGGER.warn("Checking Effects: " + effect_name);
	    	    		
	    	    		if(!DwarfHighlightMod.charm_summary_maxes.containsKey(effect_name)) continue;
	    	    		
	    	    		//DwarfHighlightMod.LOGGER.warn("Is in maxes: " + effect_name);
	    	    		
	    	    		if(changed_values.containsKey(effect_name)) {
	    	    			Double new_value = changed_values.get(effect_name) + Double.parseDouble(effect_value.replace("%", ""));
	    	    			changed_values.put(effect_name, new_value);
	    	    		}else {
	    	    			changed_values.put(effect_name, Double.parseDouble(effect_value.replace("%", "")));
	    	    		}
	    	    		
	    	    	}
	    	    	// charm_summary_overflow.put(" " + (effect_value_i - charm_maxes.charm_maxes.get(effect_name).get(3)) + (percent_value ? "%" : "") + " overflow", i+offset);
				}
				
				for (String effect_name : DwarfHighlightMod.charm_summary_maxes.keySet()) {
					
					if(!changed_values.containsKey(effect_name)) {
						DwarfHighlightMod.charm_summary_overflow.remove(effect_name);
						DwarfHighlightMod.LOGGER.warn("Wasn't removed: " + effect_name);
					}else {
						DwarfHighlightMod.charm_summary_overflow.put(effect_name + "| " + (Math.round((changed_values.get(effect_name) - DwarfHighlightMod.charm_summary_maxes.get(effect_name)) * 100.0) / 100.0) + (DwarfHighlightMod.charm_summary_is_percent.get(effect_name) ? "%" : "") + " overflow", DwarfHighlightMod.charm_summary_overflow.get(effect_name));
						DwarfHighlightMod.charm_summary_overflow.remove(effect_name);
					}
				}
				DwarfHighlightMod.update_charm_summary_lines = 2;
			}
		}*/
		
		String enabled_profile = DwarfHighlightMod.json_weights.get("enabled").getAsString();
		
		for (int k = 0; k < container.slots.size(); ++k) {
            Slot slot = container.slots.get(k);
            if (slot.isEnabled()) {
            	
            	if(!slot.hasStack()) continue;
            	
            	ItemStack item = slot.getStack();
            	
    			if(item.getNbt() == null) continue;
    			if(!item.getNbt().contains("Monumenta")) continue;
    			if(!item.getNbt().getCompound("Monumenta").contains("Tier")) continue;
    			
    			int color = 0xC9365A7F;
    			
    			String compare_item_name = item.getNbt().getCompound("plain").getCompound("display").getString("Name");
    			String compare_item_region = item.getNbt().getCompound("Monumenta").getString("Region").toLowerCase();
    			
    			String compare_item_name_lower_case = compare_item_name.toLowerCase();
    			String compare_item_name_with_region = compare_item_region + " " + compare_item_name_lower_case;
    			
    			if(item.getNbt().getCompound("Monumenta").getString("Tier").equals("zenithcharm")) {
    			
    				int effect_number = 1;
    				String effect_name = "";
    				boolean highlight_slot = false;
    			
    				while((effect_name = item.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getString("DEPTHS_CHARM_EFFECT"+effect_number)).length() != 0) {
    				
    					if(!DwarfHighlightMod.charm_maxes.charm_maxes.containsKey(effect_name.toLowerCase())) {
    						effect_number++;
    						continue;
    					}
    					
    					if(!(DwarfHighlightMod.json_weights.getAsJsonObject(enabled_profile).get(effect_name.toLowerCase()) == null) &&
    							DwarfHighlightMod.json_weights.getAsJsonObject(enabled_profile).getAsJsonObject(effect_name.toLowerCase()).get("highlight").getAsBoolean()) {
    						highlight_slot = true;
    						break;
    					}
    					effect_number++;
    				}
            	
    				if(!highlight_slot) continue;  			
    			
    				switch(item.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getInt("DEPTHS_CHARM_RARITY")) {
    				case (1):
    					color = 0xC9555555;
    					break;
    				case(2):
    					color = 0xC970BC6D;
						break;
    				case(3):
    					color = 0xC9705ECA;
						break;
    				case(4):
    					color = 0xC9CD5ECA;
						break;
    				case(5):
    					color = 0xC9E49B20;
						break;
    				}
    			
    			}else if(DwarfHighlighterModMenuIntegration.getToggleValue() && (DwarfHighlightMod.needed_items.containsKey(compare_item_name_with_region) || DwarfHighlightMod.needed_items.containsKey(compare_item_name.toLowerCase()))) {
    				color = 0xC9FF0000;
    			}else {
    				
    				continue;
    			}
                int l = slot.x;
                int m = slot.y;
                
                context.drawBorder(l, m, 16, 16, color);
                context.drawBorder(l+1, m+1, 14, 14, color);
            }
        }
        
	}

	@SuppressWarnings("rawtypes")
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;getMatrices()Lnet/minecraft/client/util/math/MatrixStack;"))
	//@Inject(method = "render()V", at = @At("HEAD"))
	private void renderUngrabbedCharms(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci)
	{
		HandledScreen this_object = (HandledScreen<?>)(Object)this;
		ScreenHandler container = this_object.getScreenHandler();
		
		if(!DwarfHighlighterModMenuIntegration.getToggleZenithValue() && !DwarfHighlightMod.grab_tool_tip) return;
		
		for (int k = 0; k < container.slots.size(); ++k) {
            Slot slot = container.slots.get(k);
            if (slot.isEnabled()) {
            	
            	if(!slot.hasStack()) continue;
            	
            	ItemStack item = slot.getStack();
            	
    			if(item.getNbt() == null) continue;
    			if(!item.getNbt().contains("Monumenta")) continue;
    			if(!item.getNbt().getCompound("Monumenta").contains("Tier")) continue;
    			if(!item.getNbt().getCompound("Monumenta").getString("Tier").equals("zenithcharm")) continue;
    			
    			if(DwarfHighlightMod.unique_zenith_charms.containsKey(item.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID"))) continue;
    			
    			int color = 0xC9FF0000;
    			    					
                int l = slot.x;
                int m = slot.y;
                
                context.drawBorder(l+7, m+2, 2, 8, color);
                context.drawBorder(l+7, m+12, 2, 2, color);
            }
        }
        
	}
	
	
	@Inject(at = @At("HEAD"), method = "close()V")
	private void onCloseInject(CallbackInfo info) 
	{
		
		onCloseCheck((HandledScreen<?>) (Object) this);
		
	}
	
	
	@SuppressWarnings("resource")
	private void onCloseCheck(HandledScreen<?> screen) 
	{
		
		if(!DwarfHighlightMod.container_checkpointing && !DwarfHighlighterModMenuIntegration.getToggleValue() && !DwarfHighlighterModMenuIntegration.getToggleZenithValue()) return;
		if(screen.getClass() != GenericContainerScreen.class) return;
		
		//String containerType = screen.getTitle().getString();
		//if(!containerType.equals("Barrel")&&!DwarfHighlighterModMenuIntegration.getcheckChestsValue()) return;
		
		ScreenHandler container = screen.getScreenHandler();
		List<ItemStack> list_of_items = container.slots.stream().filter(slot -> slot.hasStack() && slot.inventory.getClass() != PlayerInventory.class).map(Slot::getStack).toList();
		String messagevalue = "";
		
		if(DwarfHighlighterModMenuIntegration.getToggleZenithValue()) {
			outputAllCharms(list_of_items);
		}
		
		if(DwarfHighlightMod.last_right_clicked != null && DwarfHighlightMod.anti_monu_flag && DwarfHighlightMod.container_checkpointing) {
			DwarfHighlightMod.anti_monu_flag = false;
			createCheckpoint(list_of_items);
		}
		
		if(!DwarfHighlighterModMenuIntegration.getToggleValue()) { return;}
		
		//DwarfHighlightMod.LOGGER.info("Checking for matches:");
		for(ItemStack compare_item : list_of_items) {
			
			if(compare_item.getNbt() == null) continue;
			
			int item_qty = compare_item.getCount();
			String compare_item_name = compare_item.getNbt().getCompound("plain").getCompound("display").getString("Name");
			String compare_item_region = compare_item.getNbt().getCompound("Monumenta").getString("Region").toLowerCase();
			
			String compare_item_name_lower_case = compare_item_name.toLowerCase();
			String compare_item_name_with_region = compare_item_region + " " + compare_item_name_lower_case;
			
			if(DwarfHighlightMod.needed_items.containsKey(compare_item_name_with_region)) {
				
				addItemToFound(compare_item_name_with_region, compare_item_name, item_qty);
				
			}
			else if(DwarfHighlightMod.needed_items.containsKey(compare_item_name.toLowerCase())) {
				
				addItemToFound(compare_item_name_lower_case, compare_item_name, item_qty);
				
			}
		}
		
		for(String add_item : found_items.keySet()) {
			
			int qty_found = found_items.get(add_item).getFirst();
			int amt_wanted = found_items.get(add_item).getSecond();
			String who_wants = DwarfHighlightMod.needed_items.get(add_item).get(1);
			
			if (amt_wanted <= 0){
				messagevalue = messagevalue + qty_found + "x " + nameTranslate.get(add_item) + " [" + who_wants +"]; ";
			}else {
				messagevalue = messagevalue + qty_found +"x/" + amt_wanted +"x " + nameTranslate.get(add_item) + " [" + who_wants + "]; ";
			}
		}
		
		if(DwarfHighlightMod.show_chat_message_if_highlighted_items && !messagevalue.isEmpty()) {
			
			MinecraftClient.getInstance().player.sendMessage(Text.of((messagevalue.substring(0, messagevalue.length()-2)+".")));
			
		}
	}
	
	
	private void createCheckpoint(List<ItemStack> items)
	{
		
		if(DwarfHighlightMod.check_containers == null) return;
		
		long unix_timestamp = Instant.now().getEpochSecond();
		
		int block_x = DwarfHighlightMod.last_right_clicked.getX();
		int block_y = DwarfHighlightMod.last_right_clicked.getY();
		int block_z = DwarfHighlightMod.last_right_clicked.getZ();	
		
		if(!DwarfHighlightMod.check_containers.containsKey("x"+block_x+"/y"+block_y+"/z"+block_z)) return;
		
		//Human readable time stamp
		String logLine = "("+Instant.now().truncatedTo(ChronoUnit.SECONDS)+", "+unix_timestamp+", [";
		
		// Get all the items and condense them.
		HashMap<String, Integer> itemsInContainer = new HashMap<String, Integer>();
		
		for(ItemStack item : items) {
			
			String item_name = "";
			int item_qty = item.getCount();	
			
			if(item.getNbt() == null || !item.getNbt().contains("Monumenta")) {
				item_name = item.getItem().getName().getString();
			}else {
				item_name = item.getNbt().getCompound("plain").getCompound("display").getString("Name");
			}
			
			if(itemsInContainer.containsKey(item_name)) {
				
				int previousQty = itemsInContainer.get(item_name);
				itemsInContainer.put(item_name, previousQty + item_qty);
				
			}else {
				itemsInContainer.put(item_name, item_qty);
			}
			
		}
		
		// Translate the hashmap of condensed items into a log
		for (String item : itemsInContainer.keySet()) {
			logLine = logLine + "("+item+", "+itemsInContainer.get(item)+"), ";
		}
		
		if(itemsInContainer.keySet().isEmpty()) {
			logLine = logLine + "])";
		}else {
			logLine = logLine.substring(0, logLine.length()-2)+"])";
		}
		
		// Create file!
		// Barrel Name, coords, .txt
		String fileName = DwarfHighlightMod.base_barrel_checkpoints_file+DwarfHighlightMod.check_containers.get("x"+block_x+"/y"+block_y+"/z"+block_z).replace(" ", "_")+"_x"+block_x+"y"+block_y+"z"+block_z+".txt";
		
		try(FileWriter fw = new FileWriter(fileName, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
				out.println(logLine);
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
				DwarfHighlightMod.LOGGER.error("Failed at creating checkpoint.");
			}
		
	}
	
	@SuppressWarnings("resource")
	public void outputAllCharms(List<ItemStack> items) 
	{
		
		List<String> charm_nbts = new ArrayList<String>();
		List<Long> charm_uuids = new ArrayList<Long>();
		
		for(ItemStack item : items) {
			
			if(item.getNbt() == null) continue;
			if(!item.getNbt().contains("Monumenta")) continue;
			if(!item.getNbt().getCompound("Monumenta").contains("Tier")) continue;
			if(!item.getNbt().getCompound("Monumenta").getString("Tier").equals("zenithcharm")) continue;
			
			Long depths_uuid = item.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID");
			
			if(DwarfHighlightMod.unique_zenith_charms.containsKey(depths_uuid)) continue;	
			
			String item_name = item.getNbt().getCompound("plain").getCompound("display").getString("Name");
			
			if(item_name.length() == 0) {
				
				String parseItemName = item.getNbt().getCompound("display").getString("Name");
				item_name = parseItemName.substring(parseItemName.indexOf("text")+7, parseItemName.indexOf("\"",parseItemName.indexOf("text")+9));
			}
			
			int charm_power = item.getNbt().getCompound("Monumenta").getInt("CharmPower");
			NbtCompound monumenta_tailed = item.getNbt().getCompound("Monumenta").getCompound("PlayerModified");
			
			monumenta_tailed.putString("ItemName", item_name);
			monumenta_tailed.putInt("CharmPower", charm_power);
			monumenta_tailed.put("CharmText", item.getNbt().getCompound("Monumenta").get("CharmText"));
						
			charm_nbts.add(monumenta_tailed.asString());
			
			DwarfHighlightMod.unique_zenith_charms.put(depths_uuid,true);
			charm_uuids.add(depths_uuid);
		}
		
		if(charm_nbts.isEmpty()) return;
		
		MinecraftClient.getInstance().player.sendMessage(Text.of("Yoinked " + charm_nbts.size()+" charms."));
		
		// https://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
		try(FileWriter fw = new FileWriter(DwarfHighlightMod.charm_file, true);
			    BufferedWriter bw = new BufferedWriter(fw);
			    PrintWriter out = new PrintWriter(bw))
			{
				for (String charm : charm_nbts) {
					out.print("\n"+charm);
				}
			} catch (IOException e) {
			    //exception handling left as an exercise for the reader
			}
		
		// https://stackoverflow.com/questions/1625234/how-to-append-text-to-an-existing-file-in-java
		try(FileWriter fw = new FileWriter(DwarfHighlightMod.unique_charm_uuids_file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
			{
				for (Long depths_uuid : charm_uuids) {
					out.print("\n"+depths_uuid+"");
				}
			} catch (IOException e) {
				//exception handling left as an exercise for the reader
			}

	}
	
	
	public void addItemToFound(String item_name_with_region, String actual_item_name, int item_qty) 
	{
		
		int amt_wanted = Integer.parseInt(DwarfHighlightMod.needed_items.get(item_name_with_region).get(0));
		
		if(found_items.containsKey(item_name_with_region)) {
			
			int old_qty = found_items.get(item_name_with_region).getFirst();
			found_items.put(item_name_with_region, new Pair<Integer, Integer>(old_qty+item_qty,amt_wanted));
			
		}else {
			
			found_items.put(item_name_with_region, new Pair<Integer, Integer>(item_qty, amt_wanted));
			nameTranslate.put(item_name_with_region, actual_item_name);
			
		}
	}
}