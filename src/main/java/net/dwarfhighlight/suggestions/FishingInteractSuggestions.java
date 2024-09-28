package net.dwarfhighlight.suggestions;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

public class FishingInteractSuggestions {

	
	public static CompletableFuture<Suggestions> getInteractionSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"minigame")) {
			builder.suggest("minigame");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"combat")) {
			builder.suggest("combat");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> interactionSUGGESTION_PROVIDER = (context, builder) -> {
		return getInteractionSuggestions(context,builder);
    };

	
	public static CompletableFuture<Suggestions> getItemRecvSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"cache")) {
			builder.suggest("cache");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"salmon")) {
			builder.suggest("salmon");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"flounder")) {
			builder.suggest("flounder");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"carp")) {
			builder.suggest("carp");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"sardine")) {
			builder.suggest("sardine");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"trout")) {
			builder.suggest("trout");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"seabass")) {
			builder.suggest("seabass");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"shroomfish")) {
			builder.suggest("shroomfish");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"mungfish")) {
			builder.suggest("mungfish");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"monkfish")) {
			builder.suggest("monkfish");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> itemRecvSUGGESTION_PROVIDER = (context, builder) -> {
		return getItemRecvSuggestions(context,builder);
    };
    
    
	public static CompletableFuture<Suggestions> getNumberSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"0")) {
			builder.suggest("0");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"1")) {
			builder.suggest("1");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"3")) {
			builder.suggest("3");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"4")) {
			builder.suggest("4");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"5")) {
			builder.suggest("5");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> numberSUGGESTION_PROVIDER = (context, builder) -> {
		return getNumberSuggestions(context,builder);
    }; 
}