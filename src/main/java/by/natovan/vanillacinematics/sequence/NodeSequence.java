package by.natovan.vanillacinematics.sequence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;

public class NodeSequence {
    private String sequenceName;
    private final ArrayList<Node> cameraNodes;

    public NodeSequence(String name) {
        this.sequenceName = name;
        this.cameraNodes = new ArrayList<>();
    }

    public NodeSequence(JsonObject object) {
        this.sequenceName = object.get("name").getAsString();
        this.cameraNodes = new ArrayList<>();

        JsonArray nodes = object.get("nodes").getAsJsonArray();
        for (Object o : nodes) {
            JsonObject nodeObj = (JsonObject) o;
            Node n = new Node(nodeObj);
            n.isWritten = true;
            this.appendCameraNode(n);
        }
    }

    public void appendCameraNode(Node node) {
        cameraNodes.add(node);
    }

    public void deleteCameraNode(int index) {
        this.cameraNodes.remove(index);
    }

    public ArrayList<Node> getCameraNodes() {
        return cameraNodes;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setName(String name) {
	this.sequenceName = name;
    }

    public void markAsWritten() {
        for (Node n : this.cameraNodes) { n.isWritten = true; }
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.add("name", new JsonPrimitive(this.sequenceName));

        JsonArray nodes = new JsonArray();
        for (Node n : cameraNodes) {
            nodes.add(n.toJson());
        }
        obj.add("nodes", nodes);
        return obj;
    }
}
