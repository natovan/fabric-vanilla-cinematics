package camera_sequence.sequence;

import java.util.ArrayList;
import java.util.List;

public class NodeSequence {
    private String sequenceName;
    private ArrayList<Node> cameraNodes;

    public NodeSequence(String name) {
        this.sequenceName = name;
        this.cameraNodes = new ArrayList<>();
    }

    public void appendCameraNode(Node node) {
        cameraNodes.add(node);
    }

    public ArrayList<Node> getCameraNodes() {
        return cameraNodes;
    }

    public String getSequenceName() {
        return sequenceName;
    }
}
