package camera_sequence;

import camera_sequence.render.OverlayRenderer;
import camera_sequence.render.WorldRenderer;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;

public class InitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        WorldRenderer worldRenderer = WorldRenderer.getInstance();
        RenderEventHandler.getInstance().registerWorldLastRenderer(worldRenderer);

        OverlayRenderer overlayRenderer = OverlayRenderer.getInstance();
        RenderEventHandler.getInstance().registerGameOverlayRenderer(overlayRenderer);
    }
}
