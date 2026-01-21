import net.java.games.input.Component;
import net.java.games.input.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MovementManager {
    private boolean wPressed = false;								// If VK_W is pressed
    private boolean sPressed = false;								// If VK_W is pressed
    private boolean aPressed = false;								// If VK_W is pressed
    private boolean dPressed = false;						 		// If VK_W is pressed
    private boolean jumping = false;
    private boolean shiftPressed = false;
    private boolean controllerSprinting = false;

    private Controller controller;

    // Camera variables
    private int lastMouseX = -1;									// Previous mouse x position (-1 is no mouse movement)
    private int lastMouseY = -1;									// Previous mouse y position (-1 is no mouse movement)

    // Game objects
    private final Player player;
    private final Map map;

    private final boolean isUsingController;

    public MovementManager(Player player, Map map, JPanel panel) {
        // Game variables
        this.player = player;
        this.map = map;

        // Turning timer
        final Timer turningTimerController = new Timer(1, e -> {
            controller.poll();
            Component[] components = controller.getComponents();

            float xTurn = 0f, yTurn = 0f;

            for (Component c : components) {
                if (c.getIdentifier() == Component.Identifier.Axis.RX) {
                    xTurn = c.getPollData(); // Left/Right movement
                    xTurn = Math.abs(xTurn) > 0.1 ? xTurn : 0;
                }
                if (c.getIdentifier() == Component.Identifier.Axis.RY) {
                    yTurn = c.getPollData(); // Up/Down movement
                    yTurn = Math.abs(yTurn) > 0.1 ? yTurn : 0;
                }
            }

            player.turn(-xTurn * 10, -yTurn * 10);
        });

        // Controller
        InputManager inManager = new InputManager(panel);
        this.controller = inManager.getController();

        if (this.controller == null) {

            // Registers mouse dragging
            panel.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (lastMouseX != -1 && lastMouseY != -1) {
                        int dx = e.getX() - lastMouseX;
                        int dy = e.getY() - lastMouseY;

                        player.turn(dx, dy);
                    }

                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            });

            // Reset last position when clicking
            panel.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }

                public void mouseReleased(MouseEvent e) {
                    lastMouseX = -1;
                    lastMouseY = -1;
                }
            });

            // Keyboard movement
            KeyboardFocusManager.getCurrentKeyboardFocusManager()
                    .addKeyEventDispatcher(e -> {

                        boolean pressed = e.getID() == KeyEvent.KEY_PRESSED;
                        boolean released = e.getID() == KeyEvent.KEY_RELEASED;

                        if (!pressed && !released) return false;

                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_W -> wPressed = pressed;
                            case KeyEvent.VK_A -> aPressed = pressed;
                            case KeyEvent.VK_S -> sPressed = pressed;
                            case KeyEvent.VK_D -> dPressed = pressed;

                            case KeyEvent.VK_SHIFT -> shiftPressed = pressed;
                            case KeyEvent.VK_SPACE -> jumping = pressed;
                        }

                        return false;
                    });

            this.isUsingController = false;
        } else {
            this.isUsingController = true;
            turningTimerController.start();
        }
    }

    public void update(double deltaTime) {
        if (this.isUsingController) {
            controllerMovement(deltaTime);
        } else {
            keyboardMovement(deltaTime);
        }
    }

    private void controllerMovement(double deltaTime) {
        this.controller.poll();
        Component[] components = this.controller.getComponents();

        float xAxis = 0f, yAxis = 0f;
        boolean sprinting = false;

        for (Component c : components) {
            if (c.getIdentifier() == Component.Identifier.Axis.X) {
                xAxis = c.getPollData(); // Left/Right movement
                xAxis = Math.abs(xAxis) > 0.2 ? xAxis : 0;
            }
            if (c.getIdentifier() == Component.Identifier.Axis.Y) {
                yAxis = c.getPollData(); // Up/Down movement
                yAxis = Math.abs(yAxis) > 0.2 ? yAxis : 0;
            }
            if (c.getIdentifier() == Component.Identifier.Button._0 && c.getPollData() == 1.0f) {
                jumping = true;
            }
            if (c.getIdentifier() == Component.Identifier.Button._8 && c.getPollData() == 1.0f) {
                controllerSprinting = !controllerSprinting;
            }
        }

        player.updatePosition(deltaTime, xAxis, yAxis, controllerSprinting);

        updatePlayerElevation(deltaTime);
    }

    private void keyboardMovement(double deltaTime) {
        player.updatePosition(deltaTime, wPressed, sPressed, aPressed, dPressed, shiftPressed);

        updatePlayerElevation(deltaTime);
    }

    private void updatePlayerElevation(double deltaTime) {
        if (jumping) {
            player.jump();
            jumping = false;
        }

        float elevation = getElevation();
        player.updateY(deltaTime, elevation);
    }

    private float getElevation() {
        int x = (int) Math.floor(player.getPosition().x + map.getMapCentre());
        int z = (int) Math.floor(player.getPosition().z + map.getMapCentre());

        if (x >= 0 && z >= 0 && x < map.getMapSize() - 1 && z < map.getMapSize() - 1) {
            int idx = x * (map.getMapSize() - 1) + z;
            float localX = (player.getPosition().x + map.getMapCentre()) - x;
            float localZ = (player.getPosition().z + map.getMapCentre()) - z;

            int triOffset;
            if (localX + localZ < 1.0f) {
                triOffset = 0;
            } else {
                triOffset = 1;
            }

            Triangle t = map.getMap().get(idx * 2 + triOffset);

            if (t != null) {
                float fx = player.getPosition().x + map.getMapCentre();
                float fz = player.getPosition().z + map.getMapCentre();

                int x0 = (int) Math.floor(fx);
                int z0 = (int) Math.floor(fz);

                float tx = fx - x0;
                float tz = fz - z0;

                float h00 = map.getHeightMap()[x0][z0];
                float h10 = map.getHeightMap()[x0 + 1][z0];
                float h01 = map.getHeightMap()[x0][z0 + 1];
                float h11 = map.getHeightMap()[x0 + 1][z0 + 1];

                float h0 = h00 * (1 - tx) + h10 * tx;
                float h1 = h01 * (1 - tx) + h11 * tx;

                return (h0 * (1 - tz) + h1 * tz) * map.getYScale();
            }
        }

        return 0;

    }

}