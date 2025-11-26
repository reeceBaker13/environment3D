import java.awt.Color;

public class Triangle {
    public Vector3f v1;
    public Vector3f v2;
    public Vector3f v3;
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
    }

    private Vector3f computerNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        float wx = (float) ((v2.getY() - v1.getY()) * (v3.getZ() - v1.getZ())) - (float) ((v2.getZ() - v1.getZ()) * (v3.getY() - v1.getY()));
        float wy = (float) ((v2.getZ() - v1.getZ()) * (v3.getX() - v1.getX())) - (float) ((v2.getX() - v1.getX()) * (v3.getZ() - v1.getZ()));
        float wz = (float) ((v2.getX() - v1.getX()) * (v3.getY() - v1.getY())) - (float) ((v2.getY() - v1.getY()) * (v3.getX() - v1.getX()));

        float len = (float) Math.sqrt(wx*wx + wy*wy + wz*wz);
        wx /= len;
        wy /= len;
        wz /= len;

        return new Vector3f(wx, wy, wz);
    }

	private Vector3f computeCentre(Vector3f v1, Vector3f v2, Vector3f v3) {
		float cx = (float) (v1.getX() + v2.getX() + v3.getX()) / 3;
		float cy = (float) (v1.getY() + v2.getY() + v3.getY()) / 3;
		float cz = (float) (v1.getZ() + v2.getZ() + v3.getZ()) / 3;

		return new Vector3f(cx, cy, cz);
	}
}