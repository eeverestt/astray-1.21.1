package com.everest.astray.ui;

public class SpriteSheetHelper {
    public static float[] getToastUV(int id) {
        if (id < 1 || id > 16) {
            id = 16;
            ToastHelper.showErrorToast("A Fatal error occurred!", "Toast ID was not between 1 and 16 inclusive!");
        }

        int cols, rows = 4;
        int tileWidth = 160;
        int tileHeight = 32;
        int textureWidth = 640;
        int textureHeight = 128;

        int col = (id - 1) / rows;
        int row = (id - 1) % rows;

        float u0 = (float) (col * tileWidth) / textureWidth;
        float v0 = (float) (row * tileHeight) / textureHeight;
        float u1 = (float) ((col + 1) * tileWidth) / textureWidth;
        float v1 = (float) ((row + 1) * tileHeight) / textureHeight;

        return new float[]{u0, v0, u1, v1};
    }
}
