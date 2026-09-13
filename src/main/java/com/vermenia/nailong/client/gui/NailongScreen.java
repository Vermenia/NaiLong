package com.vermenia.nailong.client.gui;

import com.vermenia.nailong.inventory.NailongMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Compatibility alias; keep all screen behavior in the registered implementation. */
@Deprecated
public class NailongScreen extends com.vermenia.nailong.client.screen.NailongScreen {
    public NailongScreen(NailongMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
