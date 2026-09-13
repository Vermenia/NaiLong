package com.vermenia.nailong.ai;

import com.vermenia.nailong.NailongMod;
import com.vermenia.nailong.entity.NailongEntity;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.ServerChatEvent;

public final class NailongChatListener {
    private static final double RANGE = 8.0D;
    private static final Set<UUID> BUSY = new HashSet<>();

    private NailongChatListener() {}

    public static void onChat(ServerChatEvent event) {
        String message = event.getRawText();
        if (event.isCanceled()) return;
        ServerPlayer player = event.getPlayer();
        if (player.isRemoved()) return;

        // Check if message contains "奶龙" or any nearby Nailong's custom name
        boolean shouldRespond = message.contains("奶龙");
        if (!shouldRespond) {
            for (NailongEntity entity : player.level().getEntitiesOfClass(NailongEntity.class,
                    player.getBoundingBox().inflate(RANGE),
                    e -> e.isAlive() && e.hasCustomName())) {
                if (message.contains(entity.getCustomName().getString())) {
                    shouldRespond = true;
                    break;
                }
            }
        }

        if (shouldRespond) respond(player, message);
    }

    private static void respond(ServerPlayer player, String message) {
        NailongEntity nailong = player.level().getEntitiesOfClass(NailongEntity.class,
                        player.getBoundingBox().inflate(RANGE),
                        entity -> entity.isAlive() && player.distanceToSqr(entity) <= RANGE * RANGE)
                .stream().min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
        if (nailong == null) return;

        if (message.codePointCount(0, message.length()) > 160) {
            player.sendSystemMessage(nailong.getName().copy().append("：说短一点嘛，一次不要超过 160 个字哦。"));
            return;
        }

        // Bound the shared CPU model's queue, and avoid multiple requests for the same creature.
        if (BUSY.size() >= 4 || !BUSY.add(nailong.getUUID())) return;

        String nailongName = nailong.getCustomName() != null ? nailong.getCustomName().getString() : null;

        NailongChatService.ask(nailong.getUUID(), player.getUUID(), message, nailongName)
                .whenComplete((reply, failure) -> player.server.execute(() -> {
                    BUSY.remove(nailong.getUUID());
                    if (nailong.isRemoved() || !nailong.isAlive() || player.isRemoved()
                            || nailong.level() != player.level()
                            || player.distanceToSqr(nailong) > RANGE * RANGE) return;
                    if (failure != null) {
                        NailongMod.LOGGER.warn("Nailong could not answer player chat", failure);
                        player.sendSystemMessage(nailong.getName().copy().append("：我有点迷糊，等一下再和我说吧。"));
                        return;
                    }

                    // 如果回复包含配置提示，只发给发起者
                    if (reply.contains("[系统提示]")) {
                        player.sendSystemMessage(Component.literal(reply));
                    } else {
                        // 正常回复广播给附近玩家
                        Component answer = Component.literal("<").append(nailong.getName()).append("> " + reply);
                        for (ServerPlayer listener : player.serverLevel().players()) {
                            if (listener.distanceToSqr(nailong) <= RANGE * RANGE) {
                                listener.sendSystemMessage(answer);
                            }
                        }
                    }
                }));
    }
}
