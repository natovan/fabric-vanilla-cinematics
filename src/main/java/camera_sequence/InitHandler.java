package camera_sequence;

import camera_sequence.render.RenderHandler;
import fi.dy.masa.malilib.event.RenderEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;

public class InitHandler implements IInitializationHandler {

    @Override
    public void registerModHandlers() {
        RenderHandler renderer = RenderHandler.getInstance();
        RenderEventHandler.getInstance().registerWorldLastRenderer(renderer);
    }
}
