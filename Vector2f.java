public class Vector2f {
	public float x, y;

	public Vector2f() {
		this(0, 0);
	}

	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2f add(Vector2f other) {
		this.x += other.x;
		this.y += other.y;
		return this;
	}

	public Vector2f sub(Vector2f other) {
		this.x -= other.x;
		this.y -= other.y;
		return this;
	}

	public Vector2f mul(float c) {
		this.x *= c;
		this.y *= c;
		return this;
	}

	public Vector2f copy() {
		return new Vector2f(x, y);
	}

	public Vector2f normalize() {
		float length = (float) Math.sqrt(this.x*this.x + this.y*this.y);
		if (length != 0) {
			this.x /= length;
			this.y /= length;
		}
		return this;
	}

	public float dot(Vector2f other) {
		return this.x * other.x + this.y * other.y;
	}
}