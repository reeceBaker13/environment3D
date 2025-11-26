import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.event.*;
import javax.swing.Timer;

public class Environment {

    // Terrain variables
    private List<Triangle> terrainTris;								// List of triangles representing the map
    private Map map;

    private final int terrainSize = 100;							// Side length of the map (number of squares)
    private final float yScale = 20f;								// Vertical scale [mountain intensity]

    // Camera variables
    private int lastMouseX = -1;									// Previous mouse x position (-1 is no mouse movement)
    private int lastMouseY = -1;									// Previous mouse y position (-1 is no mouse movement)

    // Player movement variables
	private float elevation = 0;									// Elevation of player

    private boolean wPressed = false;								// If VK_W is pressed
    private boolean sPressed = false;								// If VK_W is pressed
    private boolean aPressed = false;								// If VK_W is pressed
	private boolean dPressed = false;								// If VK_W is pressed
    private boolean spacePressed = false;
    private boolean shiftPressed = false;

	// General variables
	private final int gameSpeed = 32;								// Time between game refreshes

    private boolean[] highlight;

    // Constructor
    public Environment() {
        map = new Map(this.terrainSize, this.yScale);
        this.terrainTris = map.generateTriangles();
        
        this.highlight = new boolean[this.terrainTris.size()];
    }

	// Main code
    public void start() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // panel to display render results
        JPanel renderPanel = new JPanel() {

			int bufferWidth;
			int bufferHeight;

			BufferedImage framebuffer;
			int[] fbPixels;
			float[] zBuffer;
            Renderer renderer = new Renderer();
			Player player;

            
			// Timer that updates player movement
            Timer movementTimer = new Timer(gameSpeed, e -> {
				player.updatePosition(0.5f, wPressed, aPressed, sPressed, dPressed, spacePressed, shiftPressed);

                int x = (int)Math.floor(player.getPosition().x + map.getMapCentre());
                int z = (int)Math.floor(player.getPosition().z + map.getMapCentre());

                if (x < terrainSize && z < terrainSize && x >= 0 && z >= 0) {
                    player.updateY(map.getHeightMap()[x][z] * map.getYScale());
                }
				repaint();
            });

			// Controls and listeners
            {
				// Getting window focus
                setFocusable(true);
                requestFocusInWindow(true);

				// Setting background colour
				setBackground(Color.BLACK);

				// Player
				player = new Player(0, 5, 0, 20);

				// Registers mouse dragging
                addMouseMotionListener(new MouseMotionAdapter() {
                    public void mouseDragged(MouseEvent e) {
                        if (lastMouseX != -1 && lastMouseY != -1) {
                            int dx = e.getX() - lastMouseX;
                            int dy = e.getY() - lastMouseY;

							player.turn(dx, dy);

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
				im.put(KeyStroke.getKeyStroke("SPACE"), "pressSpace");
				im.put(KeyStroke.getKeyStroke("released SPACE"), "releaseSpace");
				im.put(KeyStroke.getKeyStroke("L"), "pressShift");
				im.put(KeyStroke.getKeyStroke("released L"), "releaseShift");

                am.put("pressW", new AbstractAction() { public void actionPerformed(ActionEvent e) { wPressed = true; }});
                am.put("releaseW", new AbstractAction() { public void actionPerformed(ActionEvent e) { wPressed = false; }});
                am.put("pressS", new AbstractAction() { public void actionPerformed(ActionEvent e) { sPressed = true; }});
                am.put("releaseS", new AbstractAction() { public void actionPerformed(ActionEvent e) { sPressed = false; }});
                am.put("pressA", new AbstractAction() { public void actionPerformed(ActionEvent e) { aPressed = true; }});
                am.put("releaseA", new AbstractAction() { public void actionPerformed(ActionEvent e) { aPressed = false; }});
                am.put("pressD", new AbstractAction() { public void actionPerformed(ActionEvent e) { dPressed = true; }});
                am.put("releaseD", new AbstractAction() { public void actionPerformed(ActionEvent e) { dPressed = false; }});
                am.put("pressSpace", new AbstractAction() { public void actionPerformed(ActionEvent e) { spacePressed = true; }});
                am.put("releaseSpace", new AbstractAction() { public void actionPerformed(ActionEvent e) { spacePressed = false; }});
                am.put("pressShift", new AbstractAction() { public void actionPerformed(ActionEvent e) { shiftPressed = true; }});
                am.put("releaseShift", new AbstractAction() { public void actionPerformed(ActionEvent e) { shiftPressed = false; }});

				// Starting player movement
                movementTimer.start();

            }

			// Graphics and drawing environment
            public void paintComponent(Graphics g) {
				// Initialising graphics
                Graphics2D g2 = (Graphics2D) g;
				super.paintComponent(g);

				// ENsuring buffer is same as panel size
				ensureFrameBuffer();

				// Clearing buffers
				Arrays.fill(fbPixels, 0);
				Arrays.fill(zBuffer, Float.NEGATIVE_INFINITY);

                // Resetting highlight
                Arrays.fill(highlight, false);

                int i = (int)Math.floor(player.getPosition().x + map.getMapCentre());
                int j = (int)Math.floor(player.getPosition().z + map.getMapCentre());

                if (i >= 0 && j >= 0 && i < terrainSize - 1 && j < terrainSize - 1) {
                    int idx = i * (terrainSize - 1) + j;

                    // Updating highlight booleans based on player position
                    highlight[idx * 2] = true;
                    highlight[idx * 2 + 1] = true;

					// Getting elevation
					elevation = map.getHeightMap()[i][j] * yScale;
                } else {
					elevation = 0;
				}

				// Rendering triangles and image
                renderer.render(terrainTris, player, fbPixels, zBuffer, framebuffer.getWidth(), framebuffer.getHeight(), highlight);
                g2.drawImage(framebuffer, 0, 0, getWidth(), getHeight(), null);

				// Drawing elevation & coordinates
				g2.setColor(Color.WHITE);
				g2.setFont(new Font("Arial", Font.BOLD, 14));
				g2.drawString("Yaw: " + String.format("%.2f", player.getCamera().yaw), 10, 20);
				g2.drawString("Pitch: " + String.format("%.2f", player.getCamera().pitch), 10, 40);
				g2.drawString(String.format("X: %.2f    Y: %.2f    Z: %.2f", player.getPosition().x, player.getPosition().y, player.getPosition().z), 10, 60);
				g2.drawString("Elevation: " + String.format("%.2f", elevation), 10, 80);

            }

			public void ensureFrameBuffer() {
				int w = getWidth() / 4;
				int h = getHeight() / 4;

				if (framebuffer == null || framebuffer.getWidth() != w || framebuffer.getHeight() != h) {
					framebuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
					fbPixels = ((DataBufferInt) framebuffer.getRaster().getDataBuffer()).getData();
					zBuffer = new float[w * h];
				}
			}
        };

        pane.add(renderPanel, BorderLayout.CENTER);

        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}