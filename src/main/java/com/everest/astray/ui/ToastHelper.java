package com.everest.astray.ui;

import com.everest.astray.ui.toast.ErrorToast;
import com.everest.astray.ui.toast.MusicToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;

public class ToastHelper {

    public static void showToast(String title, String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.getToastManager() == null) return;

        client.execute(() -> {
            client.getToastManager().add(
                    new SystemToast(
                            SystemToast.Type.PERIODIC_NOTIFICATION,
                            Text.literal(title),
                            message != null ? Text.literal(message) : null
                    )
            );
        });
    }

    public static void showToast(String title) {
        showToast(title, null);
    }

    public static void showMusicToast(String title, String message) {
        MusicToast.show(title, message);
    }

    public static void showMusicToast(String title) {
        MusicToast.show(title, null);
    }

    public static void showErrorToast(String title, String message) {
        ErrorToast.show(title, message);
    }

    public static void showErrorToast(String title) {
        ErrorToast.show(title, null);
    }
}

