public class Player {
    private Vertex coords;

    private boolean wPressed;
    private boolean sPressed;
    private boolean aPressed;
    private boolean dPressed;

    private int nextX;
    private int nextZ;

    private final double maxSpeed;
    private double currentSpeed; //maybe be vector instead??
    private double acceleration; //should also have a vector or something to move straight forward

    private Camera camera;

    public Player(int x, int y, int z, double maxSpeed) {
        this.coords = new Vertex(x, y, z);

        this.maxSpeed = maxSpeed;
        this.currentSpeed = 0;
        this.acceleration = 0;

        this.camera = new Camera();
    }

    // Getters & Setters
    public Camera getCamera() {
        return this.camera;
    }

    public void setCoords(double x, double y, double z) {
        this.coords.setX(x);
        this.coords.setY(y);
        this.coords.setZ(z);
    }

    public Vertex getCoords() {
        return this.coords;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public double getCurrentSpeed() {
        return this.currentSpeed;
    }

    public double getMaxSpeed() {
        return this.maxSpeed;
    }

    public void setAcceleration(double acceleration) {
        this.acceleration = acceleration;
    }

    public double getAccleration() {
        return this.acceleration;
    }

}