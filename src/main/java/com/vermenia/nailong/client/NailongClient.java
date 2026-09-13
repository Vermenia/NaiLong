package com.vermenia.nailong.client;

import com.vermenia.nailong.NailongMod;
import com.vermenia.nailong.client.render.NailongRenderer;
import com.vermenia.nailong.client.screen.NailongScreen;
import com.vermenia.nailong.registry.NailongEntities;
import com.vermenia.nailong.registry.NailongMenuTypes;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = NailongMod.MOD_ID, value = Dist.CLIENT)
public final class NailongClient {
    private NailongClient() {
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NailongEntities.NAILONG.get(), NailongRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(NailongMenuTypes.NAILONG_MENU.get(), NailongScreen::new);
    }
}
