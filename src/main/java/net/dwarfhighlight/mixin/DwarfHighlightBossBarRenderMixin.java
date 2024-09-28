package net.dwarfhighlight.mixin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dwarfhighlight.DwarfHighlightMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
@Mixin(BossBarHud.class)
public class DwarfHighlightBossBarRenderMixin {
	
	@Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/gui/DrawContext;)V")
	private void bossBarRenderStart(DrawContext context, CallbackInfo ci) {
		
		if(!DwarfHighlightMod.check_bars) return;
		
		List<String> usernames_to_remove = new ArrayList<String>();
		
		BossBarHud this_object = (BossBarHud)(Object)this;
		
		for(String username : DwarfHighlightMod.paradox_players_bars.keySet()) {
			if(DwarfHighlightMod.paradox_players_flag.get(username)) {
				usernames_to_remove.add(username);
			}
		}
		
		for(String username : usernames_to_remove) {
			((DwarfHighlightBossBarHudAccessor) this_object).getBossBars().remove(DwarfHighlightMod.paradox_players_bars.get(username));
			DwarfHighlightMod.paradox_players_times.remove(username);
			DwarfHighlightMod.paradox_players_bars.remove(username);
			DwarfHighlightMod.paradox_players_flag.remove(username);
		}
		
	}
	
	@Inject(at = @At("HEAD"), method = "renderBossBar(Lnet/minecraft/client/gui/DrawContext;IILnet/minecraft/entity/boss/BossBar;II)V")
	private void onBossBarRender(DrawContext context, int x, int y, BossBar bossBar, int width, int height, CallbackInfo ci) {
		
		String boss_bar_name = bossBar.getName().getString().toLowerCase();
		
		if(boss_bar_name.length() == 0) {
			
		}else if(DwarfHighlightMod.show_sirius && boss_bar_name.startsWith("sirius, the final herald")){
			float percent = bossBar.getPercent();
			long currentTime = Instant.now().getEpochSecond();
			long timePassed = currentTime - DwarfHighlightMod.sirius_start_time;
			Text newBossBarName = Text.literal("Sirius, the Final Herald (" + Math.round((percent*100.0)/2.5) + ") " + (int)(timePassed/60) + ":" + String.format("%02d", timePassed%60)).formatted(Formatting.BOLD).formatted(Formatting.DARK_AQUA);
			bossBar.setName(newBossBarName);
			//DwarfHighlightMod.LOGGER.info(boss_bar_name);	
		// Declarations and their win/lose amounts.
		// gotta find core changes
		}else if(DwarfHighlightMod.show_sirius && boss_bar_name.contains("remaining crowned blight")) {
			String numberRemaining = boss_bar_name.substring(0, boss_bar_name.indexOf(" "));
			bossBar.setName(Text.literal(numberRemaining  + " Remaining Crowned Blight! " + getHpChangeValues(-1, 5, 15)).setStyle(bossBar.getName().getStyle()));
		}else if(DwarfHighlightMod.show_sirius &&  boss_bar_name.contains("core exposed")) {
			bossBar.setName(Text.literal("Core Exposed " + getHpChangeValues(-1, 5, 19)).setStyle(bossBar.getName().getStyle()));
		}else if(DwarfHighlightMod.show_sirius &&  boss_bar_name.endsWith("star energy remaining!")) {
			//Max Duration is 10s (?)
			// 2 / -5			
			// Fix by matching above.
			String numberRemaining = boss_bar_name.substring(0, boss_bar_name.indexOf(" "));
			bossBar.setName(Text.literal(numberRemaining+ " Star Energy Remaining! " + getHpChangeValues(-2, 5, 10)).setStyle(bossBar.getName().getStyle()));
		}else if(DwarfHighlightMod.show_sirius &&  boss_bar_name.contains("channeling power behind the tomb")) {
			//Duration is 12s
			// 1 / -5
			// Not working for some reason?! Doesn't detect the name for some reason. Maybe constant render overwrite?
			bossBar.setName(Text.literal("Channeling Power Behind the Tomb " + getHpChangeValuesWin(-1, 5, 12)).setStyle(bossBar.getName().getStyle()));
		}else if(boss_bar_name.equals("silver construct")) {
			
			if(DwarfHighlightMod.silver_construct_phase == 0 && bossBar.getPercent() <= .666 && bossBar.getPercent() >= .34) {
				DwarfHighlightMod.silver_construct_phase = 1;
			}else if(DwarfHighlightMod.silver_construct_phase == 1 && bossBar.getPercent() <= .333) {
				DwarfHighlightMod.silver_construct_phase = 2;
				for(String username : DwarfHighlightMod.paradox_players_bars.keySet()) {
					DwarfHighlightMod.paradox_players_flag.replace(username, true);
				}
				DwarfHighlightMod.watch_for_next_clear = true;
				DwarfHighlightMod.check_bars = true;
			}else if(DwarfHighlightMod.watch_for_next_clear && bossBar.getPercent() <= .25) {
				/*for(String username : DwarfHighlightMod.paradox_players_bars.keySet()) {
					DwarfHighlightMod.paradox_players_flag.replace(username, true);
				}
				DwarfHighlightMod.watch_for_next_clear = false;
				DwarfHighlightMod.check_bars = true;*/
				DwarfHighlightMod.watch_for_next_clear = false;
			}else if(DwarfHighlightMod.silver_construct_phase == 2 && bossBar.getPercent() <= 0.5) {
				DwarfHighlightMod.silver_construct_phase = 0;
			
				for(String username : DwarfHighlightMod.paradox_players_bars.keySet()) {
					DwarfHighlightMod.paradox_players_flag.replace(username, true);
				}
			
				DwarfHighlightMod.check_bars = true;
			}
			
		}else {
			for(String username : DwarfHighlightMod.paradox_players_bars.keySet()) {
				if(boss_bar_name.startsWith(username.toLowerCase())) {
					
					int time_remaining = 30 - Math.round(Instant.now().getEpochSecond() - DwarfHighlightMod.paradox_players_times.get(username));
					float percent_time_left = (float)(time_remaining)/(float)(30);
					bossBar.setName(Text.literal(username + " paradoxed ["+time_remaining+"]"));
					bossBar.setPercent(percent_time_left);
					if(time_remaining == 0) {
						DwarfHighlightMod.paradox_players_flag.replace(username, true);
						DwarfHighlightMod.check_bars = true;
					}					
				}
			}
		}
	}
	
	private static String getHpChangeValuesWin(int win, int lose, int duration) {
		
		double multiplier = 1.0;
		
		long currentTime = Instant.now().getEpochSecond() + duration;
		long timePassed = currentTime - DwarfHighlightMod.sirius_start_time;
		
		if (timePassed > 1200) {
			return "";
		}
		
		multiplier += (double)(Math.floor(timePassed/90)) * 0.5;
		
		int onWin = (int) (win*multiplier);
		int onLose = (int) (lose*multiplier);
		
		return "["+onWin+"/"+onLose+"]";
		
	}
	
	private String getHpChangeValues(int win, int lose, int duration) {
		
		double multiplierLose = 1.0;
		double multiplierWin = 1.0;
		
		long currentTime = Instant.now().getEpochSecond() + duration;
		long currentTimeWin = Instant.now().getEpochSecond();
		long timePassed = currentTime - DwarfHighlightMod.sirius_start_time;
		
		multiplierLose += (double)(Math.floor(timePassed/90)) * 0.5;
		multiplierWin += (double)(Math.floor((currentTimeWin - DwarfHighlightMod.sirius_start_time)/90)) * 0.5;
		
		int onWin = (int) (win*multiplierWin);
		int onLose = (int) (lose*multiplierLose);
		
		return "["+onWin+"/"+onLose+"]";
		
	}
	
}
