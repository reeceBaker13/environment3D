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

    public Vector3f add(float x, float y, float z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

	public Vector3f mul(float c) {
		this.x *= c;
		this.y *= c;
		this.z *= c;
		return this;
	}

    public Vector3f normalize() {
        float length = (float) Math.sqrt(lengthSquared());
        if (length != 0) {
            this.x /= length;
            this.y /= length;
            this.z /= length;
        }
        return this;

    }

    public float lengthSquared() {
        return x * x + y * y + z * z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void reset() {
        this.set(0, 0, 0);
    }

}