package camera_sequence.render;

import fi.dy.masa.malilib.interfaces.IRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

public class RenderHandler implements IRenderer {
    private static final RenderHandler INSTANCE = new RenderHandler();
    private final MinecraftClient mc;
    public RenderHandler() {
        this.mc = MinecraftClient.getInstance();
    }
    public static RenderHandler getInstance() {
        return INSTANCE;
    }

    @Override
    public void onRenderWorldLast(MatrixStack matrixStack, Matrix4f projMatrix) {
        if (this.mc.world == null && this.mc.player == null) {
            return;
        }

        SpriteRenderer.INSTANCE.render(matrixStack, projMatrix, this.mc.gameRenderer.getCamera());
    }
}
