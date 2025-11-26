public class Player {
    private Vector3f position;
	private Vector3f velocity;

    private final float maxSpeed;
    private float accel = 1f;
	private float friction = 0.6f;

    private Camera camera;
	private float sensitivity = 0.1f;

	private static final float EYE_HEIGHT = 2f ;

    public Player(int x, int y, int z, float maxSpeed) {
        this.position = new Vector3f(x, y, z);
        this.velocity = new Vector3f();

        this.maxSpeed = maxSpeed;

        this.camera = new Camera();
    }

    // Getters & Setters
    public Camera getCamera() {
        return this.camera;
    }

    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;

		this.camera.x = x;
		this.camera.y = y + this.EYE_HEIGHT;
		this.camera.z = z;
    }

    public Vector3f getPosition() {
        return this.position;
    }

	public void updatePosition(float dt, boolean W, boolean A, boolean S, boolean D, boolean space, boolean shift) {
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
		this.camera.y = this.position.y + this.EYE_HEIGHT;
		this.camera.z = this.position.z;
	}

	public void updateY(float y) {
		this.position.y = y;
		this.camera.y = this.position.y + this.EYE_HEIGHT;
	}

	public void turn(float mouseDx, float mouseDy) {
		this.camera.yaw += mouseDx * sensitivity;
		this.camera.pitch -= mouseDy * sensitivity;

		// Clamping pitch
		if (this.camera.pitch > 230) this.camera.pitch = 230;
		if (this.camera.pitch < 150) this.camera.pitch = 150;
	}

}