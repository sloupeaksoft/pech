package com.rustfurnaces.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;

/** Рисование интерфейса в стиле Rust. */
public final class RustGui {
    public static final int BG = 0xD81E1D1B;
    public static final int PANEL = 0x70504A44;
    public static final int SLOT = 0xA0383532;
    public static final int LINE = 0xFF8C857D;
    public static final int LABEL = 0xFFB4ACA2;
    public static final int TITLE = 0xFFE8E2DA;
    public static final int GREEN = 0xFF5E7B2D;
    public static final int GREEN_HOVER = 0xFF6E8F35;
    public static final int GREEN_TEXT = 0xFFD2EBA5;
    public static final int RED = 0xFF8A3B2C;
    public static final int RED_HOVER = 0xFFA04634;
    public static final int RED_TEXT = 0xFFF5CDBF;
    public static final int GRAY_BTN = 0xFF4A4540;
    public static final int GRAY_HOVER = 0xFF5A544E;
    public static final int ORANGE = 0xFFE58A2B;
    public static final int SELECTED = 0xB0566B33;

    private RustGui() {
    }

    public static void rect(int x1, int y1, int x2, int y2, int color) {
        Gui.drawRect(x1, y1, x2, y2, color);
    }

    public static void border(int x1, int y1, int x2, int y2, int color) {
        rect(x1, y1, x2, y1 + 1, color);
        rect(x1, y2 - 1, x2, y2, color);
        rect(x1, y1, x1 + 1, y2, color);
        rect(x2 - 1, y1, x2, y2, color);
    }

    /** Большая ячейка 22x22 вокруг слота (x, y = позиция слота). */
    public static void bigSlot(int x, int y) {
        rect(x - 3, y - 3, x + 19, y + 19, SLOT);
        border(x - 3, y - 3, x + 19, y + 19, 0x30FFFFFF);
    }

    /** Ячейка 18x18 для инвентаря игрока. */
    public static void smallSlot(int x, int y) {
        rect(x - 1, y - 1, x + 17, y + 17, SLOT);
    }

    /** Стрелка вниз: линия от y1, острие заканчивается на y2. */
    public static void arrowDown(int cx, int y1, int y2, int color) {
        rect(cx - 1, y1, cx + 1, y2 - 4, color);
        for (int i = 0; i < 4; i++) {
            rect(cx - 4 + i, y2 - 4 + i, cx + 4 - i, y2 - 3 + i, color);
        }
    }

    public static void button(FontRenderer fr, int x1, int y1, int x2, int y2, String label, int bg, int textColor) {
        rect(x1, y1, x2, y2, bg);
        fr.drawString(label, (x1 + x2 - fr.getStringWidth(label)) / 2, (y1 + y2 - 8) / 2 + 1, textColor);
    }

    public static void title(FontRenderer fr, String text, int x, int y, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(2.0F, 2.0F, 1.0F);
        fr.drawString(text, 0, 0, color);
        GlStateManager.popMatrix();
    }

    public static boolean inside(int mx, int my, int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx < x2 && my >= y1 && my < y2;
    }

    public static void resetColor() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void click() {
        Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}
