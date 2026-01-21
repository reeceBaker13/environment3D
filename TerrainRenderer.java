public class TerrainRenderer {
    public void renderChunk(TerrainChunk node, Renderer renderer, Player player, int[] pixels, float[] zBuffer, int width, int height, boolean[] highlight) {
        if (!node.isVisible(player.getCamera())) {
            return;
        }

        if (node.isLeaf()) {
            renderer.render(node.triangles, player, pixels, zBuffer, width, height, highlight);
        } else {
            for (TerrainChunk child : node.children) {
                renderChunk(child, renderer, player, pixels, zBuffer, width, height, highlight);
            }
        }
    }
}
