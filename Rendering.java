import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import javax.swing.Timer;

public class Rendering {

    // Terrain variables
    private List<Triangle> terrainTris;								// List of triangles representing the map

    private final int terrainSize = 100;							// Side length of the map (number of squares)
    private final double yScale = 20;								// Vertical scale [mountain intensity]
    private final double half = (terrainSize - 1) / 2.0;			// Length of half the scaled map
    
    private boolean[] highlight;									// Array of booleans on if the player is at that triangle (two/zero occupied at a time)

    // Camera variables
    private double heading = 3 * Math.PI / 4;						// Horizontal rotation
    private double pitch = 7 * Math.PI / 4;							// Vertical rotation
    private double zoom = 5.0;										// Zoom (bigger the closer)

    private int lastMouseX = -1;									// Previous mouse x position (-1 is no mouse movement)
    private int lastMouseY = -1;									// Previous mouse y position (-1 is no mouse movement)

    // Player movement variables
    private int playerX = 0;
    private int playerZ = 0;

    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean aPressed = false;
	private boolean dPressed = false;
    
    private int moveX = 0;
    private int moveZ = 0;

	// General variables
	private final int gameSpeed = 64;								// Time between game refreshes

    // Constructor
    public Rendering() {
        terrainTris = generateTriangles(this.terrainSize);
        highlight = new boolean[terrainTris.size()];
    }

    // Generating triangles for the map
    public List<Triangle> generateTriangles(int size) {
        // Getting map of heights
        double[][] heights = generateNoise(size);

        // Creating 2 triangles for each square on the grid
        List<Triangle> tris = new ArrayList<>();
        for (int i = 0; i < size - 1; i++) {
        	for (int j = 0; j < size - 1; j++) {
                float shade = (float) ((heights[i][j] + 1) / 2.0);
                Color c = new Color(shade, shade, shade);

                Vertex v00 = new Vertex(i - half, heights[i][j] * yScale, j - half);
                Vertex v10 = new Vertex((i + 1) - half, heights[i + 1][j] * yScale, j - half);
                Vertex v01 = new Vertex(i - half, heights[i][j + 1] * yScale, (j + 1) - half);
                Vertex v11 = new Vertex((i + 1) - half, heights[i + 1][j + 1] * yScale, (j + 1) - half);

                tris.add(new Triangle(v00, v10, v01, c));
                tris.add(new Triangle(v10, v11, v01, c));
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
            
            Timer movementTimer = new Timer(gameSpeed, e -> {
                moveX = 0;
                moveZ = 0;

                if (wPressed) moveZ += 1;
                if (sPressed) moveZ -= 1;
                if (aPressed) moveX += 1;
                if (dPressed) moveX -= 1;

                if (moveX != 0 || moveZ != 0) {
                    playerX += moveX;
                    playerZ += moveZ;
                    repaint();
                }

            });

            {
                setFocusable(true);
                requestFocusInWindow(true);

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
                    zoom += -e.getPreciseWheelRotation() * 0.75;
                    zoom = Math.max(1, Math.min(zoom, 50)); // clamp
                    repaint();
                });

                // Key bindings for movement
                InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                ActionMap am = getActionMap();

                im.put(KeyStroke.getKeyStroke("W"), "pressW");
                im.put(KeyStroke.getKeyStroke("released W"), "releaseW");
                im.put(KeyStroke.getKeyStroke("S"), "pressS");
                im.put(KeyStroke.getKeyStroke("released S"), "releaseS");
                im.put(KeyStroke.getKeyStroke("A"), "pressA");
                im.put(KeyStroke.getKeyStroke("released A"), "releaseA");
                im.put(KeyStroke.getKeyStroke("D"), "pressD");
                im.put(KeyStroke.getKeyStroke("released D"), "releaseD");

                am.put("pressW", new AbstractAction() { public void actionPerformed(ActionEvent e) { wPressed = true; }});
                am.put("releaseW", new AbstractAction() { public void actionPerformed(ActionEvent e) { wPressed = false; }});
                am.put("pressS", new AbstractAction() { public void actionPerformed(ActionEvent e) { sPressed = true; }});
                am.put("releaseS", new AbstractAction() { public void actionPerformed(ActionEvent e) { sPressed = false; }});
                am.put("pressA", new AbstractAction() { public void actionPerformed(ActionEvent e) { aPressed = true; }});
                am.put("releaseA", new AbstractAction() { public void actionPerformed(ActionEvent e) { aPressed = false; }});
                am.put("pressD", new AbstractAction() { public void actionPerformed(ActionEvent e) { dPressed = true; }});
                am.put("releaseD", new AbstractAction() { public void actionPerformed(ActionEvent e) { dPressed = false; }});

                movementTimer.start();

            }

            public void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                Matrix3 headingTransform = new Matrix3(new double[] {
                    Math.cos(heading), 0, -Math.sin(heading),
                    0, 1, 0,
                    Math.sin(heading), 0, Math.cos(heading)
                    });
                Matrix3 pitchTransform = new Matrix3(new double[] {
                    1, 0, 0,
                    0, Math.cos(pitch), Math.sin(pitch),
                    0, -Math.sin(pitch), Math.cos(pitch)
                });
                Matrix3 scaleMatrix = new Matrix3(new double[] {
                    zoom, 0, 0,
                    0, zoom, 0,
                    0, 0, zoom
                });
                Matrix3 translationMatrix = new Matrix3(new double[] {
                    1, 0, playerX,
                    0, 1, playerZ,
                    0, 0, 1
                });
                Matrix3 transform = headingTransform.multiply(pitchTransform).multiply(scaleMatrix).multiply(translationMatrix);

                // Colouring the player position triangle
                Arrays.fill(highlight, false);

                int i = (int)Math.floor(playerX + half);
                int j = (int)Math.floor(playerZ + half);

                if (i >= 0 && j >= 0 && i < terrainSize - 1 && j < terrainSize - 1) {
                    int idx = i * (terrainSize - 1) + j;
                    highlight[idx * 2] = true;
                    highlight[idx * 2 + 1] = true;
                }


                Renderer renderer = new Renderer();

                BufferedImage img = renderer.render(terrainTris, transform, getWidth(), getHeight(), highlight, playerX, playerZ);

                g2.drawImage(img, 0, 0, null);
            }
        };

        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new Rendering().start();

        // System.out.println(new Rendering().generateTriangles());
    }
}