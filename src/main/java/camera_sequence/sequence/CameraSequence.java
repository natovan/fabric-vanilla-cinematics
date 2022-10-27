package camera_sequence.sequence;

import java.util.ArrayList;
import java.util.List;

public class CameraSequence {
    private String sequenceName;
    private ArrayList<CameraNode> cameraNodes;

    public CameraSequence(String name) {
        this.sequenceName = name;
        this.cameraNodes = new ArrayList<>();
    }

    public void appendCameraNode(CameraNode node) {
        cameraNodes.add(node);
    }

    public List<CameraNode> getCameraNodes() {
        return cameraNodes;
    }

    public String getSequenceName() {
        return sequenceName;
    }
}
