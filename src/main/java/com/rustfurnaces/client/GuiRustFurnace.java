package com.rustfurnaces.client;

import com.rustfurnaces.ContainerRustFurnace;
import com.rustfurnaces.RustFurnaces;
import com.rustfurnaces.network.MsgToggle;
import java.io.IOException;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

/** Интерфейс печи как в Rust: FUEL -> INPUT x2 -> OUTPUT x3, снизу CONTROLS и TURN ON. */
public class GuiRustFurnace extends GuiContainer {
    private final ContainerRustFurnace container;
    private final boolean admin;

    public GuiRustFurnace(ContainerRustFurnace container, boolean admin) {
        super(container);
        this.container = container;
        this.admin = admin;
        this.xSize = 200;
        this.ySize = 244;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = guiLeft;
        int y = guiTop;
        boolean on = container.fields[0] != 0;

        RustGui.rect(x, y, x + xSize, y + ySize, RustGui.BG);
        RustGui.title(fontRenderer, admin ? "ADMIN FURNACE" : "FURNACE", x + 8, y + 6, RustGui.TITLE);
        if (admin) {
            String hint = "SHIFT + LMB = SETTINGS";
            fontRenderer.drawString(hint, x + 194 - fontRenderer.getStringWidth(hint), y + 12, RustGui.LABEL);
        }

        RustGui.rect(x + 6, y + 28, x + 194, y + 134, RustGui.PANEL);
        fontRenderer.drawString("FUEL", x + 14, y + 40, RustGui.LABEL);
        fontRenderer.drawString("INPUT", x + 14, y + 76, RustGui.LABEL);
        fontRenderer.drawString("OUTPUT", x + 14, y + 116, RustGui.LABEL);

        // FUEL
        RustGui.bigSlot(x + 92, y + 36);
        if (on) RustGui.border(x + 88, y + 32, x + 112, y + 56, RustGui.ORANGE);
        RustGui.arrowDown(x + 100, y + 57, y + 67, RustGui.LINE);

        // INPUT + прогресс
        RustGui.bigSlot(x + 78, y + 72);
        RustGui.bigSlot(x + 106, y + 72);
        for (int i = 0; i < 2; i++) {
            int bx = x + 75 + 28 * i;
            RustGui.rect(bx, y + 92, bx + 22, y + 94, 0x60000000);
            int max = container.fields[3 + i];
            int p = container.fields[1 + i];
            if (max > 0 && p > 0) {
                RustGui.rect(bx, y + 92, bx + Math.min(22, 22 * p / max), y + 94, RustGui.ORANGE);
            }
        }

        // скобка и стрелка к OUTPUT
        RustGui.rect(x + 85, y + 94, x + 87, y + 100, RustGui.LINE);
        RustGui.rect(x + 113, y + 94, x + 115, y + 100, RustGui.LINE);
        RustGui.rect(x + 85, y + 100, x + 115, y + 102, RustGui.LINE);
        RustGui.arrowDown(x + 100, y + 102, y + 108, RustGui.LINE);

        // OUTPUT (пустые ячейки)
        for (int j = 0; j < 3; j++) {
            RustGui.bigSlot(x + 64 + 28 * j, y + 112);
        }

        // CONTROLS
        RustGui.rect(x + 6, y + 137, x + 194, y + 155, RustGui.PANEL);
        fontRenderer.drawString("CONTROLS", x + 12, y + 143, RustGui.LABEL);
        boolean hover = RustGui.inside(mouseX, mouseY, x + 128, y + 138, x + 192, y + 154);
        if (on) {
            RustGui.button(fontRenderer, x + 128, y + 138, x + 192, y + 154, "TURN OFF", hover ? RustGui.RED_HOVER : RustGui.RED, RustGui.RED_TEXT);
        } else {
            RustGui.button(fontRenderer, x + 128, y + 138, x + 192, y + 154, "TURN ON", hover ? RustGui.GREEN_HOVER : RustGui.GREEN, RustGui.GREEN_TEXT);
        }

        // инвентарь игрока
        RustGui.rect(x + 6, y + 157, x + 194, y + 240, RustGui.PANEL);
        for (int i = 6; i < container.inventorySlots.size(); i++) {
            Slot s = container.inventorySlots.get(i);
            RustGui.smallSlot(x + s.xPos, y + s.yPos);
        }
        RustGui.resetColor();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && RustGui.inside(mouseX, mouseY, guiLeft + 128, guiTop + 138, guiLeft + 192, guiTop + 154)) {
            RustFurnaces.NET.sendToServer(new MsgToggle());
            RustGui.click();
            return;
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
