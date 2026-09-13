package com.vermenia.nailong.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vermenia.nailong.NailongMod;
import com.vermenia.nailong.entity.NailongEntity;
import com.vermenia.nailong.inventory.NailongMenu;
import com.vermenia.nailong.network.NailongRenamePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class NailongScreen extends AbstractContainerScreen<NailongMenu> {
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath(NailongMod.MOD_ID, "textures/gui/nailong_inventory.png");
    private EditBox nameField;

    public NailongScreen(NailongMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 133;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        NailongEntity nailong = this.menu.getNailong();
        if (nailong != null) {
            this.nameField = new EditBox(this.font, this.leftPos + 8, this.topPos + 6, 160, 10, Component.literal("名称"));
            this.nameField.setBordered(false);
            this.nameField.setTextColor(0x404040);
            this.nameField.setTextColorUneditable(0x404040);
            this.nameField.setEditable(nailong.isOwnedBy(this.minecraft.player));
            this.nameField.setMaxLength(32);
            this.nameField.setValue(nailong.getName().getString());
            this.nameField.setResponder(this::onNameChanged);
            this.addRenderableWidget(this.nameField);
        }
    }

    private void onNameChanged(String newName) {
        if (this.menu.getNailong() != null) {
            NailongRenamePacket.send(this.menu.getNailong().getId(), newName);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.nameField != null && this.nameField.isFocused() && keyCode != 256) {
            // Let text editing consume inventory hotkeys (for example E).
            if (this.nameField.keyPressed(keyCode, scanCode, modifiers) || this.nameField.canConsumeInput()) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight,
                this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xC6C6C6, false);
    }
}
