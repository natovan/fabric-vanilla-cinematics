package camera_sequence.render;

import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import java.awt.*;

public class WorldRenderer implements IRenderer {
    private static final WorldRenderer INSTANCE = new WorldRenderer();
    private final MinecraftClient mc;
    public WorldRenderer() {
        this.mc = MinecraftClient.getInstance();
    }
    public static WorldRenderer getInstance() {
        return INSTANCE;
    }

    @Override
    public void onRenderWorldLast(MatrixStack matrixStack, Matrix4f projMatrix) {
        if (this.mc.world == null && this.mc.player == null || this.mc.options.hudHidden) {
            return;
        }

        NodeRenderer.INSTANCE.render(matrixStack, projMatrix);
    }

    @Override
    public void onRenderGameOverlayPost(MatrixStack matrixStack) {
        // test
        RenderUtils.renderText(0, 0, Color.WHITE.getRGB(), "ADSDADSASDASDASDASDADASDASDADASDADSADASDA", matrixStack);
    }
}
