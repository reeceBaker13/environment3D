import java.util.List;
import java.awt.Color;

public class Renderer {

    // Light
    private static final Vector3f lightDir = new Vector3f(0.5f, 5.0f, 0.5f);
    private static final float lLen = (float) Math.sqrt(lightDir.x*lightDir.x + lightDir.y*lightDir.y + lightDir.z*lightDir.z);
    private static final float lx = lightDir.x / lLen;
    private static final float ly = lightDir.y / lLen;
    private static final float lz = lightDir.z / lLen;
    private static final float BRIGHTNESS = 5f;

	// Culling
	private static final double MAX_RENDER_DISTANCE = 80;

    public void render(List<Triangle> tris, Player player, int[] pixels, float[] zBuffer, int width, int height, boolean[] highlight) {
        for (int i = 0; i < tris.size(); i++) {
            Triangle t = tris.get(i);
            Color useColor = highlight[i] ? Color.RED : t.color;
            this.renderTriangle(t, player, pixels, width, height, zBuffer, useColor);
        }
    }

    private void renderTriangle(
        Triangle t,
        Player player,
        int[] pixels,
		int width, int height,
        float[] zBuffer,
        Color useColor
    ) {
		// Culling
		float dx = (t.centre.x - player.getPosition().x);
		float dz = (t.centre.z - player.getPosition().z);
		float distanceSquared = dx * dx + dz * dz;

		// Skipping if too far away
		if (distanceSquared > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
			return;
		}

        // Getting triangle vertices
        Vector3f v1 = toCameraSpace(t.v1, player.getCamera(), width, height);
        Vector3f v2 = toCameraSpace(t.v2, player.getCamera(), width, height);
        Vector3f v3 = toCameraSpace(t.v3, player.getCamera(), width, height);
        
        if (v1 == null || v2 == null || v3 == null) {
            return;
        }

        // Centering triangles
        v1.x = (v1.x + width / 2f);
        v1.y = (v1.y + height / 2f);
        v2.x = (v2.x + width / 2f);
        v2.y = (v2.y + height / 2f);
        v3.x = (v3.x + width / 2f);
        v3.y = (v3.y + height / 2f);

        // Normal
        Vector3f wnorm = t.normal;

        // Dot product for BRIGHTNESS
        float angleCos = (float) Math.max(0.2, wnorm.x * lx + wnorm.y * ly + wnorm.z * lz);

        int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
        int maxX = (int) Math.min(width - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
        int maxY = (int) Math.min(height - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

        float triangleArea = ((v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                float b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                float b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                    float depth = (b1 * v1.z + b2 * v2.z + b3 * v3.z);
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
        shade *= BRIGHTNESS;

        int red = Math.min((int) (color.getRed() * shade + 34), 255);
        int green = Math.min((int) (color.getGreen() * shade + 94), 255);
        int blue = Math.min((int) (color.getBlue() * shade + 34), 255);

        return new Color(red, green, blue);
    }

    private Vector3f toCameraSpace(Vector3f v, Camera cam, int width, int height) {
        float radYaw = (float) Math.toRadians(cam.yaw);
        float radPitch = (float) -Math.toRadians(cam.pitch);
		
        float x = (v.x - cam.x);
        float y = (v.y - cam.y);
        float z = (v.z - cam.z);

        float cosY = (float) Math.cos(radYaw);
        float sinY = (float) Math.sin(radYaw);
        
        float x2 = x * cosY + z * sinY;
        float z2 = -x * sinY + z * cosY;

        float cosP = (float) Math.cos(-radPitch);
        float sinP = (float) Math.sin(-radPitch);

        float y2 = y * cosP + z2 * sinP;
        float z3 = -y * sinP + z2 * cosP;

        float near = 0.1f;
        if (-z3 < near) {
            return null;
        }

        // Perspective projection
        float f = width * 0.7f;  // you can tune this like FOV

        float sx = (x2 / -z3) * f;
        float sy = (y2 / -z3) * f;

        if (Math.abs(sx) > width * 3 || Math.abs(sy) > height * 3) {
            return null;
        }

        return new Vector3f(sx, sy, z3);

    }
}