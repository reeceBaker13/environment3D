import net.java.games.input.Component;
import net.java.games.input.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MovementManager {
    private boolean wPressed = false;								// If VK_W is pressed
    private boolean sPressed = false;								// If VK_W is pressed
    private boolean aPressed = false;								// If VK_W is pressed
    private boolean dPressed = false;								// If VK_W is pressed
    private boolean spacePressed = false;
    private boolean shiftPressed = false;

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

            player.updatePosition(0.5f, xAxis, yAxis, spacePressed, shiftPressed);

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
            player.updatePosition(0.5f, wPressed, sPressed, aPressed, dPressed, spacePressed, shiftPressed);

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
            im.put(KeyStroke.getKeyStroke("L"), "pressShift");
            im.put(KeyStroke.getKeyStroke("released L"), "releaseShift");

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
        int x = (int) Math.floor(player.getPosition().x + map.getMapCentre());
        int z = (int) Math.floor(player.getPosition().z + map.getMapCentre());

        if (x < map.getMapSize() && z < map.getMapSize() && x >= 0 && z >= 0) {
            player.updateY(map.getHeightMap()[x][z] * map.getYScale());
        }
        panel.repaint();
    }

}
