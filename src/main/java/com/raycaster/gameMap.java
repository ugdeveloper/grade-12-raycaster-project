package com.raycaster;

public class gameMap {
    
    private int mapWidth, mapHeight, cubeSize;
    private int[][] map;

    public gameMap(int[][] map) {
        this.map = map;
        this.mapWidth = map[0].length;
        this.mapHeight = map.length;
        this.cubeSize = 64;
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getCubeSize() {
        return cubeSize;
    }

    public int[][] getMap() {
        return map;
    }

    public void addWall(int x, int y) {
        if (x >= 0 && x < mapWidth && y >= 0 && y < mapHeight) {
            map[y][x] = 1;
        }
    }

    public void removeWall(int x, int y) {
        map[y][x] = 0;
    }

}
