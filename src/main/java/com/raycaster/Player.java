package com.raycaster;

public class Player {
    
    private double px, py, pdx, pdy, pa;

    public Player(double px, double py, double pa) {
        this.px = px;
        this.py = py;
        this.pa = Math.toRadians(pa);
        this.pdx = Math.cos(this.pa) * 5;
        this.pdy = Math.sin(this.pa) * 5;
    }

    public Sprite attack(Sprite sp, gameMap map) {
        int[][] grid = map.getMap();
        int cs = map.getCubeSize();

        if (grid[(int)(sp.getY() / cs)][(int)((sp.getX() + pdx) / cs)] == 0)
            sp.setX(sp.getX() + pdx);

        if (grid[(int)((sp.getY() + pdy) / cs)][(int)(sp.getX() / cs)] == 0)
            sp.setY(sp.getY() + pdy);

        return sp;
    }

    public Sprite dealDamage(Sprite sp) {
        sp.damage(25);
        return sp;
    }

    public double getPx() {
        return px;
    }

    public void setPx(double px) {
        this.px = px;
    }

    public double getPy() {
        return py;
    }

    public void setPy(double py) {
        this.py = py;
    }

    public double getPdx() {
        return pdx;
    }

    public void setPdx(double pdx) {
        this.pdx = pdx;
    }

    public double getPdy() {
        return pdy;
    }

    public void setPdy(double pdy) {
        this.pdy = pdy;
    }

    public double getPa() {
        return pa;
    }

    public void setPa(double pa) {
        this.pa = Math.toRadians(pa);
        pdx = Math.cos(this.pa) * 5;
        pdy = Math.sin(this.pa) * 5;
    }

    public void moveForward(double speed, gameMap map) {
        int[][] grid = map.getMap();
        int cs = map.getCubeSize();
        if (grid[(int)(py / cs)][(int)((px + pdx * 5) / cs)] == 0) px += pdx * speed;
        if (grid[(int)((py + pdy * 5) / cs)][(int)(px / cs)] == 0) py += pdy * speed;
    }

    public void moveBackward(double speed, gameMap map) {
        int[][] grid = map.getMap();
        int cs = map.getCubeSize();
        if (grid[(int)(py / cs)][(int)((px - pdx * 5) / cs)] == 0) px -= pdx * speed;
        if (grid[(int)((py - pdy * 5) / cs)][(int)(px / cs)] == 0) py -= pdy * speed;
    }

    public void moveLeft(double speed, gameMap map) {
        int[][] grid = map.getMap();
        int cs = map.getCubeSize();
        if (grid[(int)(py / cs)][(int)((px + pdy * 5) / cs)] == 0) px += pdy * speed;
        if (grid[(int)((py - pdx* 5) / cs)][(int)(px / cs)] == 0) py -= pdx * speed;
    }

    public void moveRight(double speed, gameMap map) {
        int[][] grid = map.getMap();
        int cs = map.getCubeSize();
        if (grid[(int)(py / cs)][(int)((px - pdy * 5) / cs)] == 0) px -= pdy * speed;
        if (grid[(int)((py + pdx * 5) / cs)][(int)(px / cs)] == 0) py += pdx * speed;
    }

    public void rotateLeft(double speed) {
        pa -= speed;
        if (pa < 0) pa += 2 * Math.PI;
        pdx = Math.cos(pa) * 5;
        pdy = Math.sin(pa) * 5;
    }

    public void rotateRight(double speed) {
        pa += speed;
        if (pa > 2 * Math.PI) pa -= 2 * Math.PI;
        pdx = Math.cos(pa) * 5;
        pdy = Math.sin(pa) * 5;
    }
}
