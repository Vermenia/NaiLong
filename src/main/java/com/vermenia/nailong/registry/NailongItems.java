package com.vermenia.nailong.registry;

import com.vermenia.nailong.NailongMod;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NailongItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NailongMod.MOD_ID);

    public static final DeferredItem<SpawnEggItem> NAILONG_SPAWN_EGG = ITEMS.registerItem(
            "nailong_spawn_egg",
            // The custom sprite carries its own colors; vanilla egg tinting must stay neutral.
            properties -> new SpawnEggItem(NailongEntities.NAILONG.get(), 0xFFFFFF, 0xFFFFFF, properties),
            new Item.Properties());

    private NailongItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(NailongItems::addCreativeTabContents);
    }

    private static void addCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(NAILONG_SPAWN_EGG.get());
        }
    }
}
