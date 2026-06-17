package com.nedosug.endercompanion.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nedosug.endercompanion.client.model.EnderCompanionModel;
import com.nedosug.endercompanion.entity.EnderCompanionEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renderer for the Ender Companion entity.
 *
 * <p>Scale is applied globally via {@link #scale} so that the model hierarchy
 * remains intact and all joint pivots stay correct. A scale of 1.4 makes the
 * companion appear taller than a standard player (~1.75 blocks visual height).
 *
 * <p>The shadow radius (0.5f) is left at a compact value; it will be scaled
 * proportionally by the engine along with the entity.
 */
@Environment(EnvType.CLIENT)
public class EnderCompanionRenderer
        extends MobRenderer<EnderCompanionEntity, EnderCompanionModel<EnderCompanionEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("endercompanion", "textures/entity/ender_companion.png");

    /**
     * Global visual scale factor.
     * Applied via matrixStack.scale() so the entire model hierarchy scales
     * uniformly without touching any part positions or pivot offsets.
     */
    private static final float SCALE = 1.4F;

    public EnderCompanionRenderer(EntityRendererProvider.Context context) {
        super(context,
                new EnderCompanionModel<>(context.bakeLayer(EnderCompanionModel.LAYER_LOCATION)),
                0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderCompanionEntity entity) {
        return TEXTURE;
    }

    /**
     * Apply a uniform scale to the model.
     *
     * <p>This is the ONLY place where size is adjusted — never in the model
     * geometry or setupAnim. Using the poseStack here ensures the entire
     * bone hierarchy is scaled from the entity's root, preserving all
     * joint positions and pivot relationships.
     */
    @Override
    protected void scale(EnderCompanionEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
