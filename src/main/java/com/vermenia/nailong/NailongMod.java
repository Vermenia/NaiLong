package com.vermenia.nailong;

import com.mojang.logging.LogUtils;
import com.vermenia.nailong.ai.NailongChatListener;
import com.vermenia.nailong.command.NailongCommand;
import com.vermenia.nailong.network.NailongRenamePacket;
import com.vermenia.nailong.registry.NailongEntities;
import com.vermenia.nailong.registry.NailongItems;
import com.vermenia.nailong.registry.NailongMenuTypes;
import org.slf4j.Logger;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(NailongMod.MOD_ID)
public final class NailongMod {
    public static final String MOD_ID = "nailong";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NailongMod(IEventBus modEventBus, ModContainer modContainer) {
        NailongEntities.register(modEventBus);
        NailongItems.register(modEventBus);
        NailongMenuTypes.register(modEventBus);
        modEventBus.addListener(NailongMod::onRegisterPayloads);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, NailongChatListener::onChat);
        NeoForge.EVENT_BUS.addListener(NailongMod::onRegisterCommands);
    }

    private static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar.playToServer(
            NailongRenamePacket.TYPE,
            NailongRenamePacket.STREAM_CODEC,
            new NailongRenamePacket.Handler()
        );
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        NailongCommand.register(event.getDispatcher());
        LOGGER.info("已注册奶龙命令");
    }
}
