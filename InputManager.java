import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

import javax.swing.*;

public class InputManager {
    private Controller controller;
    private JPanel panel;

    public InputManager(JPanel panel) {
        detectController();

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

    public Controller getController() {
        return controller;
    }

    public boolean controllerDetected() { return controller != null; }

}