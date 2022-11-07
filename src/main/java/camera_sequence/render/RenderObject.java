package camera_sequence.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;

import java.util.function.Supplier;

public class RenderObject {
    private final VertexFormat.DrawMode glMode;
    private final VertexBuffer vertexBuffer;
    private final boolean hasTexture;
    private final Supplier<Shader> shader;

    public RenderObject(VertexFormat.DrawMode glMode, VertexFormat format, Supplier<Shader> shader) {
        this.glMode = glMode;
        this.shader = shader;
        this.vertexBuffer = new VertexBuffer();

        boolean hasTexture = false;

        // This isn't really that nice and clean, but it'll do for now...
        for (VertexFormatElement el : format.getElements()) {
            if (el.getType() == VertexFormatElement.Type.UV) {
                hasTexture = true;
                break;
            }
        }

        this.hasTexture = hasTexture;
    }

    public VertexFormat.DrawMode getGlMode() {
        return this.glMode;
    }

    public Supplier<Shader> getShader() {
        return this.shader;
    }

    public void uploadData(BufferBuilder buffer) {
        BufferBuilder.BuiltBuffer builtBuffer = buffer.end();
        this.vertexBuffer.bind();
        this.vertexBuffer.upload(builtBuffer);
        VertexBuffer.unbind();
    }

    public void draw(MatrixStack matrixStack, Matrix4f projMatrix) {
        if (this.hasTexture) RenderSystem.enableTexture();

        RenderSystem.setShader(this.getShader());
        this.vertexBuffer.bind();
        this.vertexBuffer.draw(matrixStack.peek().getPositionMatrix(), projMatrix, this.getShader().get());
        VertexBuffer.unbind();

        if (this.hasTexture) RenderSystem.disableTexture();
    }

    public void deleteGlResources() {
        this.vertexBuffer.close();
    }
}