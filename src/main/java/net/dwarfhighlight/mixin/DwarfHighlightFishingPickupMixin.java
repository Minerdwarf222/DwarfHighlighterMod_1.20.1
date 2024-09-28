package net.dwarfhighlight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dwarfhighlight.DwarfHighlightMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.item.TippedArrowItem;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;

//(Removed) Ripped from https://github.com/khanshoaib3/minecraft-access/pull/51/files
//(Test 2) https://discord.com/channels/507304429255393322/507982478276034570/1138649321634541608
@Environment(EnvType.CLIENT)
@Mixin(ClientPlayNetworkHandler.class)
public class DwarfHighlightFishingPickupMixin
	{
	    @Inject(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	    private void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo ci)
	    {
	    	if(!DwarfHighlightMod.log_fishing) return;
	        
	    	MinecraftClient client = MinecraftClient.getInstance();
	        
	    	if (!client.world.isClient || !(client.player.getMainHandStack().getItem() instanceof FishingRodItem)) return;
	        
	    	Entity entity = client.player.getWorld().getEntityById(packet.getEntityId());
	    	
	    	if (entity == null ) return;
	    	if (!entity.getType().equals(EntityType.ITEM)) return;
	    	
	        ItemStack item_stack = ((ItemEntity) entity).getStack();
	        String item_name = ((ItemEntity) entity).getStack().getName().getString().toLowerCase();
	        int item_count = ((ItemEntity) entity).getStack().getCount();
	        			
	        if (item_stack.getNbt() != null) {
	        	item_name = item_stack.getNbt().getCompound("plain").getCompound("display").getString("Name").toLowerCase();
	        }
	    	
	        item_name = item_count + ";" + item_name;
	        
	        String item_title = "";
	        
	        
    		if(item_stack.getNbt() == null) {
    			if(item_name.equals("raw cod")||item_name.equals("raw salmon")||item_name.equals("tropical fish")) {
    				item_title = "fish";
    			}else if(item_name.equals("arrow")) {
    				item_title = "arrows";
    			}
    		}else {
    			if(item_stack.getNbt().getCompound("Monumenta").contains("FishQuality")){
    				item_title = "quality " + item_stack.getNbt().getCompound("Monumenta").getInt("FishQuality");
    			}else if(item_stack.getNbt().getCompound("Monumenta").contains("Tier")) {
    				item_title = "tier "+item_stack.getNbt().getCompound("Monumenta").getString("Tier").toLowerCase();
    			}else if(item_stack.getItem() instanceof TippedArrowItem) {
    				item_title = "arrows";
    			}else if(item_stack.getItem() instanceof PotionItem || item_stack.getItem() instanceof SplashPotionItem) {
    				item_title = "potions";
    			}
    		}
	        
	        DwarfHighlightMod.writeToFile(item_name,DwarfHighlightMod.fishing_log_file,DwarfHighlightMod.top_level_dir + "/DwarfHighlightModFishing_temp.txt", item_title, 0.5);
	            		
	    }
	    
	}