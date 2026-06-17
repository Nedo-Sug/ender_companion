package com.nedosug.endercompanion.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nedosug.endercompanion.entity.EnderCompanionEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class EnderCompanionModel<T extends EnderCompanionEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation("endercompanion", "ender_companion"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;
    private final ModelPart right_leg;
    private final ModelPart left_leg;

    public EnderCompanionModel(ModelPart root) {
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.right_arm = root.getChild("right_arm");
        this.left_arm = root.getChild("left_arm");
        this.right_leg = root.getChild("right_leg");
        this.left_leg = root.getChild("left_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        head.addOrReplaceChild("cube_r1",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -28.0F, 2.0F, -3.1416F, 3.1416F, 3.1416F));

        PartDefinition body = partdefinition.addOrReplaceChild("body",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        body.addOrReplaceChild("cube_r2",
                CubeListBuilder.create()
                        .texOffs(40, 15).addBox(-3.0F, -4.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(30, 36).addBox(0.0F, -4.0F, -1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -24.0F, -1.0F, -0.5672F, 0.0F, 0.0F));
        body.addOrReplaceChild("cube_r3",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-4.0F, -5.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-4.0F, -23.0F, 2.0F, -3.1416F, 3.1416F, 3.1416F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        right_arm.addOrReplaceChild("cube_r4",
                CubeListBuilder.create().texOffs(16, 36)
                        .addBox(-2.0F, -1.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(2.0F, -27.0F, 1.25F, -3.1416F, 3.1416F, 3.1416F));

        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create(),
                PartPose.offset(-10.0F, 24.0F, 0.0F));
        left_arm.addOrReplaceChild("cube_r5",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-2.0F, -1.0F, -1.0F, 3.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(1.0F, -27.0F, 1.0F, -3.1416F, 3.1416F, 3.1416F));

        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        right_leg.addOrReplaceChild("cube_r6",
                CubeListBuilder.create().texOffs(24, 16)
                        .addBox(-2.5F, 0.0F, -1.5F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-5.5F, -16.0F, 1.5F, -3.1416F, 3.1416F, 3.1416F));

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        left_leg.addOrReplaceChild("cube_r7",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-2.5F, 0.0F, -1.5F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-1.5F, -16.0F, 1.5F, -3.1416F, 3.1416F, 3.1416F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // Head follows player's look direction
        // The child cubes have a (-π, π, π) flip, so yaw/pitch signs are inverted relative to vanilla
        this.head.yRot = netHeadYaw * (Mth.PI / 180.0F);
        this.head.xRot = headPitch * (Mth.PI / 180.0F);

        // Biped walk cycle — arms counter-swing to legs
        this.right_arm.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 2.0F * limbSwingAmount * 0.5F;
        this.left_arm.xRot  = Mth.cos(limbSwing * 0.6662F)           * 2.0F * limbSwingAmount * 0.5F;
        this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F)           * 1.4F * limbSwingAmount;
        this.left_leg.xRot  = Mth.cos(limbSwing * 0.6662F + Mth.PI)  * 1.4F * limbSwingAmount;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_arm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
