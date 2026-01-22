import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class InputManager {
    private final InputState state = new InputState();
    private Controller controller;

    // Mouse variables
    private int lastMouseX = -1;
    private int lastMouseY = -1;

    // Keyboard variables
    private boolean w, a, s, d;
    private boolean shift, space;

    // Controller variables
    private boolean controllerShift = false;
    private boolean isControllerShiftPressed = false;

    public InputManager(JPanel panel) {
        detectController();
        registerKeyboard();
        registerMouse(panel);
    }

    public void update() {
        state.moveX = 0;
        state.moveZ = 0;
        state.jumping = false;
        state.sprinting = shift || controllerShift;

        state.moveX += (this.d ? 1f : 0f) + (this.a ? -1f : 0f);
        state.moveZ += (this.w ? -1f : 0f) + (this.s ? 1f : 0f);
        state.jumping = this.space;

        if (controller != null) {
            pollController();
        }

        state.moveX = Math.max(-1f, Math.min(1f, state.moveX));
        state.moveZ = Math.max(-1f, Math.min(1f, state.moveZ));
    }

    public InputState getInputState() {
        return state;
    }

    private void detectController() {
        // Find a suitable controller
        Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
        for (Controller c : controllers) {
            if (c.getType() == Controller.Type.GAMEPAD || c.getType() == Controller.Type.STICK) {
                controller = c;
                System.out.println("Controller found: " + c.getName());
                break;
            }
        }
        if (controller == null) {
            System.out.println("No controller detected.");
        }
    }

    private void registerKeyboard() {

        // Keyboard movement
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(e -> {

                    boolean pressed = e.getID() == KeyEvent.KEY_PRESSED;
                    boolean released = e.getID() == KeyEvent.KEY_RELEASED;

                    if (!pressed && !released) return false;

                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_W -> this.w = pressed;
                        case KeyEvent.VK_A -> this.a = pressed;
                        case KeyEvent.VK_S -> this.s = pressed;
                        case KeyEvent.VK_D -> this.d = pressed;

                        case KeyEvent.VK_SHIFT -> this.shift = pressed;
                        case KeyEvent.VK_SPACE -> this.space = pressed;
                    }

                    return false;
                });
    }

    private void registerMouse(JPanel panel) {
        // Registers mouse dragging
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (lastMouseX != -1 && lastMouseY != -1) {
                    state.lookX += e.getX() - lastMouseX;
                    state.lookY += e.getY() - lastMouseY;
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

                state.lookX = 0;
                state.lookY = 0;
            }
        });
    }

    private void pollController() {
        this.controller.poll();
        net.java.games.input.Component[] components = this.controller.getComponents();

        for (net.java.games.input.Component c : components) {
            if (c.getIdentifier() == net.java.games.input.Component.Identifier.Axis.X) {
                float xAxis = c.getPollData(); // Left/Right movement
                state.moveX += Math.abs(xAxis) > 0.2 ? xAxis : 0f;

            }
            if (c.getIdentifier() == net.java.games.input.Component.Identifier.Axis.Y) {
                float zAxis = c.getPollData(); // Up/Down movement
                state.moveZ += Math.abs(zAxis) > 0.2 ? zAxis : 0f;

            }
            state.jumping |= c.getIdentifier() == Component.Identifier.Button._0 && c.getPollData() == 1.0f;
            if (c.getIdentifier() == Component.Identifier.Button._8) {
                boolean pressed = c.getPollData() == 1.0f;

                if (pressed && !isControllerShiftPressed) {
                    state.sprinting = controllerShift = !controllerShift;
                }

                isControllerShiftPressed = pressed;
            }

            if (c.getIdentifier() == Component.Identifier.Axis.RX) {
                float xTurn = c.getPollData(); // Left/Right movement
                state.lookX -= Math.abs(xTurn) > 0.2 ? xTurn * 20f : 0f;
            }
            if (c.getIdentifier() == Component.Identifier.Axis.RY) {
                float yTurn = c.getPollData(); // Up/Down movement
                state.lookY -= Math.abs(yTurn) > 0.2 ? yTurn * 20f : 0f;
            }
        }
    }


}