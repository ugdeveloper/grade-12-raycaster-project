package com.raycaster;

/**
 * Simple weapon data for the player's hitscan pistol.
 * Rendering and hit detection are handled by App and RayEngine.
 */
public class Gun {

    private final double damage;
    private final long cooldownMillis;
    private final double hitRadius;

    public Gun() {
        this(25.0, 250, 18.0);
    }

    public Gun(double damage, long cooldownMillis, double hitRadius) {
        this.damage = damage;
        this.cooldownMillis = cooldownMillis;
        this.hitRadius = hitRadius;
    }

    public double getDamage() {
        return damage;
    }

    public long getCooldownMillis() {
        return cooldownMillis;
    }

    public double getHitRadius() {
        return hitRadius;
    }
}
