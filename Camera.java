public class Camera {
    // Camera variables
    public float yaw;
    public float pitch;

    public float x;
    public float y;
    public float z;

    // Constructor
    public Camera() {
        this.yaw = (float) -90;//(1 * Math.PI / 4);
        this.pitch = (float) 200;

        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

}