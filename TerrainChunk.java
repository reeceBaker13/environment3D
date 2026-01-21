import java.util.ArrayList;
import java.util.List;

public class TerrainChunk {
    float minX, maxX;
    float minZ, maxZ;

    List<Triangle> triangles;
    TerrainChunk[] children;

    public TerrainChunk(float minX, float maxX, float minZ, float maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.triangles = new ArrayList<>();
    }
    public TerrainChunk(float minX, float maxX, float minZ, float maxZ, List<Triangle> triangles) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
        this.triangles = triangles;
    }

    public boolean intersects(Triangle t) {
        float cx = t.centre.x;
        float cz = t.centre.z;

        return cx >= this.minX && cx <= this.maxX && cz >= this.minZ && cz <= this.maxZ;
    }

    public boolean isLeaf() {
        return this.children == null;
    }

    public boolean isVisible(Camera cam) {
        float dx = (this.minX + this.maxX) * 0.5f - cam.x;
        float dz = (this.minZ + this.maxZ) * 0.5f - cam.z;

        float distSquared = dx*dx + dz*dz;
        float radius = 0.5f * (maxX - minX);
        return distSquared < (Renderer.MAX_RENDER_DISTANCE * radius) * (Renderer.MAX_RENDER_DISTANCE * radius);
    }
}
