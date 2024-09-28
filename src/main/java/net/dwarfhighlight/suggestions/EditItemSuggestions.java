package net.dwarfhighlight.suggestions;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.dwarfhighlight.DwarfHighlightMod;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;

public class EditItemSuggestions {

	
	public static CompletableFuture<Suggestions> getListSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"private")) {
			builder.suggest("private");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"tcl")) {
			builder.suggest("tcl");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"both")) {
			builder.suggest("both");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> listSUGGESTION_PROVIDER = (context, builder) -> {
		return getListSuggestions(context,builder);
    };

	
	public static CompletableFuture<Suggestions> getEditTypeSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"rename")) {
			builder.suggest("rename");
		}
		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),"quantity")) {
			builder.suggest("quantity");
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> editTypeSUGGESTION_PROVIDER = (context, builder) -> {
		return getEditTypeSuggestions(context,builder);
    };
    
    
	public static CompletableFuture<Suggestions> getItemSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
		
		String List = context.getArgument("List", String.class);
		
		if(List.equals("private")) {
			
			for(String item : DwarfHighlightMod.needed_personal_items.keySet()) {
				
				if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(), "\""+item+"\"")) {
					builder.suggest("\""+item+"\"");
				}
					
			}
			
		}
		
		if(List.equals("tcl")) {
			
			for(String item : DwarfHighlightMod.needed_tcl_items.keySet()) {
				
				if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(), "\""+item+"\"")) {
					builder.suggest("\""+item+"\"");
				}
				
			}
			
		}
		
		if(List.equals("both")) {
			
			for(String item : DwarfHighlightMod.needed_items.keySet()) {
				
				if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(), "\""+item+"\"")) {
					builder.suggest("\""+item+"\"");
				}
				
			}
			
		}
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> itemSUGGESTION_PROVIDER = (context, builder) -> {
		return getItemSuggestions(context,builder);
    }; 
    
    public static CompletableFuture<Suggestions> getCharmSuggestions(CommandContext<FabricClientCommandSource> context,
			SuggestionsBuilder builder) throws CommandSyntaxException {
    	
    	for(String effect : DwarfHighlightMod.charm_maxes.charm_maxes.keySet()) {
    		
    		String editEffect = "\""+effect+"\"";
    		
    		if(CommandSource.shouldSuggest(builder.getRemainingLowerCase(),editEffect)) {
    			builder.suggest(editEffect);
    		}
    	}		
		
		return builder.buildFuture();
	}
	
	
    public final static SuggestionProvider<FabricClientCommandSource> charmSUGGESTION_PROVIDER = (context, builder) -> {
		return getCharmSuggestions(context,builder);
    };
}