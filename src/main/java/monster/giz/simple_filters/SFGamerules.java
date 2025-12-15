package monster.giz.simple_filters;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.world.rule.*;

import java.util.function.ToIntFunction;

public class SFGamerules {

    public static final GameRule<Boolean> FILTER_EMPTY_ACCEPT_ALL =
            registerBooleanRule("empty_filter_accepts_any", GameRuleCategory.MISC, true);

    public static final GameRule<Integer> FILTER_COMPARATOR_OUTPUT_TICKS =
            registerIntRule("filter_comparator_output_ticks", GameRuleCategory.MISC, 10, 0, 40);


    private static GameRule<Boolean> registerBooleanRule(String name, GameRuleCategory category, boolean defaultValue) {
        return register(name, category, GameRuleType.BOOL, BoolArgumentType.bool(), Codec.BOOL, defaultValue, FeatureSet.empty(), GameRuleVisitor::visitBoolean, (value) -> value ? 1 : 0);
    }

    private static GameRule<Integer> registerIntRule(String name, GameRuleCategory category, int defaultValue, int minValue, int maxValue) {
        return registerIntRule(name, category, defaultValue, minValue, maxValue, FeatureSet.empty());
    }

    private static GameRule<Integer> registerIntRule(String name, GameRuleCategory category, int defaultValue, int minValue, int maxValue, FeatureSet requiredFeatures) {
        return register(name, category, GameRuleType.INT, IntegerArgumentType.integer(minValue, maxValue), Codec.intRange(minValue, maxValue), defaultValue, requiredFeatures, GameRuleVisitor::visitInt, (value) -> value);
    }

    private static <T> GameRule<T> register(
            String name,
            GameRuleCategory category,
            GameRuleType type,
            ArgumentType<T> argumentType,
            Codec<T> codec,
            T defaultValue,
            FeatureSet requiredFeatures,
            GameRules.Acceptor<T> acceptor,
            ToIntFunction<T> commandResultSupplier
    ) {
        return Registry.register(
                Registries.GAME_RULE,
                SimpleFilters.idOf(name),
                new GameRule<>(
                        category,
                        type,
                        argumentType,
                        acceptor,
                        codec,
                        commandResultSupplier,
                        defaultValue,
                        requiredFeatures
                )
        );
    }

    public static void register() {

    }

}
