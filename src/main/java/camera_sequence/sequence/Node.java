package camera_sequence.sequence;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.util.JsonUtils;
import net.minecraft.util.math.Vec3d;

public class Node {
    private final Vec3d standPos;
    private final Vec3d eyePos;
    private final float yaw;
    private final float pitch;
    private final int delay; // in ticks

    public Node(Vec3d pos, Vec3d eyePos, float yaw, float pitch, int delay) {
        this.standPos   = pos;
        this.eyePos     = eyePos;
        this.yaw        = yaw;
        this.pitch      = pitch;
        this.delay      = delay;
    }

    public Node(JsonObject object) {
        this.standPos   = JsonUtils.vec3dFromJson(object, "standPos");
        this.eyePos     = JsonUtils.vec3dFromJson(object, "eyePos");
        this.yaw        = object.get("yaw").getAsFloat();
        this.pitch      = object.get("pitch").getAsFloat();
        this.delay      = object.get("delay").getAsInt();
    }

    public Vec3d getStandPos() {
        return this.standPos;
    }

    public Vec3d getEyePos() {
        return this.eyePos;
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

    public JsonObject toJson() {
        JsonObject node = new JsonObject();
        node.add("standPos", JsonUtils.vec3dToJson(this.standPos));
        node.add("eyePos", JsonUtils.vec3dToJson(this.eyePos));
        node.add("yaw", new JsonPrimitive(this.yaw));
        node.add("pitch", new JsonPrimitive(this.pitch));
        node.add("delay", new JsonPrimitive(this.delay));
        return node;
    }
}
