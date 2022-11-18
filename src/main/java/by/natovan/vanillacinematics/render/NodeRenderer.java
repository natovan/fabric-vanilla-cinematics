package by.natovan.vanillacinematics.render;

import by.natovan.vanillacinematics.VanillaCinematics;
import by.natovan.vanillacinematics.sequence.Node;
import by.natovan.vanillacinematics.sequence.NodeSequence;
import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class NodeRenderer {
    public static final NodeRenderer INSTANCE = new NodeRenderer();
    private static final Identifier TEXTURE = new Identifier("modid", "textures/misc/camera.png");
    private final MinecraftClient mc;
    public boolean shouldRender = true;
    private final RenderObject spriteObject, lineObject;
    private final List<String> strings = new ArrayList<>();

    public NodeRenderer() {
        this.mc = MinecraftClient.getInstance();

        spriteObject = new RenderObject(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE, GameRenderer::getPositionTexShader);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(-0.5, 0.5, 0).texture(1, 0).next();
        buffer.vertex(-0.5, -0.5, 0).texture(1, 1).next();
        buffer.vertex(0.5, -0.5, 0).texture(0, 1).next();
        buffer.vertex(0.5, 0.5, 0).texture(0, 0).next();
        spriteObject.uploadData(buffer);

        lineObject = new RenderObject(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR, GameRenderer::getPositionColorShader);
    }

    public void render(MatrixStack matrixStack, Matrix4f projMatrix) {
        if (!this.shouldRender) return;
        Camera c = this.mc.gameRenderer.getCamera();
        BufferBuilder bb = Tessellator.getInstance().getBuffer();

        float viewDist = this.mc.gameRenderer.getViewDistance();
        for (NodeSequence seq : VanillaCinematics.sequences) {
            ArrayList<Node> nodes = seq.getCameraNodes();
            for (int i = 0; i < nodes.size(); i++) {


                // Drawing sprites
                RenderSystem.disableCull();
                RenderUtils.setupBlend();
                RenderUtils.color(1.0f, 1.0f, 1.0f, 0.45f);
                Node n = nodes.get(i);
                if (c.getPos().distanceTo(n.getStandPos()) > viewDist) continue;
                matrixStack.push();
                matrixStack.translate(
                        n.getEyePos().x - c.getPos().x,
                        n.getEyePos().y - c.getPos().y,
                        n.getEyePos().z - c.getPos().z);
                matrixStack.multiply(c.getRotation());
                RenderSystem.enableTexture();
                RenderUtils.bindTexture(TEXTURE);
                this.spriteObject.draw(matrixStack, projMatrix);
                matrixStack.pop();
                RenderUtils.color(1.0f, 1.0f, 1.0f, 1.0f);


                // Drawing lines in between
                // TODO: bb on update

                if (i + 1 < nodes.size()) {
                    Vec3d p1 = n.getEyePos();
                    Vec3d p2 = nodes.get(i + 1).getEyePos();

                    bb.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                    bb.vertex(p1.x, p1.y, p1.z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
                    bb.vertex(p2.x, p2.y, p2.z).color(1.0f, 1.0f, 1.0f, 1.0f).next();
                    this.lineObject.uploadData(bb);
                    bb.clear();

                    matrixStack.push();
                    matrixStack.translate(0 - c.getPos().x, 0 - c.getPos().y, 0 - c.getPos().z);
                    lineObject.draw(matrixStack, projMatrix);
                    matrixStack.pop();
                }


                // Drawing floating text

                final double textRenderDist = 10;
                if (c.getPos().distanceTo(n.getEyePos()) < textRenderDist) {
                    this.strings.add(seq.getSequenceName() + " #" + i);
                    this.strings.add("Delay: " + n.getDelay());
                    if (n.getCommand() != null) strings.add(n.getCommand());
                    RenderUtils.drawTextPlate(strings, n.getEyePos().x, n.getEyePos().y + 0.8, n.getEyePos().z, 0.01f);
                    this.strings.clear();
                }
            }
        }
    }

    public void deleteGlResources() {
        this.spriteObject.deleteGlResources();
        this.lineObject.deleteGlResources();
    }
}
