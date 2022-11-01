package camera_sequence.sequence;

import net.minecraft.util.math.Vec3d;

public class Node {
    private Vec3d pos;
    private float yaw;
    private float pitch;
    private int delay; // in ticks

    public Node(Vec3d pos, float yaw, float pitch, int delay) {
        this.pos = pos;
        this.yaw = yaw;
        this.pitch = pitch;
        this.delay = delay;
    }

    public Vec3d getPos() {
        return pos;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public int getDelay() {
        return delay;
    }
}
