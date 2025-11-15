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

    private int playerX = 0;
    private int playerZ = 0;

    private List<Triangle> terrainTris;

    public Rendering() {
        terrainTris = generateTriangles(50);
    }

    public List<Triangle> generateTriangles(int size) {
        double[][] heights = generateNoise(size);

        double scale = 50;
        double yScale = 10;
        double half = (size - 1) * scale / 2.0;

        List<Triangle> tris = new ArrayList<>();
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1; j++) {
                float shade = (float) ((heights[i][j] + 1) / 2.0);
                Color c = new Color(shade, shade, shade);

                Vertex v00 = new Vertex(i * scale - half, heights[i][j] * scale * yScale, j * scale - half);
                Vertex v10 = new Vertex((i + 1) * scale - half, heights[i + 1][j] * scale * yScale, j * scale - half);
                Vertex v01 = new Vertex(i * scale - half, heights[i][j + 1] * scale * yScale, (j + 1) * scale - half);
                Vertex v11 = new Vertex((i + 1) * scale - half, heights[i + 1][j + 1] * scale * yScale, (j + 1) * scale - half);

                tris.add(new Triangle(v00, v10, v01, c));
                tris.add(new Triangle(v11, v10, v01, c));
            }
        }

        return tris;
    }

    public double[][] generateNoise(int size) {
        Noise noise = new Noise();
        
        double[][] array = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                array[i][j] = noise.noise(i * 0.05, j * 0.05, 1);
            }
        }

        return array;
    }

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

                List<Triangle> tris = generateTriangles(10);
                // List<Triangle> tris = new ArrayList<>();
                // tris.add(new Triangle(new Vertex(100, 100, 100),
                //                         new Vertex(-100, -100, 100),
                //                         new Vertex(-100, 100, -100),
                //                         Color.WHITE));
                // tris.add(new Triangle(new Vertex(100, 100, 100),
                //                         new Vertex(-100, -100, 100),
                //                         new Vertex(100, -100, -100),
                //                         Color.RED));
                // tris.add(new Triangle(new Vertex(-100, 100, -100),
                //                         new Vertex(100, -100, -100),
                //                         new Vertex(100, 100, 100),
                //                         Color.GREEN));
                // tris.add(new Triangle(new Vertex(-100, 100, -100),
                //                         new Vertex(100, -100, -100),
                //                         new Vertex(-100, -100, 100),
                //                         Color.BLUE));

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

                BufferedImage img = renderer.render(terrainTris, transform, getWidth(), getHeight());

                g2.drawImage(img, 0, 0, null);
            }
        };

        // NEED TO ADD PLAYER, PLAYER MOVEMENT ETC
        // renderPanel.setFocusable(true); // must receive keyboard focus
        // renderPanel.requestFocusInWindow(); // request focus on start

        // renderPanel.addKeyListener(new KeyAdapter() {
        //     @Override
        //     public void keyPressed(KeyEvent e) {
        //         int code = e.getKeyCode();
        //         switch (code) {
        //             case KeyEvent.VK_W: // move forward
        //                 // adjust camera or translate terrain
        //                 break;
        //             case KeyEvent.VK_S: // move backward
        //                 break;
        //             case KeyEvent.VK_A: // move left
        //                 break;
        //             case KeyEvent.VK_D: // move right
        //                 break;
        //         }
        //         renderPanel.repaint();
        //     }
        // });

        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Rendering().start();

        // System.out.println(new Rendering().generateTriangles());
    }
}