package com.vermenia.nailong.inventory;

import com.vermenia.nailong.entity.NailongEntity;
import com.vermenia.nailong.registry.NailongMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NailongMenu extends AbstractContainerMenu {
    private final Container nailongInventory;
    private final NailongEntity nailong;

    public NailongMenu(int containerId, Inventory playerInventory, Container nailongInventory, NailongEntity nailong) {
        super(NailongMenuTypes.NAILONG_MENU.get(), containerId);
        this.nailongInventory = nailongInventory;
        this.nailong = nailong;

        nailongInventory.startOpen(playerInventory.player);

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(nailongInventory, i, 8 + i * 18, 18));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    public NailongMenu(int containerId, Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new SimpleContainer(9), resolveNailong(playerInventory, buf));
    }

    private static NailongEntity resolveNailong(Inventory playerInventory, net.minecraft.network.RegistryFriendlyByteBuf buf) {
        var entity = playerInventory.player.level().getEntity(buf.readVarInt());
        return entity instanceof NailongEntity nailong ? nailong : null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < 9) {
                if (!this.moveItemStackTo(stack, 9, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, 0, 9, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.nailong != null && this.nailong.isAlive() && this.nailong.distanceTo(player) < 8.0F;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.nailongInventory.stopOpen(player);
    }

    public NailongEntity getNailong() {
        return this.nailong;
    }
}
