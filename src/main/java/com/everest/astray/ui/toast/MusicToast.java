package com.everest.astray.ui.toast;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MusicToast implements Toast {

    private static final Identifier TEXTURE = Identifier.of("astray", "textures/gui/music.png");
    private static final int HEIGHT = 32;

    private final Text title;
    private final Text description;
    private final ItemStack icon;
    private final long displayTime;

    private long startTime = -1;
    private boolean initialized = false;

    private int width;

    public MusicToast(Text title, Text description) {
        this(title, description, new ItemStack(Items.MUSIC_DISC_5), 3000L);
    }

    public MusicToast(Text title, Text description, ItemStack icon, long displayTime) {
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
        if (!initialized) {
            startTime = time;
            initialized = true;
        }

        int leftWidth = 10;
        int rightWidth = 10;
        int middleWidth = width - leftWidth - rightWidth;

        context.drawTexture(TEXTURE, 0, 0, 0, 0, leftWidth, HEIGHT, 160, 32);

        context.drawTexture(TEXTURE, leftWidth, 0, leftWidth, 0, middleWidth, HEIGHT, 2, 32);

        context.drawTexture(TEXTURE, leftWidth + middleWidth, 0, 160 - rightWidth, 0, rightWidth, HEIGHT, 160, 32);

        float scale = 1.5F;
        context.getMatrices().push();
        context.getMatrices().translate(6, 4, 0);
        context.getMatrices().scale(scale, scale, 1);
        context.drawItem(icon, 0, 0);
        context.getMatrices().pop();

        context.drawText(manager.getClient().textRenderer, title, 32, 7, 0xFFFFFF, false);
        if (description != null)
            context.drawText(manager.getClient().textRenderer, description, 32, 18, 0xAAAAAA, false);

        return (time - startTime) < displayTime ? Visibility.SHOW : Visibility.HIDE;
    }


    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public Object getType() {
        return MusicToast.class;
    }

    public static void show(String title, String description) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getToastManager() == null) return;
        client.execute(() -> client.getToastManager().add(
                new MusicToast(Text.literal(title), Text.literal(description))
        ));
    }
}