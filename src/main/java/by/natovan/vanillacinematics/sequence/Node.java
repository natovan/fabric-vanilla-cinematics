package by.natovan.vanillacinematics.sequence;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fi.dy.masa.malilib.util.JsonUtils;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec2f;

import javax.annotation.Nullable;

public class Node {

    public boolean isWritten = false;
    private Vec3d standPos;
    private Vec3d eyePos;
    private float yaw;
    private float pitch;
    private int delay; // in ticks
    private String command;

    public Node(Vec3d pos, Vec3d eyePos, float yaw, float pitch, int delay, @Nullable String command) {
        // todo: refactor this, generate eye pos and stand pos here instead of out of the class
        this.standPos   = pos;
        this.eyePos     = eyePos;
        this.yaw        = yaw;
        this.pitch      = pitch;
        this.delay      = delay;
        this.command    = command;
        this.isWritten  = false;
    }

    public Node(JsonObject object) {
        this.standPos   = JsonUtils.vec3dFromJson(object, "standPos");
        this.eyePos     = JsonUtils.vec3dFromJson(object, "eyePos");
        this.yaw        = object.get("yaw").getAsFloat();
        this.pitch      = object.get("pitch").getAsFloat();
        this.delay      = object.get("delay").getAsInt();
        if (object.has("command")) {
            this.command = object.get("command").getAsString();
        }
    }

    public @Nullable String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.isWritten = false;
        this.command = command;
    }
    public Vec3d getStandPos() {
        return this.standPos;
    }

    public Vec3d getEyePos() {
        return this.eyePos;
    }

    public void setPos(Vec3d pos) {
        final float armorStandEyeHeight = 1.7f; // i don't know actually
        final double yOffset = 0.198; 		// i don't know this eather
        this.standPos = new Vec3d(pos.x, pos.y - yOffset, pos.z);
        this.eyePos = new Vec3d(pos.x, pos.y + armorStandEyeHeight, pos.z);
        this.isWritten = false;
    }

    public void setRotation(Vec2f rotation) {
        this.yaw = rotation.x;
        this.pitch = rotation.y;
        this.isWritten = false;
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

    public void setDelay(int delay) {
        this.delay = delay;
        this.isWritten = false;
    }

    public JsonObject toJson() {
        JsonObject node = new JsonObject();
        node.add("standPos", JsonUtils.vec3dToJson(this.standPos));
        node.add("eyePos", JsonUtils.vec3dToJson(this.eyePos));
        node.add("yaw", new JsonPrimitive(this.yaw));
        node.add("pitch", new JsonPrimitive(this.pitch));
        node.add("delay", new JsonPrimitive(this.delay));

        if (this.command != null) {
            node.add("command", new JsonPrimitive(this.command));
        }
        return node;
    }
}
