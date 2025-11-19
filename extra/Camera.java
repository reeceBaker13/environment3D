public class Camera {
    // Camera variables
    private double heading;
    private double pitch;
    private double zoom;
    
    // Mouse variables
    private int lastMouseX;
    private int lastMouseY;

    // Matrices
    private Matrix3 headingMatrix;
    private Matrix3 pitchMatrix;
    private Matrix3 zoomMatrix;
    private Matrix3 translationMatrix;

    // Constructor
    public Camera() {
        this.heading = 3 * Math.PI / 4;
        this.pitch = 7 * Math.PI / 4;
        this.zoom = 5.0;

        this.lastMouseX = -1;
        this.lastMouseY = -1;
    }

    // Getters & Setters
    public double getHeading() {
        return this.heading;
    }

}