package com.vermenia.nailong.registry;

import com.vermenia.nailong.NailongMod;
import com.vermenia.nailong.entity.NailongEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NailongEntities {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NailongMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<NailongEntity>> NAILONG =
            ENTITY_TYPES.register("nailong", () -> EntityType.Builder
                    .of(NailongEntity::new, MobCategory.CREATURE)
                    .sized(1.15F, 2.5F)
                    .eyeHeight(2.15F)
                    .clientTrackingRange(10)
                    .build(NailongMod.MOD_ID + ":nailong"));

    private NailongEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener(NailongEntities::createAttributes);
    }

    private static void createAttributes(EntityAttributeCreationEvent event) {
        event.put(NAILONG.get(), NailongEntity.createAttributes().build());
    }
}
