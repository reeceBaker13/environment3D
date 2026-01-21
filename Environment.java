import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.event.*;

public class Environment {

    // Terrain variables
    private final Map map;

    private final int terrainSize = 250;							// Side length of the map (number of squares)
    private final float yScale = 20f;								// Vertical scale [mountain intensity]

    // Player movement variables
    private final Player player;
    private final MovementManager movementManager;
	private float elevation = 0;									// Elevation of player

	// General variables
    private final boolean[] highlight;

    // Swing objects
    private JPanel renderPanel;

    // Constructor
    public Environment() {
        map = new Map(this.terrainSize, this.yScale);
        
        this.highlight = new boolean[this.map.getMap().size()];

        // Player
        player = new Player(0, 5, 0);

        // Frame
        start();

        // Movement manager
        this.movementManager = new MovementManager(player, map, this.renderPanel);
    }

	// Main code
    public void start() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        frame.setVisible(true);

        // panel to display render results
        this.renderPanel = new JPanel() {

			BufferedImage framebuffer;
			int[] fbPixels;
			float[] zBuffer;
            final TerrainRenderer terrainRenderer = new TerrainRenderer();
            final Renderer renderer = new Renderer();

            final Font arialFont = new Font("Arial", Font.BOLD, 14);

			// Controls and listeners
            {
				// Getting window focus
                setOpaque(true);
                setFocusable(true);
                setVisible(true);
                requestFocusInWindow(true);
                setBackground(new Color(189, 238, 252));
            }

			// Graphics and drawing environment
            public void paintComponent(Graphics g) {
				// Initialising graphics
                Graphics2D g2 = (Graphics2D) g;
				super.paintComponent(g);

				// Ensuring buffer is same as panel size
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
                terrainRenderer.renderChunk(map.getRoot(), renderer, player, fbPixels, zBuffer, framebuffer.getWidth(), framebuffer.getHeight(), highlight);
                g2.drawImage(framebuffer, 0, 0, getWidth(), getHeight(), null);

				// Drawing elevation & coordinates
				g2.setColor(Color.WHITE);
				g2.setFont(arialFont);
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

        frame.setSize(1920, 1080);
        frame.setVisible(true);
    }

    public void update(double deltaTime) {
        movementManager.update(deltaTime);
        renderPanel.repaint();
    }

    public static void main(String[] args) {
        Environment environment = new Environment();

        final long[] lastTime = { System.nanoTime() };

        new Timer(16, e -> {
            long now = System.nanoTime();
            double deltaTime = (now - lastTime[0]) / 1_000_000_000.0;
            lastTime[0] = now;

            environment.update(deltaTime);
        }).start();
    }
}