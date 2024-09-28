package net.dwarfhighlight.mixin;

import java.time.Instant;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dwarfhighlight.DwarfHighlightMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(ClientBossBar.class)
public class DwarfHighlightBossBarMixin {
	
	@Inject(at = @At("TAIL"), method = "<init>(Ljava/util/UUID;Lnet/minecraft/text/Text;FLnet/minecraft/entity/boss/BossBar$Color;Lnet/minecraft/entity/boss/BossBar$Style;ZZZ)V")
	private void onCreationClientBossBar(UUID uuid, Text name, float percent, BossBar.Color color, BossBar.Style style, boolean darkenSky, boolean dragonMusic, boolean thickenFog, CallbackInfo ci) {
		
		if(!DwarfHighlightMod.show_sirius && !DwarfHighlightMod.show_paradox) {
			return;
		}
		
		ClientBossBar this_object = (ClientBossBar)(Object)this;
		
		String boss_bar_name = this_object.getName().getString().toLowerCase();
			
		if(DwarfHighlightMod.show_sirius && boss_bar_name.startsWith("sirius, the final herald")){
			DwarfHighlightMod.sirius_start_time = Instant.now().getEpochSecond();	
		}else if(DwarfHighlightMod.show_sirius && boss_bar_name.equals("impaling doom")) {
			//Duration is 15s
			// 1 / -5		
			this_object.setName(Text.literal(this_object.getName().getString() + " " + getHpChangeValues(-1, 5, 15)).setStyle(this_object.getName().getStyle()));
		}else if(DwarfHighlightMod.show_sirius && boss_bar_name.equals("encroaching blight")) {
			//Duration is 7s
			// 1 / -5
			this_object.setName(Text.literal(this_object.getName().getString() + " " + getHpChangeValues(-1, 5, 7)).setStyle(this_object.getName().getStyle()));
		}else if(DwarfHighlightMod.show_sirius && boss_bar_name.equals("defend tuulen!")) {
			//Duration is 12s
			// 2 / -5
			this_object.setName(Text.literal(this_object.getName().getString() + " " + getHpChangeValues(-2, 5, 12)).setStyle(this_object.getName().getStyle()));
		}else if(boss_bar_name.equals("silver construct")) {
			DwarfHighlightMod.silver_construct_phase = 0;
		}
		
	}
	
	public String getHpChangeValues(int win, int lose, int duration) {
		
		double multiplier = 1.0;
		
		long current_time = Instant.now().getEpochSecond() + duration;
		long time_passed = current_time - DwarfHighlightMod.sirius_start_time;
		
		if (time_passed > 1200) {
			return "";
		}
		
		multiplier += (double)(Math.floor(time_passed/90)) * 0.5;
		
		int on_win = (int) (win*multiplier);
		int on_lose = (int) (lose*multiplier);
		
		return "["+on_win+"/"+on_lose+"]";
		
	}
	
}
