package com.rustfurnaces.client;

import com.rustfurnaces.ContainerAdminConfig;
import com.rustfurnaces.RecipeStore;
import com.rustfurnaces.RustFurnaces;
import com.rustfurnaces.SmeltRecipe;
import com.rustfurnaces.network.MsgDeleteRecipe;
import com.rustfurnaces.network.MsgLoadRecipe;
import com.rustfurnaces.network.MsgSaveRecipe;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * Настройки ADMIN печи. Слева: INPUT / FUEL / OUTPUT (количество в слоте = количество в рецепте)
 * и время плавки. SAVE сохраняет сейв для всех печей. Справа: список сейвов (EDIT / DELETE).
 */
public class GuiAdminConfig extends GuiContainer {
    private static final int ROWS = 7;

    private final ContainerAdminConfig container;
    private GuiTextField timeField;
    private String timeText = "5";
    private String selectedId;
    private int scroll;

    public GuiAdminConfig(ContainerAdminConfig container) {
        super(container);
        this.container = container;
        this.xSize = 360;
        this.ySize = 244;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        if (timeField != null) timeText = timeField.getText();
        timeField = new GuiTextField(0, fontRenderer, guiLeft + 78, guiTop + 140, 40, 12);
        timeField.setMaxStringLength(6);
        timeField.setText(timeText);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        timeField.updateCursorCounter();
    }

    private List<SmeltRecipe> recipes() {
        return RecipeStore.get(true);
    }

    private SmeltRecipe selected() {
        if (selectedId == null) return null;
        for (SmeltRecipe r : recipes()) {
            if (r.id.equals(selectedId)) return r;
        }
        return null;
    }

    private static String formatTime(int ticks) {
        if (ticks % 20 == 0) return String.valueOf(ticks / 20);
        return String.valueOf(ticks / 20.0F);
    }

    private void clampScroll() {
        int max = Math.max(0, recipes().size() - ROWS);
        if (scroll > max) scroll = max;
        if (scroll < 0) scroll = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        clampScroll();
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        timeField.drawTextBox();
        renderHoveredToolTip(mouseX, mouseY);

        List<SmeltRecipe> list = recipes();
        for (int i = 0; i < ROWS; i++) {
            int idx = scroll + i;
            if (idx >= list.size()) break;
            SmeltRecipe r = list.get(idx);
            int ry = guiTop + 44 + 22 * i + 2;
            ItemStack hovered = ItemStack.EMPTY;
            if (RustGui.inside(mouseX, mouseY, guiLeft + 204, ry, guiLeft + 220, ry + 16)) hovered = r.input;
            if (RustGui.inside(mouseX, mouseY, guiLeft + 224, ry, guiLeft + 240, ry + 16)) hovered = r.fuel;
            for (int k = 0; k < r.outputs.size(); k++) {
                int ox = guiLeft + 252 + 18 * k;
                if (RustGui.inside(mouseX, mouseY, ox, ry, ox + 16, ry + 16)) hovered = r.outputs.get(k);
            }
            if (!hovered.isEmpty()) renderToolTip(hovered, mouseX, mouseY);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        int x = guiLeft;
        int y = guiTop;

        RustGui.rect(x, y, x + xSize, y + ySize, RustGui.BG);
        RustGui.title(fontRenderer, "ADMIN SETTINGS", x + 8, y + 6, RustGui.TITLE);

        // левая часть: как печь
        RustGui.rect(x + 6, y + 28, x + 194, y + 134, RustGui.PANEL);
        fontRenderer.drawString("FUEL", x + 14, y + 40, RustGui.LABEL);
        fontRenderer.drawString("INPUT", x + 14, y + 76, RustGui.LABEL);
        fontRenderer.drawString("OUTPUT", x + 14, y + 116, RustGui.LABEL);
        RustGui.bigSlot(x + 92, y + 36);
        RustGui.arrowDown(x + 100, y + 57, y + 67, RustGui.LINE);
        RustGui.bigSlot(x + 92, y + 72);
        RustGui.arrowDown(x + 100, y + 94, y + 108, RustGui.LINE);
        for (int j = 0; j < 3; j++) {
            RustGui.bigSlot(x + 64 + 28 * j, y + 112);
        }

        // время + SAVE
        RustGui.rect(x + 6, y + 137, x + 194, y + 155, RustGui.PANEL);
        fontRenderer.drawString("TIME (SEC)", x + 12, y + 143, RustGui.LABEL);
        boolean hSave = RustGui.inside(mouseX, mouseY, x + 128, y + 138, x + 192, y + 154);
        RustGui.button(fontRenderer, x + 128, y + 138, x + 192, y + 154, "SAVE", hSave ? RustGui.GREEN_HOVER : RustGui.GREEN, RustGui.GREEN_TEXT);

        // инвентарь
        RustGui.rect(x + 6, y + 157, x + 194, y + 240, RustGui.PANEL);
        for (int i = 5; i < container.inventorySlots.size(); i++) {
            Slot s = container.inventorySlots.get(i);
            RustGui.smallSlot(x + s.xPos, y + s.yPos);
        }

        // правая часть: список сейвов
        RustGui.rect(x + 200, y + 28, x + 354, y + 240, RustGui.PANEL);
        fontRenderer.drawString("RECIPES", x + 206, y + 33, RustGui.LABEL);
        List<SmeltRecipe> list = recipes();
        for (int i = 0; i < ROWS; i++) {
            int idx = scroll + i;
            if (idx >= list.size()) break;
            int ry = y + 44 + 22 * i;
            boolean sel = list.get(idx).id.equals(selectedId);
            RustGui.rect(x + 202, ry, x + 352, ry + 20, sel ? RustGui.SELECTED : 0x40000000);
        }

        boolean hasSel = selected() != null;
        boolean hEdit = RustGui.inside(mouseX, mouseY, x + 204, y + 202, x + 276, y + 218);
        boolean hDel = RustGui.inside(mouseX, mouseY, x + 280, y + 202, x + 352, y + 218);
        boolean hUp = RustGui.inside(mouseX, mouseY, x + 204, y + 222, x + 276, y + 236);
        boolean hDown = RustGui.inside(mouseX, mouseY, x + 280, y + 222, x + 352, y + 236);
        RustGui.button(fontRenderer, x + 204, y + 202, x + 276, y + 218, "EDIT",
                hasSel && hEdit ? RustGui.GRAY_HOVER : RustGui.GRAY_BTN, hasSel ? RustGui.TITLE : RustGui.LINE);
        RustGui.button(fontRenderer, x + 280, y + 202, x + 352, y + 218, "DELETE",
                hasSel ? (hDel ? RustGui.RED_HOVER : RustGui.RED) : RustGui.GRAY_BTN, hasSel ? RustGui.RED_TEXT : RustGui.LINE);
        RustGui.button(fontRenderer, x + 204, y + 222, x + 276, y + 236, "UP", hUp ? RustGui.GRAY_HOVER : RustGui.GRAY_BTN, RustGui.TITLE);
        RustGui.button(fontRenderer, x + 280, y + 222, x + 352, y + 236, "DOWN", hDown ? RustGui.GRAY_HOVER : RustGui.GRAY_BTN, RustGui.TITLE);
        RustGui.resetColor();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        List<SmeltRecipe> list = recipes();
        if (list.isEmpty()) {
            fontRenderer.drawString("Нет сейвов", 206, 50, RustGui.LINE);
            return;
        }
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < ROWS; i++) {
            int idx = scroll + i;
            if (idx >= list.size()) break;
            SmeltRecipe r = list.get(idx);
            int ry = 44 + 22 * i + 2;
            itemRender.renderItemAndEffectIntoGUI(r.input, 204, ry);
            itemRender.renderItemOverlayIntoGUI(fontRenderer, r.input, 204, ry, null);
            if (!r.fuel.isEmpty()) {
                itemRender.renderItemAndEffectIntoGUI(r.fuel, 224, ry);
                itemRender.renderItemOverlayIntoGUI(fontRenderer, r.fuel, 224, ry, null);
            }
            for (int k = 0; k < r.outputs.size(); k++) {
                ItemStack o = r.outputs.get(k);
                itemRender.renderItemAndEffectIntoGUI(o, 252 + 18 * k, ry);
                itemRender.renderItemOverlayIntoGUI(fontRenderer, o, 252 + 18 * k, ry, null);
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        for (int i = 0; i < ROWS; i++) {
            int idx = scroll + i;
            if (idx >= list.size()) break;
            SmeltRecipe r = list.get(idx);
            int ry = 44 + 22 * i + 6;
            if (r.fuel.isEmpty()) fontRenderer.drawString("-", 230, ry, RustGui.LINE);
            fontRenderer.drawString(">", 243, ry, RustGui.LABEL);
            fontRenderer.drawString(formatTime(r.time) + "s", 310, ry, RustGui.TITLE);
        }
        if (list.size() > ROWS) {
            String s = (scroll + 1) + "-" + Math.min(list.size(), scroll + ROWS) + "/" + list.size();
            fontRenderer.drawString(s, 352 - fontRenderer.getStringWidth(s), 33, RustGui.LINE);
        }
        RustGui.resetColor();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        timeField.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            int x = guiLeft;
            int y = guiTop;
            if (RustGui.inside(mouseX, mouseY, x + 128, y + 138, x + 192, y + 154)) {
                save();
                RustGui.click();
                return;
            }
            List<SmeltRecipe> list = recipes();
            for (int i = 0; i < ROWS; i++) {
                int idx = scroll + i;
                if (idx >= list.size()) break;
                int ry = y + 44 + 22 * i;
                if (RustGui.inside(mouseX, mouseY, x + 202, ry, x + 352, ry + 20)) {
                    selectedId = list.get(idx).id;
                    RustGui.click();
                    return;
                }
            }
            SmeltRecipe sel = selected();
            if (RustGui.inside(mouseX, mouseY, x + 204, y + 202, x + 276, y + 218)) {
                if (sel != null) {
                    RustFurnaces.NET.sendToServer(new MsgLoadRecipe(sel.id));
                    timeField.setText(formatTime(sel.time));
                    RustGui.click();
                }
                return;
            }
            if (RustGui.inside(mouseX, mouseY, x + 280, y + 202, x + 352, y + 218)) {
                if (sel != null) {
                    RustFurnaces.NET.sendToServer(new MsgDeleteRecipe(sel.id));
                    selectedId = null;
                    RustGui.click();
                }
                return;
            }
            if (RustGui.inside(mouseX, mouseY, x + 204, y + 222, x + 276, y + 236)) {
                scroll--;
                clampScroll();
                RustGui.click();
                return;
            }
            if (RustGui.inside(mouseX, mouseY, x + 280, y + 222, x + 352, y + 236)) {
                scroll++;
                clampScroll();
                RustGui.click();
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void save() {
        float sec;
        try {
            sec = Float.parseFloat(timeField.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            sec = 5.0F;
            timeField.setText("5");
        }
        if (sec < 0.05F) sec = 0.05F;
        RustFurnaces.NET.sendToServer(new MsgSaveRecipe(Math.round(sec * 20.0F)));
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d != 0) {
            scroll += d > 0 ? -1 : 1;
            clampScroll();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (timeField.isFocused() && keyCode != Keyboard.KEY_ESCAPE) {
            boolean allowed = Character.isDigit(typedChar) || typedChar == '.' || typedChar == ','
                    || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE
                    || keyCode == Keyboard.KEY_LEFT || keyCode == Keyboard.KEY_RIGHT
                    || keyCode == Keyboard.KEY_HOME || keyCode == Keyboard.KEY_END;
            if (allowed) timeField.textboxKeyTyped(typedChar, keyCode);
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
}
