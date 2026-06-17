package com.nedosug.endercompanion.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nedosug.endercompanion.client.model.EnderCompanionModel;
import com.nedosug.endercompanion.entity.EnderCompanionEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class EnderCompanionRenderer extends MobRenderer<EnderCompanionEntity, EnderCompanionModel<EnderCompanionEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation("endercompanion", "textures/entity/ender_companion.png");

    /** Visual scale applied via matrix — keeps texture pixel density intact. */
    private static final float SCALE = 1.35F;

    public EnderCompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new EnderCompanionModel<>(context.bakeLayer(EnderCompanionModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderCompanionEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(EnderCompanionEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }
}
