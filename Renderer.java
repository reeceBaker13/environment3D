import java.util.List;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.Color;

public class Renderer {

    // Light
    private static final Vector3f lightDir = new Vector3f(0.5f, 5.0f, 0.5f);
    private static final float lLen = (float) Math.sqrt(lightDir.getX()*lightDir.getX() + lightDir.getY()*lightDir.getY() + lightDir.getZ()*lightDir.getZ());
    private static final float lx = (float) lightDir.getX() / lLen;
    private static final float ly = (float) lightDir.getY() / lLen;
    private static final float lz = (float) lightDir.getZ() / lLen;
    private static final float brightness = 6f;

	// Perspective
	private static final float minFov = 60f;
	private static final float maxFov = 90f;

	// Culling
	private static final double MAX_RENDER_DISTANCE = 80;

    public void render(List<Triangle> tris, Player player, int[] pixels, float[] zBuffer, int width, int height, boolean[] highlight) {
        for (int i = 0; i < tris.size(); i++) {
            Triangle t = tris.get(i);
            Color useColor = highlight[i] ? Color.RED : t.color;
            this.renderTriangle(t, player, getViewDirection(player.getCamera()), pixels, width, height, zBuffer, useColor);
        }
    }

    private void renderTriangle(
        Triangle t,
        Player player,
        Vector3f viewDirection,
        int[] pixels,
		int width, int height,
        float[] zBuffer,
        Color useColor
    ) {
		// Culling
		float dx = (float) (t.centre.getX() - player.getPosition().x);
		float dz = (float) (t.centre.getZ() - player.getPosition().z);
		float distanceSquared = dx * dx + dz * dz;

		// Skipping if too far away
		if (distanceSquared > MAX_RENDER_DISTANCE * MAX_RENDER_DISTANCE) {
			return;
		}

        // if (isBehindPlayer(t, player, viewDirection)) {
        //     return;
        // }

        // Getting triangle vertices
        Vector3f v1 = toCameraSpace(t.v1, player, player.getCamera(), width, height);
        Vector3f v2 = toCameraSpace(t.v2, player, player.getCamera(), width, height);
        Vector3f v3 = toCameraSpace(t.v3, player, player.getCamera(), width, height);
        
        if (v1 == null || v2 == null || v3 == null) {
            return;
        }

        // Centering triangles
        v1.setX(v1.getX() + width / 2);
        v1.setY(v1.getY() + height / 2);
        v2.setX(v2.getX() + width / 2);
        v2.setY(v2.getY() + height / 2);
        v3.setX(v3.getX() + width / 2);
        v3.setY(v3.getY() + height / 2);

        // Normal
        Vector3f wnorm = t.normal;

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

        int red = (int) Math.min((int) (color.getRed() * shade), 255);
        int green = (int) Math.min((int) (color.getGreen() * shade), 255);
        int blue = (int) Math.min((int) (color.getBlue() * shade), 255);

        return new Color(red, green, blue);
    }

    private Vector3f toCameraSpace(Vector3f v, Player player, Camera cam, int width, int height) {
        float radYaw = (float) Math.toRadians(cam.yaw);
        float radPitch = (float) -Math.toRadians(cam.pitch);
		
        float x = (float) (v.getX() - cam.x);
        float y = (float) (v.getY() - cam.y);
        float z = (float) (v.getZ() - cam.z);

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

        // if (z3 > 0) {
        //     return null;
        // } else {
        //     return new Vector3f(x2 * 15, y2 * 20, z3 * 1);
        // }

    }
    
    private Vector3f getViewDirection(Camera cam) {
        float radYaw = (float) -Math.toRadians(cam.yaw);
        float radPitch = (float) Math.toRadians(cam.pitch);

        float x = (float) Math.sin(radYaw);
        float z = (float) Math.cos(radYaw);

        return new Vector3f(x, 0, z); 
    }

    private boolean isBehindPlayer(Triangle t, Player player, Vector3f viewDirection) {
        Vector3f triangleCentre = new Vector3f((t.centre.x >= player.getPosition().x ? t.centre.x - 5 : t.centre.x + 5), 0, (t.centre.z >= player.getPosition().z ? t.centre.z - 5 : t.centre.z + 5));
        Vector3f toTriangle = triangleCentre.sub(player.getPosition());
        Vector3f normal = toTriangle.normalize();
		normal.y = 0;
        return (viewDirection.dot(normal) < 0);
    }
}