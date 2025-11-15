import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class Renderer {
    public BufferedImage render(List<Triangle> tris, Matrix3 transform, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        double[] zBuffer = new double[img.getWidth() * img.getHeight()];

        for (int i = 0; i < zBuffer.length; i++) {
            zBuffer[i] = Double.NEGATIVE_INFINITY;
        }

        for (Triangle t : tris) {
            this.renderTriangle(t, transform, img, zBuffer);
        }

        return img;
    }

    private void renderTriangle(
        Triangle t, 
        Matrix3 transform, 
        BufferedImage img, 
        double[] zBuffer
    ) {
        // Getting img width and height
        int width = img.getWidth(); // could be irrelevant and need to be different
        int height = img.getHeight();

        // Getting triangle vertices
        Vertex v1 = transform.transform(t.getVertex1());
        Vertex v2 = transform.transform(t.getVertex2());
        Vertex v3 = transform.transform(t.getVertex3());

        // Centering triangles
        v1.setX(v1.getX() + width / 2);
        v1.setY(v1.getY() + height / 2);
        v2.setX(v2.getX() + width / 2);
        v2.setY(v2.getY() + height / 2);
        v3.setX(v3.getX() + width / 2);
        v3.setY(v3.getY() + height / 2);

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
        int maxX = (int) Math.min(width - 1, Math.floor(Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()))));
        int maxY = (int) Math.min(height - 1, Math.floor(Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()))));

        double triangleArea = (v1.getY() - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - v1.getX());

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double b1 = ((y - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - x)) / triangleArea;
                double b2 = ((y - v1.getY()) * (v3.getX() - v1.getX()) + (v3.getY() - v1.getY()) * (v1.getX() - x)) / triangleArea;
                double b3 = ((y - v2.getY()) * (v1.getX() - v2.getX()) + (v1.getY() - v2.getY()) * (v2.getX() - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                    double depth = b1 * v1.getZ() + b2 * v2.getZ() + b3 * v3.getZ();
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < depth) {
                        img.setRGB(x, y, getShade(t.getColor(), angleCos).getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }
    }

    private static Color getShade(Color color, double shade) {
        double redLinear = Math.pow(color.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1/2.4);
        int green = (int) Math.pow(greenLinear, 1/2.4);
        int blue = (int) Math.pow(blueLinear, 1/2.4);

        return new Color(red, green, blue);
    }
}