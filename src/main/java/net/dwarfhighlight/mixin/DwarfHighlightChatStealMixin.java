package net.dwarfhighlight.mixin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dwarfhighlight.DwarfHighlightMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

/*
 * ,
    "DwarfHighlightChatStealMixin"
 */

@Environment(EnvType.CLIENT)
@Mixin(ChatHud.class)
public class DwarfHighlightChatStealMixin {

		   @SuppressWarnings("resource")
		@Inject(at = @At("HEAD"), method = "logChatMessage")
		    private void onLogChatMessage(Text message, @Nullable MessageIndicator indicator, CallbackInfo ci)
		    {
			   if(message.getString().indexOf('<') == -1){
				   // Later condense paradox_players into an object(?) and add player UUID so it can be checked if they just died or check it every render.
				   // Check skt's difficulty by getting constructs hp.
				   
				   for(String playername : DwarfHighlightMod.paradox_players_bars.keySet()) {
					   if(message.getString().contains(playername+" was")) {
						   if(!playername.equals(MinecraftClient.getInstance().player.getName().getString())){
								((DwarfHighlightBossBarHudAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().remove(DwarfHighlightMod.paradox_players_bars.get(playername));
							}
							DwarfHighlightMod.paradox_players_bars.remove(playername);
							DwarfHighlightMod.paradox_players_times.remove(playername);
							DwarfHighlightMod.paradox_players_flag.remove(playername);
							return;
					   }
				   }
			   }
			   if(message.getString().indexOf('<') == -1 && message.getString().contains("TEMPORAL ANOMALY TRANSFERRED")){
			   		DwarfHighlightMod.paradox_mode = 2;
			   }else if(message.getString().indexOf('<') == -1 && message.getString().contains("TEMPORAL SHIFT PROTOCOL INITIATED")){
			   		DwarfHighlightMod.paradox_mode = 1;
			   }else if(message.getString().indexOf('<') == -1 && message.getString().endsWith("has been given the Paradox effect!")){
			   		
				   if(!DwarfHighlightMod.show_paradox) {
					   return;
				   }
				   
				   if(DwarfHighlightMod.paradox_mode == 0) {
					   // Fill in for the moment till/if a better solution is found.
					   
					   if(DwarfHighlightMod.paradox_players_bars.size() == 0) {
						   DwarfHighlightMod.paradox_mode = 1;
					   }else {
						   DwarfHighlightMod.paradox_mode = 2;
					   }
					   
				   }
				   
			   		String username = message.getString().split(" ")[0];
			   		
			   		if(DwarfHighlightMod.paradox_mode == 2){
			   			// Transfer paradox from the closest paradoxer.
						MinecraftClient client = MinecraftClient.getInstance();
						
						Double closest_distance_to_exchanger = 100000.0;
						String closest_player_username = "";
						
						String output_str = "In map: ";
						
						for(String player : DwarfHighlightMod.paradox_players_bars.keySet()) {
							output_str = output_str + " " + player;
						}
						
						DwarfHighlightMod.LOGGER.warn(output_str);
						
						for(AbstractClientPlayerEntity nearby_player : client.world.getPlayers()){
							DwarfHighlightMod.LOGGER.warn(nearby_player.getName().getString());
							if(!DwarfHighlightMod.paradox_players_bars.containsKey(nearby_player.getName().getString())) continue;
							
							// Calculate distance from temporal paradox
							// Shulker's X, Y, Z double
							Double player_distance = nearby_player.squaredDistanceTo(-133.5, DwarfHighlightMod.silver_construct_coords.get(DwarfHighlightMod.silver_construct_phase), 207.5);
							
							DwarfHighlightMod.LOGGER.warn(nearby_player.getName().getString() + " " + player_distance);
							
							if(player_distance < closest_distance_to_exchanger){
								closest_distance_to_exchanger = player_distance;
								closest_player_username = nearby_player.getName().getString();
							}
							
						}
						
						if(closest_player_username.length() == 0){
							DwarfHighlightMod.LOGGER.error("Paradox was transfered but no paradox player was nearby?");
						}else{
							if(!closest_player_username.equals(MinecraftClient.getInstance().player.getName().getString())){
								((DwarfHighlightBossBarHudAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().remove(DwarfHighlightMod.paradox_players_bars.get(closest_player_username));
							}
							DwarfHighlightMod.paradox_players_bars.remove(closest_player_username);
							DwarfHighlightMod.paradox_players_times.remove(closest_player_username);
							DwarfHighlightMod.paradox_players_flag.remove(closest_player_username);
						}
						
						DwarfHighlightMod.paradox_mode = 1;
			   		}
			   		
			   		if(DwarfHighlightMod.paradox_mode == 1 && username.equals(MinecraftClient.getInstance().player.getName().getString())){
			   			// Nothing happens here atm.
			   			UUID boss_bar_uuid = UUID.randomUUID();
			   			DwarfHighlightMod.paradox_players_bars.put(username, boss_bar_uuid);
			   			DwarfHighlightMod.paradox_players_times.put(username, Instant.now().getEpochSecond());
			   			DwarfHighlightMod.paradox_players_flag.put(username, false);
			   			
			   			DwarfHighlightMod.LOGGER.warn("Adding client player (" + username + ") to list.");
			   			
			   		}else if(DwarfHighlightMod.paradox_mode == 1){
			   			// Create a new clientside boss bar for the player that got paradoxed. And get start time of it.
			   			 
			   			UUID boss_bar_uuid = UUID.randomUUID();
					
						((DwarfHighlightBossBarHudAccessor) MinecraftClient.getInstance().inGameHud.getBossBarHud()).getBossBars().put(boss_bar_uuid, new ClientBossBar(boss_bar_uuid, Text.literal(username + " paradoxed"), 1.0f, BossBar.Color.BLUE, BossBar.Style.NOTCHED_10, false, false, false));
 
			   			DwarfHighlightMod.paradox_players_bars.put(username, boss_bar_uuid);
			   			DwarfHighlightMod.paradox_players_times.put(username, Instant.now().getEpochSecond());
			   			DwarfHighlightMod.paradox_players_flag.put(username, false);
			   		
			   			DwarfHighlightMod.LOGGER.warn("Adding other player (" + username + ") to list.");
			   			
			   		}else{
			   			DwarfHighlightMod.LOGGER.error("Paradox mode is 0? This shouldn't be reached.");
			   		}
			   		
			   		DwarfHighlightMod.paradox_mode = 0;
			   		
			   		// Else = P1 Exchanger Coords ()
			   		// 66% = P2 Exchanger Coords ()
			   		// 33% = P3 Exchanger Coords ()
			   }else if(DwarfHighlightMod.grab_from_chat){
			   
				   List<Text> testing_comp = message.getSiblings();
		       
			   		if(testing_comp.size() != 2) return;
			   
			   		Text first_sibling = testing_comp.get(1);
			   
			   		bruteForceParse(first_sibling);
			   }
			   
		    }
		   
		   
		   private void bruteForceLoop(Text check_text) {
			   List<Text> checkNext = check_text.getSiblings();
			   
			   for (Text newText : checkNext) {
				   bruteForceParse(newText);
			   }
		   }
		   
		   
		   private void bruteForceParse(Text check_text) {
			   
			   if (check_text == null) return;
			   
			   //If the Text does not have style or the style does not have a hoverEvent, try to split otherwise return.
			   if (check_text.getStyle() != null && check_text.getStyle().getHoverEvent() != null) {
				   
				   	HoverEvent check_hover = check_text.getStyle().getHoverEvent();
				   
				   	if(!check_hover.getAction().toString().contains("show_item")) {bruteForceLoop(check_text); return;}
				   
				   	ItemStack stack = check_hover.getValue(HoverEvent.Action.SHOW_ITEM).asStack();
				   
				   	if(stack.getNbt() == null) {bruteForceLoop(check_text); return;}
			    	
			    	if(!stack.getNbt().contains("Monumenta")) {bruteForceLoop(check_text); return;}
			    	
			    	if(!stack.getNbt().getCompound("Monumenta").getString("Tier").equals("zenithcharm")) {bruteForceLoop(check_text); return;}
			    	
			    	// DwarfHighlightMod.LOGGER.info("Message charm: " + stack.getNbt().getCompound("plain").getCompound("display").getString("Name"));
			    	
			    	if(DwarfHighlightMod.unique_zenith_charms.containsKey(stack.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID"))) {
			    		bruteForceLoop(check_text);
			    		return;
			    	}

			    	outputAllCharms(stack);
			    	
			    	DwarfHighlightMod.LOGGER.info("New charm");
			   }
			   
			   bruteForceLoop(check_text);
			   
		   }
		   
		@SuppressWarnings("resource")
		private void outputAllCharms(ItemStack item) 
			{
				
				String charm_nbt = "";
					
				Long depths_uuid = item.getNbt().getCompound("Monumenta").getCompound("PlayerModified").getLong("DEPTHS_CHARM_UUID");
							
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
								
				charm_nbt = monumenta_tailed.asString();
					
				DwarfHighlightMod.unique_zenith_charms.put(depths_uuid, true);
				
				MinecraftClient.getInstance().player.sendMessage(Text.of("Yoinked " + item_name+" charm."));
				
				//if(charm_nbt.isEmpty()) return;
				
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
}