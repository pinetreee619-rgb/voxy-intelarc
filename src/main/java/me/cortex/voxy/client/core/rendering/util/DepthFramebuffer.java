package me.cortex.voxy.client.core.rendering.util;

import me.cortex.voxy.client.core.gl.Capabilities;
import me.cortex.voxy.client.core.gl.GlFramebuffer;
import me.cortex.voxy.client.core.gl.GlTexture;
import me.cortex.voxy.common.util.ThreadUtils;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.ARBDirectStateAccess.nglClearNamedFramebufferfv;
import static org.lwjgl.opengl.ARBDirectStateAccess.nglClearNamedFramebufferiv;
import static org.lwjgl.opengl.GL11C.GL_DEPTH;
import static org.lwjgl.opengl.GL11C.glGetInteger;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30C.*;

public class DepthFramebuffer {
    private final int depthType;
    private GlTexture depthBuffer;
    public final GlFramebuffer framebuffer = new GlFramebuffer();

    public DepthFramebuffer() {
        this(GL_DEPTH_COMPONENT24);
    }

    public DepthFramebuffer(int depthType) {
        this.depthType = depthType;
    }

    public boolean resize(int width, int height) {
        if (this.depthBuffer == null || this.depthBuffer.getWidth() != width || this.depthBuffer.getHeight() != height) {
            if (this.depthBuffer != null) {
                this.depthBuffer.free();
            }
            this.depthBuffer = new GlTexture().store(this.depthType, 1, width, height);
            //glTextureParameteri(this.depthBuffer.id, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            //glTextureParameteri(this.depthBuffer.id, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            this.framebuffer.bind(this.getDepthAttachmentType(), this.depthBuffer).verify();
            return true;
        }
        return false;
    }

    public int getDepthAttachmentType() {
        return this.depthType == GL_DEPTH24_STENCIL8?GL_DEPTH_STENCIL_ATTACHMENT: GL_DEPTH_ATTACHMENT;
    }

    private static boolean useBoundDepthClear() {
        return Capabilities.INSTANCE.isIntel && ThreadUtils.isWindows;
    }

    public void clear(float depth) {
        if (!useBoundDepthClear()) {
            this.clearNamed(depth);
            return;
        }

        // Intel's Windows driver can leave the reverse-Z depth buffer uncleared with DSA.
        int previousFramebuffer = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        try {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, this.framebuffer.id);
            clearBound(depth);
        } finally {
            glBindFramebuffer(GL_DRAW_FRAMEBUFFER, previousFramebuffer);
        }
    }

    public void bindAndClear(float depth) {
        if (useBoundDepthClear()) {
            this.bind();
            clearBound(depth);
        } else {
            this.clearNamed(depth);
            this.bind();
        }
    }

    private void clearNamed(float depth) {
        try (var stack = MemoryStack.stackPush()) {
            nglClearNamedFramebufferfv(this.framebuffer.id, GL_DEPTH, 0, stack.nfloat(depth));
        }
    }

    private static void clearBound(float depth) {
        try (var stack = MemoryStack.stackPush()) {
            nglClearBufferfv(GL_DEPTH, 0, stack.nfloat(depth));
        }
    }

    public void clearStencil(int to) {
        try (var stack = MemoryStack.stackPush()) {
            nglClearNamedFramebufferiv(this.framebuffer.id, GL_STENCIL, 0, stack.nint(to));
        }
    }

    public GlTexture getDepthTex() {
        return this.depthBuffer;
    }

    public void free() {
        this.framebuffer.free();
        if (this.depthBuffer != null) {
            this.depthBuffer.free();
        }
    }

    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, this.framebuffer.id);
    }

    public int getFormat() {
        return this.depthType;
    }
}
