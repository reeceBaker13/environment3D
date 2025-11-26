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

	public Vector3f sub(Vector3f other) {
		this.x -= other.x;
		this.y -= other.x;
		this.z -= other.z;
		return this;
	}

	public Vector3f mul(float c) {
		this.x *= c;
		this.y *= c;
		this.z *= c;
		return this;
	}

	public Vector3f copy() {
		return new Vector3f(x, y, z);
	}

	public Vector3f normalize() {
		float length = (float) Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
		if (length != 0) {
			this.x /= length;
			this.y /= length;
			this.z /= length;
		}
		return this;
	}

	public float dot(Vector3f other) {
		return this.x * other.x + this.y * other.y + this.z * other.z;
	}

	public Vector3f cross(Vector3f other) {
		return new Vector3f(
			this.y * other.z - this.z * other.y,
			this.z * other.x - this.x * other.z,
			this.x * other.y - this.y * other.x
		);
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public float getZ() {
		return this.z;
	}

	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setZ(float z) {
		this.z = z;
	}
}