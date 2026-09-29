package com.raycaster;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class App extends Application {

    private boolean []keys= new boolean[7];
    private boolean attacking;
    private RayEngine re;
    private gameMap map;
    private Player player;
    private List<Sprite> sprites = new ArrayList<>();
    private int attackTimer;
    private List<gameMap>maps = new ArrayList<>();
    private final Gun pistol = new Gun();
    private long lastShotTime = 0;

    public void display() {
        re.setPlayer(player);
        re.setSprites(sprites);
        re.render();
    }

    public void buttons(KeyEvent e) {
        switch (e.getCode()) {
            case W:
                if (e.getEventType() == KeyEvent.KEY_PRESSED) { keys[0] = true; }
                else { keys[0] = false; }
                break;
            case S:
                if (e.getEventType() == KeyEvent.KEY_PRESSED) { keys[1] = true; }
                else { keys[1] = false; }
                break;
            case LEFT:
                if (e.getEventType() == KeyEvent.KEY_PRESSED) { keys[4] = true; }
                else { keys[4] = false; }
                break;
            case RIGHT:
                if (e.getEventType() == KeyEvent.KEY_PRESSED) { keys[5] = true; }
                else { keys[5] = false; }
                break;
            case A:
                if (e.getEventType() == KeyEvent.KEY_PRESSED) { keys[2] = true; }
                else { keys[2] = false; }
                break;
            case D:
                if (e.getEventType() == KeyEvent.KEY_PRESSED) { keys[3] = true; }
                else { keys[3] = false; }
                break;
            case SPACE:
                if (e.getEventType() == KeyEvent.KEY_PRESSED && attackTimer == 0) {
                    shoot();
                }
                break;
            case E:
                if (e.getEventType() == KeyEvent.KEY_PRESSED) {

                    int mx = (int)((player.getPx() + player.getPdx()*5) / 64.0);
                    int my = (int)((player.getPy() + player.getPdy()*5) / 64.0);
                    if (map.getMap()[my][mx] == 4) {
                        map.removeWall(mx, my);
                        re.setMap(map);
                    }                       
                }
                break;
            default:
                break;
        }
    }

    private void shoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime < pistol.getCooldownMillis()) {
            return;
        }
        lastShotTime = now;

        // Start the muzzle-flash animation.
        attackTimer = 1;
        attacking = true;
        re.setMuzzleFlash(true);

        // Distance from the player to the first wall directly in front of them.
        double wallDistance = re.getWallDistance(player.getPa());

        double rayX = Math.cos(player.getPa());
        double rayY = Math.sin(player.getPa());

        Sprite target = null;
        double closestDistance = wallDistance;

        for (Sprite sp : sprites) {
            // Type 1 is the enemy sprite.
            if (sp.getType() != 1 || sp.getGameState() == 0) {
                continue;
            }

            double dx = sp.getX() - player.getPx();
            double dy = sp.getY() - player.getPy();

            // Forward distance along the player's aiming ray.
            double forward = dx * rayX + dy * rayY;
            if (forward <= 0) {
                continue;
            }

            // Perpendicular distance from the enemy's center to the aiming ray.
            double side = Math.abs(dx * rayY - dy * rayX);

            // Treat the enemy as having a small circular hitbox.
            double hitRadius = pistol.getHitRadius();

            if (side <= hitRadius && forward < closestDistance) {
                closestDistance = forward;
                target = sp;
            }
        }

        if (target != null) {
            target.damage(pistol.getDamage());
            System.out.println("Hit enemy for " + pistol.getDamage() + " damage!");

            if (target.getGameState() == 0) {
                System.out.println("Enemy defeated!");
            }
        }
    }

    public void start(Stage stage) throws IOException {
        maps.add(new gameMap(new int[][] {
            {1,1,1,1,1,1,1,1},
            {1,0,1,0,0,0,0,1},
            {1,0,1,0,0,0,0,1},
            {1,0,1,0,0,1,1,1},
            {1,0,0,0,0,1,0,1},
            {1,0,0,0,0,0,0,1},
            {1,0,0,0,0,1,0,1},
            {1,1,1,1,1,1,1,1}
        }));
        maps.add(new gameMap(new int[][] {
            {1,1,1,1,1,1,1,1},
            {1,0,0,0,1,1,0,1},
            {1,0,0,0,0,0,0,1},
            {1,0,1,0,0,1,0,1},
            {1,1,1,0,0,1,0,1},
            {1,0,0,0,0,1,0,1},
            {1,0,0,1,0,1,0,1},
            {1,1,1,1,1,1,1,1}
        }));
        map = maps.get(0);
        player = new Player(416, 416, 270);
        sprites.add(new Sprite(1.5*64, 5 * 64, 0, Textures.T_5, 6, 1));
        sprites.add(new Sprite(5.5*64, 1.5 * 64, 0, Textures.T_4, 1, 1));
        sprites.add(new Sprite(6.5*64, 1.5 * 64, 0, Textures.T_4, 1, 1));
        re = new RayEngine(player, map, sprites);
        Pane root = new Pane();
        root.getChildren().add(re.getCanvas());
        Scene scene = new Scene(root, re.getWidth(), re.getHeight());
        scene.setOnKeyPressed(e -> { buttons(e); });
        scene.setOnKeyReleased(e -> { buttons(e); });


        AnimationTimer timer = new AnimationTimer() {
            public void handle(long now) {

                double moveSpeed = 0.5;
                if ((keys[0] && (keys[2]||keys[3])) || (keys[1] && (keys[2]||keys[3]))) 
                    moveSpeed = moveSpeed / Math.sqrt(2);

                if (keys[0]) { // W - move forward
                    player.moveForward(moveSpeed, map);
                }
                if (keys[1]) { // S - move backward
                    player.moveBackward(moveSpeed, map);
                }
                if (keys[2]) { // A
                    player.moveLeft(moveSpeed, map);
                }
                if (keys[3]) { // D
                    player.moveRight(moveSpeed, map);
                }
                if (keys[4]) {// left
                    player.rotateLeft(0.05);
                }
                if (keys[5]) { // right
                    player.rotateRight(0.05);
                }
                if (attacking) {
                    attackTimer += 1;
                    if (attackTimer > 6) {
                        attackTimer = 0;
                        attacking = false;
                        re.setMuzzleFlash(false);
                    }
                }

                for (Sprite sp: sprites) {
                    if (sp.getType() == 1) {
                        if (Math.abs(sp.getX() - player.getPx() )< 64 && Math.abs(sp.getY() - player.getPy() )< 64) {
                            if (sp.getGameState() != 0)
                                sp.setGamestate(2);
                        }
                        sp.follow(player, 0.8, map);
                    }
                    else if (sp.getType() == 6) {
                        if (sp.getGameState() > 0) {
                            if (Math.abs(sp.getX() - player.getPx() )< 32 && Math.abs(sp.getY() - player.getPy() )< 32) {
                                re.setMap(maps.get(1));
                                map = maps.get(1);
                                player.setPx(416); player.setPy(416); player.setPa(270);
                                sprites.get(0).setGamestate(0); sprites.get(1).setGamestate(0); sprites.get(2).setGamestate(0);
                            }
                        }
                    }
                }

                display();
            }
        };
        timer.start();
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}