package com.everest.astray.ui.toast;

import com.everest.astray.ui.SpriteSheetHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ErrorToast extends GrowingToast {

    private static final Identifier TEXTURE = Identifier.of("astray", "textures/spritesheets/toast.png");

    private final Text title;
    private final Text description;
    private final ItemStack icon;
    private final long displayTime;
    private long startTime = -1;

    private final int width;

    public ErrorToast(Text title, Text description) {
        this(title, description, new ItemStack(Items.BARRIER), 3000L);
    }

    public ErrorToast(Text title, Text description, ItemStack icon, long displayTime) {
        this.title = title;
        this.description = description;
        this.icon = icon;
        this.displayTime = displayTime;

        var client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(title);
        if (description != null)
            textWidth = Math.max(textWidth, client.textRenderer.getWidth(description));

        this.width = Math.max(160, textWidth + 70);
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager manager, long time) {
        if (startTime < 0) startTime = time;
        float[] uv = SpriteSheetHelper.getToastUV(16);
        context.drawTexture(TEXTURE, 10, 10, (int)(uv[0] * 640), (int)(uv[1] * 128), 160, 32, 640, 128);

        float scale = 1.5F;
        context.getMatrices().push();
        context.getMatrices().translate(6, 4, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.drawItem(icon, 0, 0);
        context.getMatrices().pop();

        context.drawText(manager.getClient().textRenderer, title, 32, 7, 0xFF5555, false);
        if (description != null) context.drawText(manager.getClient().textRenderer, description, 32, 18, 0xFFAAAA, false);

        return (time - startTime) < displayTime ? Visibility.SHOW : Visibility.HIDE;
    }


    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public Object getType() {
        return ErrorToast.class;
    }

    public static void show(String title, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getToastManager() == null) return;
        client.execute(() -> client.getToastManager().add(
                new ErrorToast(Text.literal(title), Text.literal(description))
        ));
    }
}