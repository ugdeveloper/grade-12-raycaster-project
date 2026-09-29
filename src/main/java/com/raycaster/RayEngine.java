package com.raycaster;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;

public class RayEngine {

    private double SCREEN_WIDTH;
    private double SCREEN_HEIGHT;
    private int numRays;
    private int rayWidth;

    private static final double HALF_PI = Math.PI / 2;
    private static final double THREE_HALVES_PI = 3 * Math.PI / 2;
    private static final double degree = 0.0174533;


    private double px, py, pa;
    private GraphicsContext gc;
    private int[] pixels;
    private PixelWriter pw;
    private int mapWidth, mapHeight, cubeSize;
    private int[][] map;
    private boolean showMinimap = false;
    private Canvas canvas;
    private List<Sprite> sprites = new ArrayList<>();
    private int[] depth;
    private int[] walls, cf;
    private boolean muzzleFlash = false;

    public RayEngine(Player player, gameMap map, List<Sprite> spritelist) {
        this.SCREEN_WIDTH = 960;
        this.SCREEN_HEIGHT = 640;
        this.numRays = 120;
        this.rayWidth = (int) (SCREEN_WIDTH / numRays);
        this.canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.pixels = new int[(int) (SCREEN_WIDTH * SCREEN_HEIGHT)];
        this.pw = gc.getPixelWriter();
        this.px = player.getPx();
        this.py = player.getPy();
        this.pa = player.getPa();
        this.mapWidth = map.getMapWidth();
        this.mapHeight = map.getMapHeight();
        this.cubeSize = map.getCubeSize();
        this.map = map.getMap();
        this.sprites = spritelist;
        this.depth = new int[numRays];
        walls = Textures.T_1;
        cf = Textures.T_3;
    }

    public double getPx() { return px; }
    public double getPy() { return py; }
    public double getPa() { return pa; }
    public double getWidth()  { return SCREEN_WIDTH;  }
    public double getHeight() { return SCREEN_HEIGHT; }
    public Canvas getCanvas() { return canvas; }

    public void setPlayer(Player player) {
        this.px = player.getPx();
        this.py = player.getPy();
        this.pa = player.getPa();
    }
    
    public void setSprites(List<Sprite> spriteup) {
        for (int i = 0; i < sprites.size(); i++) {
            sprites.set(i, spriteup.get(i));
        }
    }

    public void setTextures(int[] walls, int[] ceilfloor) {
        this.walls = walls;
        this.cf = ceilfloor;
    }

    public void setMap(gameMap map) {
        this.map = map.getMap();
        mapHeight = map.getMapHeight();
        mapWidth = map.getMapWidth();
        cubeSize = map.getCubeSize();
    }

    public void toggleMinimap() {
        showMinimap = !showMinimap;
    }

    public void setMuzzleFlash(boolean muzzleFlash) {
        this.muzzleFlash = muzzleFlash;
    }

    /**
     * Cast one ray without rendering it and return the distance to the first wall.
     * The angle is in radians, matching Player.getPa().
     */
    public double getWallDistance(double rayAngle) {
        int mx, my, dof;
        double rx, ry, xo, yo;

        // Horizontal intersections
        dof = 0;
        double distH = 1000000.0;
        double aTan = -1.0 / Math.tan(rayAngle);

        if (rayAngle > Math.PI) {
            ry = (((int) py / cubeSize) * cubeSize) - 0.0001;
            rx = (py - ry) * aTan + px;
            yo = -cubeSize;
            xo = -yo * aTan;
        } else if (rayAngle < Math.PI) {
            ry = (((int) py / cubeSize) * cubeSize) + cubeSize;
            rx = (py - ry) * aTan + px;
            yo = cubeSize;
            xo = -yo * aTan;
        } else {
            rx = px;
            ry = py;
            xo = 0;
            yo = 0;
            dof = 8;
        }

        while (dof < 8) {
            mx = (int) rx / cubeSize;
            my = (int) ry / cubeSize;

            if (mx < 0 || mx >= mapWidth || my < 0 || my >= mapHeight) {
                dof = 8;
            } else if (map[my][mx] > 0) {
                distH = dist(px, py, rx, ry);
                dof = 8;
            } else {
                rx += xo;
                ry += yo;
                dof++;
            }
        }

        // Vertical intersections
        dof = 0;
        double distV = 1000000.0;
        double nTan = -Math.tan(rayAngle);

        if (rayAngle > HALF_PI && rayAngle < THREE_HALVES_PI) {
            rx = (((int) px / cubeSize) * cubeSize) - 0.0001;
            ry = (px - rx) * nTan + py;
            xo = -cubeSize;
            yo = -xo * nTan;
        } else if (rayAngle < HALF_PI || rayAngle > THREE_HALVES_PI) {
            rx = (((int) px / cubeSize) * cubeSize) + cubeSize;
            ry = (px - rx) * nTan + py;
            xo = cubeSize;
            yo = -xo * nTan;
        } else {
            rx = px;
            ry = py;
            xo = 0;
            yo = 0;
            dof = 8;
        }

        while (dof < 8) {
            mx = (int) rx / cubeSize;
            my = (int) ry / cubeSize;

            if (mx < 0 || mx >= mapWidth || my < 0 || my >= mapHeight) {
                dof = 8;
            } else if (map[my][mx] > 0) {
                distV = dist(px, py, rx, ry);
                dof = 8;
            } else {
                rx += xo;
                ry += yo;
                dof++;
            }
        }

        return Math.min(distH, distV);
    }

    public void render() {
        clearScreen();
        drawMap();
        drawSprite();
        display();
        drawCrosshair();
        if (muzzleFlash) {
            drawMuzzleFlash();
        }
    }

    private void drawSprite() {
        
        sprites.sort((a, b) ->
            Double.compare(((b.getX() - px) * (b.getX() - px) + (b.getY() - py) * (b.getY() - py)), ((a.getX() - px) * (a.getX() - px) + (a.getY() - py) * (a.getY() - py)))
        );

        for (Sprite sp: sprites) {

            if (sp.getGameState() != 0) {
                double dx = sp.getX() - px;
                double dy = sp.getY() - py;

                double spriteAngle = Math.atan2(dy, dx) - pa;

                if (spriteAngle > Math.PI)  spriteAngle -= 2 * Math.PI;
                if (spriteAngle < -Math.PI) spriteAngle += 2 * Math.PI;

                double fov = numRays * (degree / 2.0); // same FOV as wall renderer

                double dist = Math.sqrt(dx * dx + dy * dy);
                int screenX = (int)((spriteAngle + fov / 2.0)* (SCREEN_WIDTH / fov));

                int spriteSize = (int)((cubeSize/1.5 * SCREEN_HEIGHT) / dist);

                double zOffset = (sp.getZ() * SCREEN_HEIGHT) / dist;

                int screenY = (int)(SCREEN_HEIGHT / 2.0) - (int)zOffset;

                int[] texture = sp.getTexture();
                double ty = 0, tx = 0, tx_off = (cubeSize/2.0) / spriteSize, ty_off = (cubeSize/2.0) / spriteSize;
                int c, c1, c2, c3, color;
                double shade = 1.0 * (1.0 / (1.0 + dist * 0.002));

                for (double x = screenX - spriteSize / 2; x < screenX + spriteSize / 2; x++)  {
                    ty = 0;
                    int rayIndex = (int)(x * numRays / SCREEN_WIDTH);
                    if (x > 0 && x < 960 && dist < depth[rayIndex]) {
                        for(int y = screenY - spriteSize / 2; y < screenY + spriteSize / 2; y++){
                            c = (((int)ty &31) * 32 + ((int)tx&31)) * 3;
                            c1 = (int)(texture[c] * shade); c2 = (int)(texture[c + 1] * shade); c3 = (int)(texture[c +2] * shade);
                            color = argb(255, c1, c2, c3);
                            if (argb(255, texture[c], texture[c+1], texture[c+2]) != argb(255, 255, 0, 255))
                                if (y >= 0 && y < SCREEN_HEIGHT)
                                    setPixel((int)x, y, color);
                            ty += ty_off;
                        
                        }
                    }
                    tx += tx_off;
                }
            }
        }
    }

    private void drawMap() {


        int r, mx, my, dof;
        double rx, ry, ra, xo, yo, distT;


        ra = pa - (degree/2) * (numRays / 2); if (ra < 0) ra += 2*Math.PI; if (ra > 2*Math.PI) ra -= 2*Math.PI;



        for (r = 0; r < numRays; r++) {
            // --- Check Horizontal Lines ---
            dof = 0;
            double dist = 1000000, hx = px, hy = py;
            double aTan = -1/Math.tan(ra);
            if (ra > Math.PI) {          // looking up
                ry = (((int) py / cubeSize) * cubeSize) - 0.0001;
                rx = (py - ry) * aTan + px;
                yo = -cubeSize; xo = -yo * aTan;
            } else if (ra < Math.PI) {   // looking down
                ry = (((int) py / cubeSize) * cubeSize) + cubeSize;
                rx = (py - ry) * aTan + px;
                yo = cubeSize; xo = -yo * aTan;
            } else {                     // looking straight left or right
                rx = px; ry = py;
                xo = 0; yo = 0; dof = 8;
            }
            while (dof < 8) {
                mx = (int)(rx) / cubeSize;
                my = (int)(ry) / cubeSize;
                if (mx < 0 || mx >= mapWidth || my < 0 || my >= mapHeight) {
                    dof = 8;
                } else if (map[my][mx] > 0) {
                    dof = 8;
                    hx = rx; hy = ry; dist = dist(px, py, hx, hy);
                } else {
                    rx += xo; ry += yo; dof += 1;
                }
            }


            // --- Check Vertical Lines ---
            dof = 0;
            double distV = 1000000, vx = px, vy = py;
            double nTan = -Math.tan(ra);
            if (ra > HALF_PI && ra < THREE_HALVES_PI) {          // looking left
                rx = (((int) px / cubeSize) * cubeSize) - 0.0001;
                ry = (px - rx) * nTan + py;
                xo = -cubeSize; yo = -xo * nTan;
            } else if (ra < HALF_PI || ra > THREE_HALVES_PI) {   // looking right
                rx = (((int) px / cubeSize) * cubeSize) + cubeSize;
                ry = (px - rx) * nTan + py;
                xo = cubeSize; yo = -xo * nTan;
            } else {                     // looking straight up or down
                rx = px; ry = py;
                xo = 0; yo = 0; dof = 8;
            }
            while (dof < 8) {
                mx = (int)(rx) / cubeSize;
                my = (int)(ry) / cubeSize;


                if (mx < 0 || mx >= mapWidth || my < 0 || my >= mapHeight) {
                    dof = 8;
                } else if (map[my][mx] > 0) {
                    dof = 8;
                    vx = rx; vy = ry; distV = dist(px, py, vx, vy);
                } else {
                    rx += xo; ry += yo; dof += 1;
                }
            }


            int hitType;
            double shade;
            if (distV < dist) {
                rx = vx; ry = vy; distT = distV;
                hitType = map[(int)(vy) / cubeSize][(int)(vx) / cubeSize];
                shade = 0.35 * (1.0 / (1.0 + distT * 0.002));;
            } else {
                rx = hx; ry = hy; distT = dist;
                hitType = map[(int)(hx) / cubeSize][(int)(hy) / cubeSize];
                shade = 0.9 * (1.0 / (1.0 + distT * 0.002));;
            }

            depth[r] = (int)distT;

            // draw 3D walls


            double ca = pa - ra; if (ca < 0) ca += 2*Math.PI; if (ca > 2*Math.PI) ca -= 2*Math.PI;
            distT = distT * Math.cos(ca); // fix fisheye
            double lineH = (cubeSize * SCREEN_HEIGHT) / distT;
            double tyStep = 32.0/ (double)lineH;
            double tyOff = 0;
            if (lineH > SCREEN_HEIGHT) {
                tyOff = (lineH - SCREEN_HEIGHT) / 2.0;  // pixels clipped off the top
                lineH = SCREEN_HEIGHT;
            }
            double lineO = SCREEN_HEIGHT / 2.0 - lineH / 2;


            int colX = r * 8;

            double ty = tyOff * tyStep;
            double tx = (distV > dist)
                ? (rx % cubeSize) * 32.0 / cubeSize
                : (ry % cubeSize) * 32.0 / cubeSize;


            if (distV > dist && ra > Math.PI) tx = 31 - (int)tx;
            if (distV <= dist && (ra < HALF_PI || ra > THREE_HALVES_PI)) tx = 31 - (int)tx;

            int[] texture = walls;


            for (int y = (int) lineO; y < (int)(lineH + lineO); y += 1) {
                int c = (((int)ty&31) * 32 + ((int)tx&31)) * 3;

                int c1 = (int)(texture[c] * shade), c2 = (int)(texture[c + 1] * shade), c3 = (int)(texture[c +2] * shade);

                int color = argb(255, c1, c2, c3);

                for (int x = colX; x < colX + rayWidth; x++) {
                    for (int iy = 0; iy < rayWidth; iy++) {
                        setPixel(x, y + iy, color);
                    }
                    
                }
                ty += tyStep;
            }

            // draw floors
            
            for (int y = (int)lineO + (int)lineH; y < SCREEN_HEIGHT; y++) {
                double rowDist = (cubeSize * SCREEN_HEIGHT) / (2.0 * (y - SCREEN_HEIGHT / 2.0));
                double floorWorldX = px + rowDist * Math.cos(ra);
                double floorWorldY = py + rowDist * Math.sin(ra);
                double floorTexX = floorWorldX % cubeSize;
                double floorTexY = floorWorldY % cubeSize;
                int c = (((int)floorTexY & 31) * 32 + ((int)floorTexX & 31)) * 3;
                shade = .7 * (1.0 / (1.0 + rowDist * 0.01));
                texture = cf;
                int c1 = (int)(texture[c] * shade), c2 = (int)(texture[c + 1] * shade), c3 = (int)(texture[c +2] * shade);

                int color = argb(255, c1, c2, c3);

                for (int x = colX; x < colX + rayWidth; x++) {
                    for (int iy = 0; iy < rayWidth; iy++) {
                        setPixel(x, y + iy, color);
                    }
                    
                }

                // draw ceiling

                for (int x = colX; x < colX + rayWidth; x++) {
                    for (int iy = 0; iy < rayWidth; iy++) {
                        setPixel(x, (int)(SCREEN_HEIGHT - y-1) + iy, color);
                    }
                }
            }

            ra += degree / 2; if (ra < 0) ra += 2*Math.PI; if (ra > 2*Math.PI) ra -= 2*Math.PI;
        }

            // draw 2D ray on minimap
            if (showMinimap) {
            drawMinimapOverlay();
            }
    }

    private void drawMinimapOverlay() {

        int tileSize = 8;
        int offsetX = 10, offsetY = 10; // top-left corner padding

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < mapWidth; x++) {
                int color = (map[y][x] == 1) ? argb(255,255,255,255) : argb(255,0,0,0);
                for (int ty = 0; ty < tileSize; ty++)
                    for (int tx = 0; tx < tileSize; tx++)
                        setPixel(offsetX + x*tileSize + tx, offsetY + y*tileSize + ty, color);
            }
        }

        // draw player dot
        int dotX = offsetX + (int)(px / cubeSize * tileSize) + tileSize/2;
        int dotY = offsetY + (int)(py / cubeSize * tileSize) + tileSize/2;
        for (int dy = -1; dy <= 1; dy++)
            for (int dx = -1; dx <= 1; dx++)
                setPixel(dotX + dx, dotY + dy, argb(255,255,255,0));
    }

    private void drawCrosshair() {
        double cx = SCREEN_WIDTH / 2.0;
        double cy = SCREEN_HEIGHT / 2.0;

        gc.setLineWidth(2.0);
        gc.setStroke(javafx.scene.paint.Color.WHITE);
        gc.strokeLine(cx - 6, cy, cx - 2, cy);
        gc.strokeLine(cx + 2, cy, cx + 6, cy);
        gc.strokeLine(cx, cy - 6, cx, cy - 2);
        gc.strokeLine(cx, cy + 2, cx, cy + 6);
    }

    private void drawMuzzleFlash() {
        double cx = SCREEN_WIDTH / 2.0;
        double cy = SCREEN_HEIGHT;

        gc.setStroke(javafx.scene.paint.Color.YELLOW);
        gc.setLineWidth(4.0);
        gc.strokeLine(cx - 22, cy - 8, cx - 8, cy - 70);
        gc.strokeLine(cx + 22, cy - 8, cx + 8, cy - 70);
        gc.strokeLine(cx, cy - 2, cx, cy - 82);
    }

    private void display() {
        gc.clearRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        pw.setPixels(0, 0, (int) SCREEN_WIDTH, (int) SCREEN_HEIGHT, PixelFormat.getIntArgbInstance(), pixels, 0, (int) SCREEN_WIDTH);
    }

    // helper: distance between two points
    private  double dist(double ax, double ay, double bx, double by) {
        return (Math.sqrt((bx-ax)*(bx-ax)+(by-ay)*(by-ay)));
    }

    // Helper: pack ARGB into a single int correctly
    private int argb(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    // Helper: set a single pixel (bounds-checked)
    private void setPixel(int x, int y, int color) {
        if (x >= 0 && x < SCREEN_WIDTH && y >= 0 && y < SCREEN_HEIGHT) {
            pixels[y * ((int) SCREEN_WIDTH) + x] = color;
        }
    }
    
    // Helper: clear pixel array
    private void clearScreen() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }
    }

}
