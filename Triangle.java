import java.awt.Color;

public class Triangle {
    public Vector3f v1;
    public Vector3f v2;
    public Vector3f v3;

    public float minX;
    public float maxX;
    public float minZ;
    public float maxZ;

    public Color color;

    public Vector3f normal;
	public Vector3f centre;

    public Triangle(Vector3f v1, Vector3f v2, Vector3f v3, Color color) {
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
        this.color = color;
        
        this.normal = computerNormal(this.v1, this.v2, this.v3);
		this.centre = computeCentre(this.v1, this.v2, this.v3);
        computeMax();
    }

    private Vector3f computerNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        float nx = ((v2.y - v1.y) * (v3.z - v1.z)) - ((v2.z - v1.z) * (v3.y - v1.y));
        float ny = ((v2.z - v1.z) * (v3.x - v1.x)) - ((v2.x - v1.x) * (v3.z - v1.z));
        float nz = ((v2.x - v1.x) * (v3.y - v1.y)) - ((v2.y - v1.y) * (v3.x - v1.x));

        float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
        nx /= len;
        ny /= len;
        nz /= len;

        return new Vector3f(nx, ny, nz);
    }

	private Vector3f computeCentre(Vector3f v1, Vector3f v2, Vector3f v3) {
		float cx = (v1.x + v2.x + v3.x) / 3;
		float cy = (v1.y + v2.y + v3.y) / 3;
		float cz = (v1.z + v2.z + v3.z) / 3;

		return new Vector3f(cx, cy, cz);
	}

    private void computeMax() {
        this.minX = Math.min(v1.x, Math.min(v2.x, v3.x));
        this.maxX = Math.max(v1.x, Math.max(v2.x, v3.x));
        this.minZ = Math.min(v1.z, Math.min(v2.z, v3.z));
        this.maxZ = Math.max(v1.z, Math.max(v2.z, v3.z));
    }
}