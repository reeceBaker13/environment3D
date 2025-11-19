public class Map {
    private final int mapSize;
    private List<Triangles> map;
    private double[][] this.heightMap;

    private final double this.yScale;
    private double mapCentre;

    public Map(int mapSize, double yScale) {
        this.mapSize = mapSize;
        this.yScale = yScale;
    }
    
    // Generating triangles for the map
    public List<Triangle> generateTriangles() {
        // Getting map of heights
        this.heightMap = generateNoise(this.mapSize);

        // Creating 2 triangles for each square on the grid
        List<Triangle> tris = new ArrayList<>();
        for (int i = 0; i < this.mapSize - 1; i++) {
        	for (int j = 0; j < this.mapSize - 1; j++) {
                float shade = (float) ((this.heightMap[i][j] + 1) / 2.0);
                Color c = new Color(shade, shade, shade);

                Vertex v00 = new Vertex(i - half, this.heightMap[i][j] * this.yScale, j - half);
                Vertex v10 = new Vertex((i + 1) - half, this.heightMap[i + 1][j] * this.yScale, j - half);
                Vertex v01 = new Vertex(i - half, this.heightMap[i][j + 1] * this.yScale, (j + 1) - half);
                Vertex v11 = new Vertex((i + 1) - half, this.heightMap[i + 1][j + 1] * this.yScale, (j + 1) - half);

                tris.add(new Triangle(v00, v10, v01, c));
                tris.add(new Triangle(v10, v11, v01, c));
            }
        }

        return tris;
    }

	// Generating noise map
    public double[][] generateNoise(int size) {
        Noise noise = new Noise();
        
        double[][] array = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                array[i][j] = noise.noise(i * 0.05, j * 0.05, 1);
            }
        }

        return array;
    }
}