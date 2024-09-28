package net.dwarfhighlight.suggestions;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.dwarfhighlight.DwarfHighlightMod;
import net.dwarfhighlight.DwarfHighlighterModMenuIntegration;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

public class EditTitleSuggestions {

	public static CompletableFuture<Suggestions> getListSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"private")) {
			builder.suggest("private");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"tcl")) {
			builder.suggest("tcl");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> listSUGGESTION_PROVIDER = (context, builder) -> {
		return getListSuggestions(context,builder);
    };
    
    
    public static CompletableFuture<Suggestions> getProfileSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		for(String profile_name : DwarfHighlightMod.json_weights.keySet()) {
			
			if(profile_name.equals("enabled")) continue;
			
			if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(), "\""+profile_name+"\"")) {
				builder.suggest("\""+profile_name+"\"");
			}
			
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> ProfileSUGGESTION_PROVIDER = (context, builder) -> {
		return getProfileSuggestions(context,builder);
    };
    
    
    public static CompletableFuture<Suggestions> getTitleSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		String List = context.getArgument("List", String.class);
		
		if(List.equals("private")) {
			
			for(String item : DwarfHighlighterModMenuIntegration.private_list_titles.keySet()) {
				
				if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(), "\""+item+"\"")) {
					builder.suggest("\""+item+"\"");
				}
					
			}
			
		}
		
		if(List.equals("tcl")) {
			
			for(String item : DwarfHighlighterModMenuIntegration.guild_list_titles.keySet()) {
				
				if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(), "\""+item+"\"")) {
					builder.suggest("\""+item+"\"");
				}
				
			}
			
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> titleSUGGESTION_PROVIDER = (context, builder) -> {
		return getTitleSuggestions(context,builder);
    }; 
	
}
