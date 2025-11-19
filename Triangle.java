import java.awt.Color;

public class Triangle {
    private Vertex vertex1;
    private Vertex vertex2;
    private Vertex vertex3;
    private Color color;
    private Vertex normal;
	private Vertex centre;

    public Triangle(Vertex vertex1, Vertex vertex2, Vertex vertex3, Color color) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.vertex3 = vertex3;
        this.color = color;
        
        this.normal = computerNormal(this.vertex1, this.vertex2, this.vertex3);
		this.centre = computeCentre(this.vertex1, this.vertex2, this.vertex3);
    }

    public Vertex getVertex1() {
        return this.vertex1;
    }

    public Vertex getVertex2() {
        return this.vertex2;
    }

    public Vertex getVertex3() {
        return this.vertex3;
    }

    public Color getColor() {
        return this.color;
    }

    public Vertex getNormal() {
        return this.normal;
    }

	public Vertex getCentre() {
		return this.centre;
	}

    private Vertex computerNormal(Vertex v1, Vertex v2, Vertex v3) {
        float wx = (float) ((v2.getY() - v1.getY()) * (v3.getZ() - v1.getZ())) - (float) ((v2.getZ() - v1.getZ()) * (v3.getY() - v1.getY()));
        float wy = (float) ((v2.getZ() - v1.getZ()) * (v3.getX() - v1.getX())) - (float) ((v2.getX() - v1.getX()) * (v3.getZ() - v1.getZ()));
        float wz = (float) ((v2.getX() - v1.getX()) * (v3.getY() - v1.getY())) - (float) ((v2.getY() - v1.getY()) * (v3.getX() - v1.getX()));

        float len = (float) Math.sqrt(wx*wx + wy*wy + wz*wz);
        wx /= len;
        wy /= len;
        wz /= len;

        return new Vertex(wx, wy, wz);
    }

	private Vertex computeCentre(Vertex v1, Vertex v2, Vertex v3) {
		float cx = (float) (v1.getX() + v2.getX() + v3.getX()) / 3;
		float cy = (float) (v1.getY() + v2.getY() + v3.getY()) / 3;
		float cz = (float) (v1.getZ() + v2.getZ() + v3.getZ()) / 3;

		return new Vertex(cx, cy, cz);
	}
}