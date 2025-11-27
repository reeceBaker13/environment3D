public class Vector2f {
	public float x, y;

	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vector2f add(Vector2f other) {
        this.x += other.x;
        this.y += other.y;
        return this;
    }

	public Vector2f copy() {
		return new Vector2f(x, y);
	}

	public float dot(Vector2f other) {
		return this.x * other.x + this.y * other.y;
	}
}