package com.everest.astray.mixin;

import com.everest.astray.music.DynamicMusicHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.NetworkSide;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Shadow
    @Final
    private NetworkSide side;

    @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionInfo;)V", at = @At(value = "HEAD"))
    private void endMusic(DisconnectionInfo disconnectionInfo, CallbackInfo ci) {
        if(this.side == NetworkSide.CLIENTBOUND) {
            if (DynamicMusicHandler.active != null) {
                DynamicMusicHandler.active.stop();
                DynamicMusicHandler.active = null;
            }
        }
    }
}
