import java.awt.Color;
import java.util.List;
import java.util.ArrayList;

public class Map {
    private final int mapSize;
    private final List<Triangle> map;
    private float[][] heightMap;

    private final float yScale;
    private final float mapCentre;

    public Map(int mapSize, float yScale) {
        this.mapSize = mapSize;
        this.yScale = yScale;
        this.mapCentre = (mapSize - 1) / 2.0f;

        this.map = generateTriangles();
    }
    
    // Getters & Setters
    public float[][] getHeightMap() {
        return this.heightMap;
    }

    public float getMapCentre() {
        return this.mapCentre;
    }

    public float getYScale() {
        return this.yScale;
    }

    public int getMapSize() { return this.mapSize; }

    public List<Triangle> getMap() { return this.map; }
    
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

                Vector3f v00 = new Vector3f(i - this.mapCentre, this.heightMap[i][j] * this.yScale, j - this.mapCentre);
                Vector3f v10 = new Vector3f((i + 1) - this.mapCentre, this.heightMap[i + 1][j] * this.yScale, j - this.mapCentre);
                Vector3f v01 = new Vector3f(i - this.mapCentre, this.heightMap[i][j + 1] * this.yScale, (j + 1) - this.mapCentre);
                Vector3f v11 = new Vector3f((i + 1) - this.mapCentre, this.heightMap[i + 1][j + 1] * this.yScale, (j + 1) - this.mapCentre);

                tris.add(new Triangle(v00, v10, v01, c));
                tris.add(new Triangle(v10, v11, v01, c));
            }
        }

        return tris;
    }

	// Generating noise map
    public float[][] generateNoise(int size) {
        float[][] array = new float[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                array[i][j] = Noise.noise(i * 0.05f, j * 0.05f, 1f);
            }
        }

        return array;
    }
}