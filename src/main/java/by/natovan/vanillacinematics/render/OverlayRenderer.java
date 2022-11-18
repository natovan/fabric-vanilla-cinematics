package by.natovan.vanillacinematics.render;

import fi.dy.masa.malilib.interfaces.IRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

public class OverlayRenderer implements IRenderer {
    public static final OverlayRenderer INSTANCE = new OverlayRenderer();
    private final MinecraftClient mc;
    private final int wWidth;
    private final int wHeight;

    public OverlayRenderer() {
        this.mc = MinecraftClient.getInstance();
        wWidth = this.mc.getWindow().getWidth();
        wHeight = this.mc.getWindow().getHeight();
    }

    public static OverlayRenderer getInstance() { return INSTANCE; }

    @Override
    public void onRenderGameOverlayPost(MatrixStack matrixStack) {
        if (this.mc.world == null && this.mc.player == null || this.mc.options.hudHidden) {
            return;
        }
    }
}
