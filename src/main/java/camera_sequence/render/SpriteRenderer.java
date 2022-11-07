package camera_sequence.render;

import com.mojang.blaze3d.systems.RenderSystem;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class SpriteRenderer {
    public static final SpriteRenderer INSTANCE = new SpriteRenderer();
    private static final Identifier MARIO = new Identifier("modid", "textures/misc/mario.png");
    private final RenderObject spriteObject;
    private final ArrayList<Vec3d> testPos;

    public SpriteRenderer() {
        spriteObject = new RenderObject(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE, GameRenderer::getPositionTexShader);
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(-0.5, 0.5, 0).texture(0, 0).next();
        buffer.vertex(-0.5, -0.5, 0).texture(0, 1).next();
        buffer.vertex(0.5, -0.5, 0).texture(1, 1).next();
        buffer.vertex(0.5, 0.5, 0).texture(1, 0).next();
        spriteObject.uploadData(buffer);

        testPos = new ArrayList<>();
        testPos.add(new Vec3d(1, 0, 3));
        testPos.add(new Vec3d(3, 5, 1));
        testPos.add(new Vec3d(6, 3, 4));
        testPos.add(new Vec3d(4, 4, 6));
    }

    // TODO: can get camera from here
    public void render(MatrixStack matrixStack, Matrix4f projMatrix, Camera gameCamera) {
        RenderSystem.enableTexture();
        RenderUtils.bindTexture(MARIO);
        RenderSystem.disableCull();
        RenderSystem.depthMask(true);

        for (Vec3d pos : testPos) {
            matrixStack.push();
            matrixStack.translate(
                    pos.x + 0.5 - gameCamera.getPos().x,
                    pos.y + 0.5 - gameCamera.getPos().y,
                    pos.z + 0.5 - gameCamera.getPos().z);
            matrixStack.multiply(gameCamera.getRotation());
            this.spriteObject.draw(matrixStack, projMatrix);
            matrixStack.pop();
        }
    }
}
