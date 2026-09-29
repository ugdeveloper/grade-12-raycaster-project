package com.raycaster;

public class Sprite {
    private double x, y, z;
    private int gameState;
    private int[] texture;
    private int type;
    private double health;


    public Sprite(double x, double y, double z, int[] texture, int type, int gameState) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.texture = texture;
        this.type = type;
        this.gameState = gameState;
        if (type == 1) {
            health = 100;
        }
    }

    public int getType() {
        return this.type;
    }

    public int getGameState() {
        return this.gameState;
    }

    public void setGamestate(int state) {
        this.gameState = state;
    }

    public void follow(Player p, double speed, gameMap map) {
        if (gameState == 2) {
            int[][] grid = map.getMap();
            int cs = map.getCubeSize();

            double dx = p.getPx() - x;
            double dy = p.getPy() - y;

            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < 0.001) return;

            dx /= dist;
            dy /= dist;

            double nextX = x + dx * speed;
            double nextY = y + dy * speed;

            if (grid[(int)(y / cs)][(int)(nextX / cs)] == 0)
                x = nextX;

            if (grid[(int)(nextY / cs)][(int)(x / cs)] == 0)
                y = nextY;
        }
    }

    public void damage(double damage) {
        this.health = this.health - damage;
        if (health <= 0) {
            this.gameState = 0;
        }
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public int[] getTexture() {
        return texture;
    }
}