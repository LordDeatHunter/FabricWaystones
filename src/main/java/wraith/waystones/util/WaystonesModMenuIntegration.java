package wraith.waystones.util;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.gui.entries.FloatListEntry;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

import java.util.function.Supplier;

public class WaystonesModMenuIntegration implements ModMenuApi {

    public static final Supplier<Screen> getConfigScreen(Screen parent) {
        Config config = Config.getInstance();
        return () -> {
            ConfigBuilder builder = ConfigBuilder.create();
            builder.setParentScreen(parent);
            builder.setTitle(new TranslatableText("text.autoconfig.waystones.title"));
            builder.setSavingRunnable(() -> {
                config.saveConfig();
            });
            ConfigCategory general = builder
                    .getOrCreateCategory(new TranslatableText("text.autoconfig.waystones.category.general"));
            BooleanListEntry generateInVillages = builder.entryBuilder()
                    .startBooleanToggle(new TranslatableText("text.autoconfig.waystones.option.generate_in_villages"),
                            config.generateInVillages())
                    .setDefaultValue(true)
                    .setTooltip(new TranslatableText("text.autoconfig.waystones.option.generate_in_villages.@Tooltip"))
                    .setSaveConsumer(newValue -> config.configData.putBoolean("generate_in_villages", newValue))
                    .build();
            BooleanListEntry consumeInfiniteKnowledgeScrollOnUse = builder.entryBuilder()
                    .startBooleanToggle(
                            new TranslatableText(
                                    "text.autoconfig.waystones.option.consume_infinite_knowledge_scroll_on_use"),
                            config.consumeInfiniteScroll())
                    .setDefaultValue(false)
                    .setTooltip(new TranslatableText(
                            "text.autoconfig.waystones.option.consume_infinite_knowledge_scroll_on_use.@Tooltip"))
                    .setSaveConsumer(newValue -> config.configData
                            .putBoolean("consume_infinite_knowledge_scroll_on_use", newValue))
                    .build();
            BooleanListEntry canOwnersRedeemPayments = builder.entryBuilder()
                    .startBooleanToggle(
                            new TranslatableText("text.autoconfig.waystones.option.can_owners_redeem_payments"),
                            config.canOwnersRedeemPayments())
                    .setDefaultValue(false)
                    .setTooltip(new TranslatableText(
                            "text.autoconfig.waystones.option.can_owners_redeem_payments.@Tooltip"))
                    .setSaveConsumer(newValue -> config.configData.putBoolean("can_owners_redeem_payments", newValue))
                    .build();
            IntegerListEntry costAmount = builder.entryBuilder()
                    .startIntField(new TranslatableText("text.autoconfig.waystones.option.cost_amount"),
                            config.teleportCost())
                    .setDefaultValue(1)
                    .setTooltip(new TranslatableText("text.autoconfig.waystones.option.cost_amount.@Tooltip"))
                    .setSaveConsumer(newValue -> config.configData.putInt("cost_amount", newValue)).build();
            StringListEntry costType = builder.entryBuilder()
                    .startStrField(new TranslatableText("text.autoconfig.waystones.option.cost_type"),
                            config.teleportType())
                    .setDefaultValue("level")
                    .setTooltip(new TranslatableText("text.autoconfig.waystones.option.cost_type.@Tooltip"))
                    .setSaveConsumer(newValue -> config.configData.putString("cost_type", newValue)).build();
            StringListEntry costItem = builder.entryBuilder()
                    .startStrField(new TranslatableText("text.autoconfig.waystones.option.cost_item"),
                            config.configData.getString("cost_item"))
                    .setDefaultValue("minecraft:ender_pearl")
                    .setTooltip(new TranslatableText("text.autoconfig.waystones.option.cost_item.@Tooltip"))
                    .setSaveConsumer(newValue -> config.configData.putString("cost_item", newValue)).build();
            FloatListEntry waystoneBlockHardness = builder.entryBuilder()
                    .startFloatField(new TranslatableText("text.autoconfig.waystones.option.waystone_block_hardness"),
                            config.getHardness())
                    .setDefaultValue(4.0f)
                    .setTooltip(
                            new TranslatableText("text.autoconfig.waystones.option.waystone_block_hardness.@Tooltip"))
                    .setSaveConsumer(newValue -> config.configData.putFloat("waystone_block_hardness", newValue))
                    .build();
            IntegerListEntry waystoneBlockRequiredMiningLevel = builder.entryBuilder()
                    .startIntField(
                            new TranslatableText(
                                    "text.autoconfig.waystones.option.waystone_block_required_mining_level"),
                            config.getMiningLevel())
                    .setDefaultValue(1)
                    .setTooltip(new TranslatableText(
                            "text.autoconfig.waystones.option.waystone_block_required_mining_level.@Tooltip"))
                    .setSaveConsumer(
                            newValue -> config.configData.putInt("waystone_block_required_mining_level", newValue))
                    .build();
            general.addEntry(generateInVillages);
            general.addEntry(consumeInfiniteKnowledgeScrollOnUse);
            general.addEntry(canOwnersRedeemPayments);
            general.addEntry(costAmount);
            general.addEntry(costType);
            general.addEntry(costItem);
            general.addEntry(waystoneBlockHardness);
            general.addEntry(waystoneBlockRequiredMiningLevel);
            Screen screen = builder.build();
            return screen;
        };
    }

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            return getConfigScreen(parent).get();
        };
    }

}
