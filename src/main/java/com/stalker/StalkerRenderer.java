package com.stalker;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class StalkerRenderer extends EntityRenderer<StalkerEntity> {
    static final ResourceLocation NORMAL = new ResourceLocation("stalkermod", "textures/entity/stalker.png");
    static final ResourceLocation ANGRY = new ResourceLocation("stalkermod", "textures/entity/stalker_angry.png");

    public StalkerRenderer(EntityRendererProvider.Context c) { super(c); }

    @Override public ResourceLocation getTextureLocation(StalkerEntity e) { return e.isAngry() ? ANGRY : NORMAL; }

    @Override public void render(StalkerEntity e, float yaw, float pt, PoseStack ps, MultiBufferSource buf, int light) {
        ps.pushPose();
        ps.mulPose(entityRenderDispatcher.cameraOrientation());
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(e)));
        Matrix4f m = ps.last().pose(); Matrix3f n = ps.last().normal();
        float w = 1.25f, h = 2.5f;
        v(vc, m, n, -w, 0, 1, 1, light); v(vc, m, n, w, 0, 0, 1, light);
        v(vc, m, n, w, h, 0, 0, light);  v(vc, m, n, -w, h, 1, 0, light);
        ps.popPose();
    }

    void v(VertexConsumer vc, Matrix4f m, Matrix3f n, float x, float y, float u, float v, int light) {
        vc.vertex(m, x, y, 0).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
          .uv2(light).normal(n, 0, 1, 0).endVertex();
    }
}
