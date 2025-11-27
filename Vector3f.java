public class Vector3f {
	public float x, y, z;

	public Vector3f() {
		this(0, 0, 0);
	}

	public Vector3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3f add(Vector3f other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}

	public Vector3f mul(float c) {
		this.x *= c;
		this.y *= c;
		this.z *= c;
		return this;
	}

}