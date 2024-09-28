package net.dwarfhighlight.suggestions;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

public class CurrencyConverterSuggestions {

	
	public static CompletableFuture<Suggestions> getHyperSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"hcs")) {
			builder.suggest("hcs");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"hxp")) {
			builder.suggest("hxp");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"har")) {
			builder.suggest("har");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> hyperSUGGESTION_PROVIDER = (context, builder) -> {
		return getHyperSuggestions(context,builder);
    };

	
	public static CompletableFuture<Suggestions> getCompressedSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"ar")) {
			builder.suggest("ar");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"cxp")) {
			builder.suggest("cxp");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"ccs")) {
			builder.suggest("ccs");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> compressedSUGGESTION_PROVIDER = (context, builder) -> {
		return getCompressedSuggestions(context,builder);
    };   
}