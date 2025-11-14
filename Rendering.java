import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

public class Rendering {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // slider to control horizontal rotation
        JSlider headingSlider = new JSlider(-180, 180, 0);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // slider to control vertical rotation
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // panel to display render results
        JPanel renderPanel = new JPanel() {
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

                    double heading = Math.toRadians(headingSlider.getValue());
                    Matrix3 headingTransform = new Matrix3(new double[] {
                            Math.cos(heading), 0, -Math.sin(heading),
                            0, 1, 0,
                            Math.sin(heading), 0, Math.cos(heading)
                        });
                    double pitch = Math.toRadians(pitchSlider.getValue());
                    Matrix3 pitchTransform = new Matrix3(new double[] {
                            1, 0, 0,
                            0, Math.cos(pitch), Math.sin(pitch),
                            0, -Math.sin(pitch), Math.cos(pitch)
                        });
                    Matrix3 transform = headingTransform.multiply(pitchTransform);

                    BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                    double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                    // initialize array with extremely far away depths
                    for (int q = 0; q < zBuffer.length; q++) {
                        zBuffer[q] = Double.NEGATIVE_INFINITY;
                    }

                    for (Triangle t : tris) {
                        Vertex v1 = transform.transform(t.getVertex1());
                        v1.setX(v1.getX() + getWidth() / 2);
                        v1.setY(v1.getY() + getHeight() / 2);
                        Vertex v2 = transform.transform(t.getVertex2());
                        v2.setX(v2.getX() + getWidth() / 2);
                        v2.setY(v2.getY() + getHeight() / 2);
                        Vertex v3 = transform.transform(t.getVertex3());
                        v3.setX(v3.getX() + getWidth() / 2);
                        v3.setY(v3.getY() + getHeight() / 2);

                        Vertex ab = new Vertex(v2.getX() - v1.getX(), v2.getY() - v1.getY(), v2.getZ() - v1.getZ());
                        Vertex ac = new Vertex(v3.getX() - v1.getX(), v3.getY() - v1.getY(), v3.getZ() - v1.getZ());
                        Vertex norm = new Vertex(
                             ab.getY() * ac.getZ() - ab.getZ() * ac.getY(),
                             ab.getZ() * ac.getX() - ab.getX() * ac.getZ(),
                             ab.getX() * ac.getY() - ab.getY() * ac.getX()
                        );
                        double normalLength = Math.sqrt(norm.getX() * norm.getX() + norm.getY() * norm.getY() + norm.getZ() * norm.getZ());
                        norm.setX(norm.getX() / normalLength);
                        norm.setY(norm.getY() / normalLength);
                        norm.setZ(norm.getZ() / normalLength);

                        double angleCos = Math.abs(norm.getZ());

                        int minX = (int) Math.max(0, Math.ceil(Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()))));
                        int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()))));
                        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()))));
                        int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()))));

                        double triangleArea = (v1.getY() - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - v1.getX());

                        for (int y = minY; y <= maxY; y++) {
                            for (int x = minX; x <= maxX; x++) {
                                double b1 = ((y - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - x)) / triangleArea;
                                double b2 = ((y - v1.getY()) * (v3.getX() - v1.getX()) + (v3.getY() - v1.getY()) * (v1.getX() - x)) / triangleArea;
                                double b3 = ((y - v2.getY()) * (v1.getX() - v2.getX()) + (v1.getY() - v2.getY()) * (v2.getX() - x)) / triangleArea;
                                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                    double depth = b1 * v1.getZ() + b2 * v2.getZ() + b3 * v3.getZ();
                                    int zIndex = y * img.getWidth() + x;
                                    if (zBuffer[zIndex] < depth) {
                                        img.setRGB(x, y, getShade(t.getColor(), angleCos).getRGB());
                                        zBuffer[zIndex] = depth;
                                    }
                                }
                            }
                        }

                    }

                    g2.drawImage(img, 0, 0, null);
                }
            };
        pane.add(renderPanel, BorderLayout.CENTER);

        headingSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    public static Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }
}