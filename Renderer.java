import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Color;

public class Renderer {
    private static final float brightness = 10f;

    public BufferedImage render(List<Triangle> tris, Matrix3 transform, int width, int height, boolean[] highlight, int playerX, int playerZ) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        float[] zBuffer = new float[img.getWidth() * img.getHeight()];

        for (int i = 0; i < zBuffer.length; i++) {
            zBuffer[i] = Float.NEGATIVE_INFINITY;
        }

        for (int i = 0; i < tris.size(); i++) {
            Triangle t = tris.get(i);
            Color useColor = highlight[i] ? Color.RED : t.getColor();
            this.renderTriangle(t, transform, img, zBuffer, useColor, playerX, playerZ);
        }

        return img;
    }

    private void renderTriangle(
        Triangle t, 
        Matrix3 transform, 
        BufferedImage img, 
        float[] zBuffer,
        Color useColor,
        int playerX,
        int playerZ
    ) {
        // Getting img width and height
        int width = img.getWidth(); // could be irrelevant and need to be different
        int height = img.getHeight();

        // Moving whole plane based on player position
        Vertex tv1 = new Vertex(
            t.getVertex1().getX() - playerX,
            t.getVertex1().getY(),
            t.getVertex1().getZ() - playerZ
        );
        Vertex tv2 = new Vertex(
            t.getVertex2().getX() - playerX,
            t.getVertex2().getY(),
            t.getVertex2().getZ() - playerZ
        );
        Vertex tv3 = new Vertex(
            t.getVertex3().getX() - playerX,
            t.getVertex3().getY(),
            t.getVertex3().getZ() - playerZ
        );

        // Getting triangle vertices
        Vertex v1 = transform.transform(tv1);
        Vertex v2 = transform.transform(tv2);
        Vertex v3 = transform.transform(tv3);

        // Centering triangles
        v1.setX(v1.getX() + width / 2);
        v1.setY(v1.getY() + height / 2);
        v2.setX(v2.getX() + width / 2);
        v2.setY(v2.getY() + height / 2);
        v3.setX(v3.getX() + width / 2);
        v3.setY(v3.getY() + height / 2);

        // Compute normal from original, untransformed triangle
        Vertex w1 = t.getVertex1();
        Vertex w2 = t.getVertex2();
        Vertex w3 = t.getVertex3();

        Vertex wab = new Vertex(w2.getX() - w1.getX(), w2.getY() - w1.getY(), w2.getZ() - w1.getZ());
        Vertex wac = new Vertex(w3.getX() - w1.getX(), w3.getY() - w1.getY(), w3.getZ() - w1.getZ());

        // Normal
        Vertex wnorm = new Vertex(
            wab.getY() * wac.getZ() - wab.getZ() * wac.getY(),
            wab.getZ() * wac.getX() - wab.getX() * wac.getZ(),
            wab.getX() * wac.getY() - wab.getY() * wac.getX()
        );

        // Normalize
        float len = (float) Math.sqrt(wnorm.getX()*wnorm.getX() + wnorm.getY()*wnorm.getY() + wnorm.getZ()*wnorm.getZ());
        wnorm.setX(wnorm.getX() / len);
        wnorm.setY(wnorm.getY() / len);
        wnorm.setZ(wnorm.getZ() / len);

        // Directional light (sun)
        Vertex lightDir = new Vertex(0.3, 1.0, 0.2); // slightly angled sunlight
        float lLen = (float) Math.sqrt(lightDir.getX()*lightDir.getX() + lightDir.getY()*lightDir.getY() + lightDir.getZ()*lightDir.getZ());
        lightDir.setX(lightDir.getX() / lLen);
        lightDir.setY(lightDir.getY() / lLen);
        lightDir.setZ(lightDir.getZ() / lLen);

        // Dot product for brightness
        float angleCos = (float) Math.max(0.2, wnorm.getX()*lightDir.getX() + wnorm.getY()*lightDir.getY() + wnorm.getZ()*lightDir.getZ());

        int minX = (int) Math.max(0, Math.ceil(Math.min(v1.getX(), Math.min(v2.getX(), v3.getX()))));
        int maxX = (int) Math.min(width - 1, Math.floor(Math.max(v1.getX(), Math.max(v2.getX(), v3.getX()))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.getY(), Math.min(v2.getY(), v3.getY()))));
        int maxY = (int) Math.min(height - 1, Math.floor(Math.max(v1.getY(), Math.max(v2.getY(), v3.getY()))));

        float triangleArea = (float) ((v1.getY() - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - v1.getX()));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float b1 = (float) ((y - v3.getY()) * (v2.getX() - v3.getX()) + (v2.getY() - v3.getY()) * (v3.getX() - x)) / triangleArea;
                float b2 = (float) ((y - v1.getY()) * (v3.getX() - v1.getX()) + (v3.getY() - v1.getY()) * (v1.getX() - x)) / triangleArea;
                float b3 = (float) ((y - v2.getY()) * (v1.getX() - v2.getX()) + (v1.getY() - v2.getY()) * (v2.getX() - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                    float depth = (float) (b1 * v1.getZ() + b2 * v2.getZ() + b3 * v3.getZ());
                    int zIndex = y * width + x;
                    if (zBuffer[zIndex] < depth) {
                        img.setRGB(x, y, getShade(useColor, angleCos).getRGB());
                        zBuffer[zIndex] = depth;
                    }
                }
            }
        }
    }

    private static Color getShade(Color color, float shade) {
        shade *= brightness;

        float redLinear = (float) Math.pow(color.getRed(), 2.4) * shade;
        float greenLinear = (float) Math.pow(color.getGreen(), 2.4) * shade;
        float blueLinear = (float) Math.pow(color.getBlue(), 2.4) * shade;

        int red = (int) Math.max(255 - Math.pow(redLinear, 1/2.4), 0);
        int green = (int) Math.max(255 - Math.pow(greenLinear, 1/2.4), 0);
        int blue = (int) Math.max(255 - Math.pow(blueLinear, 1/2.4), 0);

        return new Color(red, green, blue);
    }
}