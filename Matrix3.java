public class Matrix3 {
    private double[] values;

    public Matrix3(double[] values) {
        this.values = values;
    }

    public double[] getValues() {
        return this.values;
    }

    public Matrix3 multiply(Matrix3 other) {
        double[] result = new double[9];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                for (int i = 0; i < 3; i++) {
                    result[row * 3 + col] += this.values[row * 3 + i] * other.values[i * 3 + col];
                }
            }
        }
        return new Matrix3(result);
    }

    public Vertex transform(Vertex in, Vertex out, double translateX, double translateZ) {
        double x = in.getX() - translateX;
        double y = in.getY();
        double z = in.getZ() - translateZ;
        out.setX(x * this.values[0] + y * this.values[3] + z * this.values[6]);
        out.setY(x * this.values[1] + y * this.values[4] + z * this.values[7]);
        out.setZ(x * this.values[2] + y * this.values[5] + z * this.values[8]);
		return out;
    }

    public Vertex transform(Vertex in, double translateX, double translateZ) {
        Vertex out = new Vertex(0, 0, 0);
        return transform(in, out, translateX, translateZ);
    }
}