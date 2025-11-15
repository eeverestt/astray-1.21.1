package com.everest.astray.entity;

import com.everest.astray.music.RadiusMusicHandler;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.light.data.PointLightData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class RiftEntity extends HostileEntity {

    private static final float MUSIC_RADIUS = 14f;
    private final PointLightData light = new PointLightData();
    private final Map<PlayerEntity, Double> prevVelY = new HashMap<>();
    private final Map<PlayerEntity, Boolean> prevOnGround = new HashMap<>();
    private final Map<PlayerEntity, Boolean> inRadius = new HashMap<>();

    private float timer = 0;

    public RiftEntity(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.setInvulnerable(true);

        if (world.isClient()) {
            RadiusMusicHandler.init();
        }
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return HostileEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0);
    }

    public float getTimer() { return timer; }
    public void addTimer(float time) { timer += time; }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        VeilRenderSystem.renderer().getLightRenderer().addLight(light);
        super.onSpawnPacket(packet);
    }

    @Override
    public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public void pushAwayFrom(net.minecraft.entity.Entity entity) {}
    @Override
    public boolean isPushable() { return false; }
    @Override
    protected void tickCramming() {};

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient()) return;

        int particleCount = 12;
        double halfHeight = 10.0;
        for (int i = 0; i < particleCount; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double r = MUSIC_RADIUS * Math.sqrt(Math.random());
            double x = this.getX() + r * Math.cos(angle);
            double z = this.getZ() + r * Math.sin(angle);
            double y = this.getY() - halfHeight + Math.random() * 20.0;
            double velocityX = (Math.random() - 0.5) * 0.01;
            double velocityY = (Math.random() - 0.5) * 0.01;
            double velocityZ = (Math.random() - 0.5) * 0.01;
            this.getWorld().addParticle(ParticleTypes.END_ROD, x, y, z, velocityX, velocityY, velocityZ);
        }

        light.setPosition(this.getX(), this.getY(), this.getZ())
                .setBrightness(0.01F)
                .setRadius(14)
                .setColor(200, 0, 255);

        double minY = this.getY() - halfHeight;
        double maxY = this.getY() + halfHeight;

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity local = client.player;

        for (PlayerEntity player : this.getWorld().getPlayers()) {
            double horizontalDist = Math.hypot(player.getX() - this.getX(), player.getZ() - this.getZ());
            boolean insideCylinder = horizontalDist <= MUSIC_RADIUS && player.getY() >= minY && player.getY() <= maxY;
            boolean wasInRadius = inRadius.getOrDefault(player, false);

            if (insideCylinder && !wasInRadius) {
                inRadius.put(player, true);
                onPlayerEnterRadius(player);
            } else if (!insideCylinder && wasInRadius) {
                inRadius.put(player, false);
                onPlayerLeaveRadius(player);
            }

            if (player == local && insideCylinder) {
                Vec3d vel = player.getVelocity();
                double currY = vel.y;
                double prevY = prevVelY.getOrDefault(player, 0.0);
                boolean prevGround = prevOnGround.getOrDefault(player, player.isOnGround());
                boolean currGround = player.isOnGround();

                double horizontalFactor = 0.8; // move slower horizontally
                double jumpFactor = 2.6;       // jump higher than normal
                double ascentFactor = 0.6;     // rise more slowly
                double gravityFactor = 0.35;   // fall more slowly

                if (prevGround && !currGround && currY > 0) {
                    player.setVelocity(vel.x * horizontalFactor, currY * jumpFactor * ascentFactor, vel.z * horizontalFactor);
                } else if (!currGround) {
                    double newY = currY > 0 ? currY * 0.97 : currY * gravityFactor;
                    player.setVelocity(vel.x * horizontalFactor, newY, vel.z * horizontalFactor);
                } else {
                    player.setVelocity(vel.x * horizontalFactor, vel.y, vel.z * horizontalFactor);
                }

                prevVelY.put(player, player.getVelocity().y);
                prevOnGround.put(player, currGround);
            } else {
                prevVelY.remove(player);
                prevOnGround.remove(player);
            }
        }
    }

    private void onPlayerEnterRadius(PlayerEntity player) {
        if (!(player instanceof ClientPlayerEntity)) return;

        RadiusMusicHandler.playComaTrack();
    }

    private void onPlayerLeaveRadius(PlayerEntity player) {
        if (!(player instanceof ClientPlayerEntity)) return;

        RadiusMusicHandler.restorePreviousTrack();
    }

    @Override
    public void onRemoved() {
        VeilRenderSystem.renderer().getLightRenderer().getLights(light.getType())
                .removeIf(l -> l.getLightData().equals(light));
        super.onRemoved();
    }

    @Override
    public void travel(Vec3d movementInput) {}
}