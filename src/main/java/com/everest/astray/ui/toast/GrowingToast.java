package com.everest.astray.ui.toast;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;

public abstract class GrowingToast implements Toast {

    protected static final int HEIGHT = 32;

    protected static void drawBackground(DrawContext context, int width, int leftWidth, int rightWidth, net.minecraft.util.Identifier texture) {
        int middleWidth = width - leftWidth - rightWidth;
        context.drawTexture(texture, 0, 0, 0, 0, leftWidth, HEIGHT, 160, 32);
        context.drawTexture(texture, leftWidth, 0, leftWidth, 0, middleWidth, HEIGHT, 2, 32);
        context.drawTexture(texture, leftWidth + middleWidth, 0, 160 - rightWidth, 0, rightWidth, HEIGHT, 160, 32);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
