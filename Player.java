public class Player {
    private final Vector3f position;
	private final Vector3f velocity;

    private final float accel = 1f;
	private final float friction = 0.6f;

    private final Camera camera;
	private static final float SENSITIVITY = 0.1f;

	private static final float EYE_HEIGHT = 2f ;

    public Player(int x, int y, int z) {
        this.position = new Vector3f(x, y, z);
        this.velocity = new Vector3f();


        this.camera = new Camera();
    }

    // Getters & Setters
    public Camera getCamera() {
        return this.camera;
    }

    public Vector3f getPosition() {
        return this.position;
    }

	public void updatePosition(float dt, float xAxis, float yAxis, boolean space, boolean shift) {
		float rad = (float) Math.toRadians(this.camera.yaw);

		Vector3f forward = new Vector3f((float) Math.sin(rad), 0, (float) -Math.cos(rad));
		Vector3f right = new Vector3f((float) Math.cos(rad), 0, (float) Math.sin(rad));

		Vector3f acceleration = new Vector3f(0, 0, 0);

		acceleration.add(forward.mul(accel).mul(yAxis));
		acceleration.add(right.mul(accel).mul(xAxis));

		velocity.add(acceleration.mul(dt));

		velocity.mul(1f - friction * dt);

		position.add(velocity.mul(dt));

		if (space) position.add(new Vector3f(0, 1, 0));
		if (shift) position.add(new Vector3f(0, -1, 0));

		// Camera
		this.camera.x = this.position.x;
		this.camera.y = this.position.y + EYE_HEIGHT;
		this.camera.z = this.position.z;
	}

    public void updatePosition(float dt, boolean W, boolean S, boolean A, boolean D, boolean space, boolean shift) {
        float rad = (float) Math.toRadians(this.camera.yaw);

        Vector3f forward = new Vector3f((float) Math.sin(rad), 0, (float) -Math.cos(rad));
        Vector3f right = new Vector3f((float) Math.cos(rad), 0, (float) Math.sin(rad));

        Vector3f acceleration = new Vector3f(0, 0, 0);

        if (W) acceleration.add(forward.mul(-accel));
        if (S) acceleration.add(forward.mul(accel));
        if (A) acceleration.add(right.mul(-accel));
        if (D) acceleration.add(right.mul(accel));

        velocity.add(acceleration.mul(dt));

        velocity.mul(1f - friction * dt);

        position.add(velocity.mul(dt));

        if (space) position.add(new Vector3f(0, 1, 0));
        if (shift) position.add(new Vector3f(0, -1, 0));

        // Camera
        this.camera.x = this.position.x;
        this.camera.y = this.position.y + EYE_HEIGHT;
        this.camera.z = this.position.z;
    }

	public void updateY(float y) {
		this.position.y = y;
		this.camera.y = this.position.y + EYE_HEIGHT;
	}

	public void turn(float dx, float dy) {
		this.camera.yaw += dx * SENSITIVITY;
		this.camera.pitch -= dy * SENSITIVITY;

		// Clamping pitch
		if (this.camera.pitch > 230) this.camera.pitch = 230;
		if (this.camera.pitch < 150) this.camera.pitch = 150;
	}

}