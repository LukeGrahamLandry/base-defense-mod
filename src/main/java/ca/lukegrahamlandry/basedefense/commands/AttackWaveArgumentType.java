package ca.lukegrahamlandry.basedefense.commands;

import ca.lukegrahamlandry.basedefense.base.attacks.AttackWaveType;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AttackWaveArgumentType extends ResourceLocationArgument {
    private static final DynamicCommandExceptionType INVALID = new DynamicCommandExceptionType((p_106991_) -> {
        return Component.literal("error.basedefense.invalid_attack_wave");
    });

    public static ResourceLocation get(CommandContext<CommandSourceStack> p_107002_, String p_107003_) throws CommandSyntaxException {
        ResourceLocation resourcelocation = getId(p_107002_, p_107003_);
        if (!options().contains(resourcelocation)) {
            throw INVALID.create(resourcelocation);
        } else {
            return resourcelocation;
        }
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader stringreader = new StringReader(builder.getInput());
        stringreader.setCursor(builder.getStart());
        String s = stringreader.getRemaining();
        stringreader.setCursor(stringreader.getTotalLength());

        stringreader.skipWhitespace();

        for (ResourceLocation check : options()){
            if (check.toString().startsWith(s)) builder.suggest(check.toString());
        }

        return builder.buildFuture();
    }

    private static Set<ResourceLocation> options(){
        return AttackWaveType.DATA.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public Collection<String> getExamples() {
        return new ArrayList<>();
    }
}