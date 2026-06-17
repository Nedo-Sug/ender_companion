package com.nedosug.endercompanion.client.renderer;

import com.nedosug.endercompanion.client.model.EnderCompanionModel;
import com.nedosug.endercompanion.entity.EnderCompanionEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@Environment(EnvType.CLIENT)
public class EnderCompanionRenderer extends GeoEntityRenderer<EnderCompanionEntity> {
    public EnderCompanionRenderer(EntityRendererProvider.Context context) {
        super(context, new EnderCompanionModel());
        this.shadowRadius = 0.5F;
    }
}
