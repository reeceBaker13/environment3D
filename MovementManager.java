import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class MovementManager {						// Previous mouse y position (-1 is no mouse movement)

    // Game objects
    private final Player player;
    private final Map map;

    public MovementManager(Player player, Map map) {
        // Game variables
        this.player = player;
        this.map = map;
    }

    public void update(double deltaTime, InputState input) {
        player.turn(
                input.lookX,
                input.lookY
        );

        input.lookX = 0;
        input.lookY = 0;

        player.move(
                deltaTime,
                input.moveX,
                input.moveZ,
                input.sprinting
        );

        if (input.jumping) {
            player.jump();
        }

        player.updateY(deltaTime, getElevation());
    }

    private float getElevation() {
        int x = (int) Math.floor(player.getPosition().x + map.getMapCentre());
        int z = (int) Math.floor(player.getPosition().z + map.getMapCentre());

        if (x >= 0 && z >= 0 && x < map.getMapSize() - 1 && z < map.getMapSize() - 1) {
            int idx = x * (map.getMapSize() - 1) + z;
            float localX = (player.getPosition().x + map.getMapCentre()) - x;
            float localZ = (player.getPosition().z + map.getMapCentre()) - z;

            int triOffset;
            if (localX + localZ < 1.0f) {
                triOffset = 0;
            } else {
                triOffset = 1;
            }

            Triangle t = map.getMap().get(idx * 2 + triOffset);

            if (t != null) {
                float fx = player.getPosition().x + map.getMapCentre();
                float fz = player.getPosition().z + map.getMapCentre();

                int x0 = (int) Math.floor(fx);
                int z0 = (int) Math.floor(fz);

                float tx = fx - x0;
                float tz = fz - z0;

                float h00 = map.getHeightMap()[x0][z0];
                float h10 = map.getHeightMap()[x0 + 1][z0];
                float h01 = map.getHeightMap()[x0][z0 + 1];
                float h11 = map.getHeightMap()[x0 + 1][z0 + 1];

                float h0 = h00 * (1 - tx) + h10 * tx;
                float h1 = h01 * (1 - tx) + h11 * tx;

                return (h0 * (1 - tz) + h1 * tz) * map.getYScale();
            }
        }

        return 0;

    }

}