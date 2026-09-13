package com.vermenia.nailong.network;

import com.vermenia.nailong.NailongMod;
import com.vermenia.nailong.entity.NailongEntity;
import com.vermenia.nailong.inventory.NailongMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record NailongRenamePacket(int entityId, String name) implements CustomPacketPayload {
    public static final Type<NailongRenamePacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(NailongMod.MOD_ID, "rename_nailong"));

    public static final StreamCodec<ByteBuf, NailongRenamePacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            NailongRenamePacket::entityId,
            ByteBufCodecs.STRING_UTF8,
            NailongRenamePacket::name,
            NailongRenamePacket::new
        );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void send(int entityId, String name) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new NailongRenamePacket(entityId, name));
    }

    public static class Handler implements IPayloadHandler<NailongRenamePacket> {
        @Override
        public void handle(NailongRenamePacket packet, IPayloadContext context) {
            context.enqueueWork(() -> {
                if (context.player() instanceof ServerPlayer player) {
                    Entity entity = player.level().getEntity(packet.entityId());
                    if (entity instanceof NailongEntity nailong && nailong.isOwnedBy(player)
                            && player.containerMenu instanceof NailongMenu menu
                            && menu.getNailong() == nailong && menu.stillValid(player)
                            && packet.name().length() <= 32) {
                        String name = packet.name().strip();
                        nailong.setCustomName(name.isEmpty() ? null : net.minecraft.network.chat.Component.literal(name));
                        nailong.setCustomNameVisible(true);
                    }
                }
            });
        }
    }
}
