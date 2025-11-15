import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;

public class Rendering {
    // Movement variables
    private double heading = 0;
    private double pitch = 0;
    private double zoom = 1.0;

    private int lastMouseX = -1;
    private int lastMouseY = -1;

    public void start() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // panel to display render results
        JPanel renderPanel = new JPanel() {
            {
                addMouseMotionListener(new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent e) {
                        if (lastMouseX != -1 && lastMouseY != -1) {
                            int dx = e.getX() - lastMouseX;
                            int dy = e.getY() - lastMouseY;

                            // Sensitivity
                            heading += dx * 0.01;
                            pitch -= dy * 0.01;

                            repaint();
                        }

                        lastMouseX = e.getX();
                        lastMouseY = e.getY();
                    }
                });
                
                // Reset last position when clicking
                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        lastMouseX = e.getX();
                        lastMouseY = e.getY();
                    }

                    public void mouseReleased(MouseEvent e) {
                        lastMouseX = -1;
                        lastMouseY = -1;
                    }
                });

                // Mouse wheel for zoom
                addMouseWheelListener(e -> {
                    zoom += -e.getPreciseWheelRotation() * 0.1;
                    zoom = Math.max(0.1, Math.min(zoom, 5)); // clamp
                    repaint();
                });
            }

            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                List<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                        new Vertex(-100, -100, 100),
                                        new Vertex(-100, 100, -100),
                                        Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100),
                                        new Vertex(-100, -100, 100),
                                        new Vertex(100, -100, -100),
                                        Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                        new Vertex(100, -100, -100),
                                        new Vertex(100, 100, 100),
                                        Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100),
                                        new Vertex(100, -100, -100),
                                        new Vertex(-100, -100, 100),
                                        Color.BLUE));

                double headingRad = heading;
                Matrix3 headingTransform = new Matrix3(new double[] {
                    Math.cos(heading), 0, -Math.sin(heading),
                    0, 1, 0,
                    Math.sin(heading), 0, Math.cos(heading)
                    });
                double pitchRad = pitch;
                Matrix3 pitchTransform = new Matrix3(new double[] {
                    1, 0, 0,
                    0, Math.cos(pitch), Math.sin(pitch),
                    0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3 scale = new Matrix3(new double[] {
                    zoom, 0, 0,
                    0, zoom, 0,
                    0, 0, zoom
                });
                Matrix3 transform = headingTransform.multiply(pitchTransform).multiply(scale);

                Renderer renderer = new Renderer();

                BufferedImage img = renderer.render(tris, transform, getWidth(), getHeight());

                g2.drawImage(img, 0, 0, null);
            }
        };
        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Rendering().start();
    }
}