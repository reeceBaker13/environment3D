public class Player {
    // Movement vectors
    private final Vector3f position;
    private final Vector3f desiredVelocity;
    private final Vector3f forward;
    private final Vector3f right;

    // Jumping forces
    private float groundY;
    private final float jumpForce = 9.8f; //??~~##
    private final float gravity = -9.8f;
    private float upVelocity;

    // Movement variables
    private final float speed = 1.0f;
    private final float sprintModifier = 3.5f;
    private boolean isJumping = false;


    // Camera variables
    private final Camera camera;
	private static final float SENSITIVITY = 0.1f;
	private static final float EYE_HEIGHT = 2f ;

    // Constructor
    public Player(int x, int y, int z) {
        this.position = new Vector3f(x, y, z);
        this.desiredVelocity = new Vector3f();

        this.forward = new Vector3f();
        this.right = new Vector3f();

        this.camera = new Camera();
    }

    // Getters & Setters
    public Camera getCamera() {
        return this.camera;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    // Controller update position method
    public void updatePosition(double dt, float xAxis, float yAxis, boolean sprinting) {
        applyMovement(dt, xAxis, yAxis, sprinting);
    }

    // Keyboard update position method
    public void updatePosition(double dt, boolean W, boolean S, boolean A, boolean D, boolean sprinting) {
        float yAxis = (W ? -1f : 0f) + (S ? 1f : 0f);
        float xAxis = (D ? 1f : 0f) + (A ? -1f : 0f);

        applyMovement(dt, xAxis, yAxis, sprinting);
    }

    // Applying movement to player
    private void applyMovement(double dt, float xAxis, float yAxis, boolean sprinting) {
        float rad = (float) Math.toRadians(this.camera.yaw);

        this.forward.set((float) Math.sin(rad), 0, (float) -Math.cos(rad));
        this.right.set((float) Math.cos(rad), 0, (float) Math.sin(rad));

        this.desiredVelocity.reset();

        float targetSpeed = speed * (sprinting ? sprintModifier : 1);

        desiredVelocity.x = forward.x * yAxis + right.x * xAxis;
        desiredVelocity.z = forward.z * yAxis + right.z * xAxis;

        if (desiredVelocity.lengthSquared() > 0) {
            desiredVelocity.normalize().mul(targetSpeed);
            position.add(desiredVelocity.mul((float) dt));
        }

        // Camera
        this.camera.x = this.position.x;
        this.camera.z = this.position.z;
    }

    public void jump() {
        if (!this.isJumping) {
            this.isJumping = true;
            this.upVelocity = jumpForce;
        }
    }

	public void updateY(double dt, float y) {
        this.groundY = y;

        if (isJumping) {
            this.position.y += (float) (this.upVelocity * dt);
            this.upVelocity += (float) (this.gravity * dt);
            if (this.position.y < this.groundY) {
                this.position.y = this.groundY;
                this.isJumping = false;
            }
        } else {
            this.position.y = this.groundY;
        }

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