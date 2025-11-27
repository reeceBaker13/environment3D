import net.java.games.input.Component;
import net.java.games.input.Controller;

import javax.swing.*;
import java.awt.event.*;

public class MovementManager {
    private boolean wPressed = false;								// If VK_W is pressed
    private boolean sPressed = false;								// If VK_W is pressed
    private boolean aPressed = false;								// If VK_W is pressed
    private boolean dPressed = false;								// If VK_W is pressed
    private boolean spacePressed = false;
    private boolean shiftPressed = false;
    private boolean ctrlPressed = false;

    private Controller controller;

    // Camera variables
    private int lastMouseX = -1;									// Previous mouse x position (-1 is no mouse movement)
    private int lastMouseY = -1;									// Previous mouse y position (-1 is no mouse movement)

    private final Player player;
    private final Map map;
    private final JPanel panel;

    public MovementManager(int gameSpeed, Player player, Map map, JPanel panel) {
        // Game variables
        this.player = player;
        this.map = map;
        this.panel = panel;

        // Timer that updates player movement
        final Timer movementTimerController = new Timer(gameSpeed, e -> {

            this.controller.poll();
            Component[] components = this.controller.getComponents();

            float xAxis = 0f, yAxis = 0f;

            for (Component c : components) {
                if (c.getIdentifier() == Component.Identifier.Axis.X) {
                    xAxis = c.getPollData(); // Left/Right movement
                }
                if (c.getIdentifier() == Component.Identifier.Axis.Y) {
                    yAxis = c.getPollData(); // Up/Down movement
                }
                if (c.getIdentifier() == Component.Identifier.Button._0 && c.getPollData() == 1.0f) {
                    System.out.println("Jump!");
                }
            }

            player.updatePosition(0.5f, xAxis, yAxis, spacePressed, shiftPressed, ctrlPressed);

            updatePlayerElevation();
        });

        final Timer turningTimerController = new Timer(1, e -> {
            controller.poll();
            Component[] components = controller.getComponents();

            float xTurn = 0f, yTurn = 0f;

            for (Component c : components) {
                if (c.getIdentifier() == Component.Identifier.Axis.RX) {
                    xTurn = c.getPollData(); // Left/Right movement
                }
                if (c.getIdentifier() == Component.Identifier.Axis.RY) {
                    yTurn = c.getPollData(); // Up/Down movement
                }
            }

            player.turn(-xTurn * 10, -yTurn * 10);
        });

        final Timer movementTimerKeyboard = new Timer(gameSpeed, e -> {
            player.updatePosition(0.5f, wPressed, sPressed, aPressed, dPressed, spacePressed, shiftPressed, ctrlPressed);

            updatePlayerElevation();
        });

        // Controller
        InputManager inManager = new InputManager();
        this.controller = inManager.getController();

        if (controller == null) {

            // Registers mouse dragging
            panel.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if (lastMouseX != -1 && lastMouseY != -1) {
                        int dx = e.getX() - lastMouseX;
                        int dy = e.getY() - lastMouseY;

                        player.turn(dx, dy);

                        panel.repaint();
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

            // Keyboard bindings for movement
            InputMap im = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = panel.getActionMap();

            im.put(KeyStroke.getKeyStroke("W"), "pressW");
            im.put(KeyStroke.getKeyStroke("released W"), "releaseW");
            im.put(KeyStroke.getKeyStroke("S"), "pressS");
            im.put(KeyStroke.getKeyStroke("released S"), "releaseS");
            im.put(KeyStroke.getKeyStroke("A"), "pressA");
            im.put(KeyStroke.getKeyStroke("released A"), "releaseA");
            im.put(KeyStroke.getKeyStroke("D"), "pressD");
            im.put(KeyStroke.getKeyStroke("released D"), "releaseD");
            im.put(KeyStroke.getKeyStroke("SPACE"), "pressSpace");
            im.put(KeyStroke.getKeyStroke("released SPACE"), "releaseSpace");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK), "pressShift");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "releaseShift");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, InputEvent.CTRL_DOWN_MASK), "pressCtrl");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0, true), "releaseCtrl");

            am.put("pressW", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    wPressed = true;
                }
            });
            am.put("releaseW", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    wPressed = false;
                }
            });
            am.put("pressS", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    sPressed = true;
                }
            });
            am.put("releaseS", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    sPressed = false;
                }
            });
            am.put("pressA", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    aPressed = true;
                }
            });
            am.put("releaseA", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    aPressed = false;
                }
            });
            am.put("pressD", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    dPressed = true;
                }
            });
            am.put("releaseD", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    dPressed = false;
                }
            });
            am.put("pressSpace", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    spacePressed = true;
                }
            });
            am.put("releaseSpace", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    spacePressed = false;
                }
            });
            am.put("pressShift", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    shiftPressed = true;
                }
            });
            am.put("releaseShift", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    shiftPressed = false;
                }
            });
            am.put("pressCtrl", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    ctrlPressed = true;
                }
            });
            am.put("releaseCtrl", new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    ctrlPressed = false;
                }
            });

            movementTimerKeyboard.start();
            movementTimerController.stop();
            turningTimerController.stop();
        } else {
            movementTimerController.start();
            turningTimerController.start();
            movementTimerKeyboard.stop();
        }
    }

    private void updatePlayerElevation() {
        float elevation = getElevation();
        player.updateY(elevation);
        panel.repaint();
    }

    private float getElevation() {
        int x = (int) Math.floor(player.getPosition().x + map.getMapCentre());
        int z = (int) Math.floor(player.getPosition().z + map.getMapCentre());

        if (x >= 0 && z >= 0 && x < map.getMapSize() - 1 && z < map.getMapSize() - 1) {
            int idx = x * (map.getMapSize() - 1) + z;
            Triangle t = map.getMap().get(idx * 2);
            if (t != null) {
                Vector3f weights = getWeights(t);
                return weights.x * t.v1.y + weights.y * t.v2.y + weights.z * t.v3.y;
            }
        }

        return 0;

    }

    private Vector3f getWeights(Triangle t) {
        Vector2f v1 = new Vector2f(t.v2.x - t.v1.x, t.v2.z - t.v1.z);
        Vector2f v2 = new Vector2f(t.v3.x - t.v1.x, t.v3.z - t.v1.z);
        Vector2f v3 = new Vector2f(player.getPosition().x - t.v1.x, player.getPosition().z - t.v1.z);

        float d11 = v1.copy().dot(v1);
        float d22 = v2.copy().dot(v2);
        float d12 = v1.copy().dot(v2);
        float d31 = v3.copy().dot(v1);
        float d32 = v3.copy().dot(v2);

        float denom = d11 * d22 - d12 * d12;

        float v = (d22 * d31 - d12 * d32) / denom;
        float w = (d11 * d32 - d12 * d31) / denom;
        float u = 1.0f - v - w;

        return new Vector3f(u, v, w);
    }

}
