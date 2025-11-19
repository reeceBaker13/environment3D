import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Color;

public class Renderer {

    // Light
    private static final Vertex lightDir = new Vertex(0.5, 5.0, 0.5);
    private static final float lLen = (float) Math.sqrt(lightDir.getX()*lightDir.getX() + lightDir.getY()*lightDir.getY() + lightDir.getZ()*lightDir.getZ());
    private static final float lx = (float) lightDir.getX() / lLen;
    private static final float ly = (float) lightDir.getY() / lLen;
    private static final float lz = (float) lightDir.getZ() / lLen;
    private static final float brightness = 10f;

	// Culling
	private static final double MAX_RENDER_DISTANCE = 80;

    public void render(List<Triangle> tris, Matrix3 transform, int[] pixels, float[] zBuffer, int width, int height, boolean[] highlight, int playerX, int playerZ) {
        for (int i = 0; i < tris.size(); i++) {
            Triangle t = tris.get(i);
            Color useColor = highlight[i] ? Color.RED : t.getColor();
            this.renderTriangle(t, transform, pixels, width, height, zBuffer, useColor, playerX, playerZ);
        }
    }

    private void renderTriangle(
        Triangle t, 
        Matrix3 transform, 
        int[] pixels,
		int width, int height,
        float[] zBuffer,
        Color useColor,
        int playerX, int playerZ
    ) {
		// Culling
		float dx = (float) (t.getCentre().getX() - playerX);
		float dz = (float) (t.getCentre().getZ() - playerZ);
		float distanceSquared = dx * dx + dz * dz;

		// Skipping if too far away
		if (distanceSquared > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
			return;
		}

        // Getting triangle vertices
        Vertex v1 = transform.transform(t.getVertex1(), playerX, playerZ);
        Vertex v2 = transform.transform(t.getVertex2(), playerX, playerZ);
        Vertex v3 = transform.transform(t.getVertex3(), playerX, playerZ);

        // Centering triangles
        v1.setX(v1.getX() + width / 2);
        v1.setY(v1.getY() + height / 2);
        v2.setX(v2.getX() + width / 2);
        v2.setY(v2.getY() + height / 2);
        v3.setX(v3.getX() + width / 2);
        v3.setY(v3.getY() + height / 2);

        // Normal
        Vertex wnorm = t.getNormal();

        // Dot product for brightness
        float angleCos = (float) Math.max(0.2, wnorm.getX() * lx + wnorm.getY() * ly + wnorm.getZ() * lz);

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
                        pixels[zIndex] = getShade(useColor, angleCos).getRGB();
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

        int red = (int) Math.min(Math.max(0, (int) (350 - color.getRed() * shade)), 255);
        int green = (int) Math.min(Math.max(0, (int) (350 - color.getGreen() * shade)), 255);
        int blue = (int) Math.min(Math.max(0, (int) (350 - color.getBlue() * shade)), 255);

        return new Color(red, green, blue);
    }
}