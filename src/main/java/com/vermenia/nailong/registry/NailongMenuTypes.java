package com.vermenia.nailong.registry;

import com.vermenia.nailong.NailongMod;
import com.vermenia.nailong.inventory.NailongMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class NailongMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(Registries.MENU, NailongMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<NailongMenu>> NAILONG_MENU =
        MENU_TYPES.register("nailong_menu", () -> IMenuTypeExtension.create(NailongMenu::new));

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
