package net.dwarfhighlight.mixin;

import java.util.Map;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;

@Mixin(BossBarHud.class)
public interface DwarfHighlightBossBarHudAccessor {

	@Accessor
	Map<UUID, ClientBossBar> getBossBars();
	
}
