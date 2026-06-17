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

/**
 * Ender Companion entity model.
 *
 * <p>Pivot design follows standard Minecraft humanoid conventions so that
 * setupAnim rotation works correctly around anatomical joints:
 * <ul>
 *   <li>head     pivot: (0, 0, 0)   — neck base, head hangs -8..0 above</li>
 *   <li>body     pivot: (0, 0, 0)   — torso top, body extends 0..12 downward</li>
 *   <li>right_arm pivot: (-5, 2, 0) — right shoulder (entity's right = -x)</li>
 *   <li>left_arm  pivot: (5, 2, 0)  — left shoulder (entity's left  = +x)</li>
 *   <li>right_leg pivot: (-1.9, 12, 0) — right hip</li>
 *   <li>left_leg  pivot: (1.9, 12, 0)  — left hip</li>
 * </ul>
 *
 * <p>UV mapping is taken verbatim from the Blockbench export (ender_companion.java).
 * Cube sizes are adapted to fit standard pivots:
 * head 8×8×8, body 8×12×4, arms 3×11×4 (slim), legs 4×16×4.
 *
 * <p>The collar decoration (cube_r2 from Blockbench) is kept as a sub-child of
 * the body bone so it moves with the torso.
 */
@Environment(EnvType.CLIENT)
public class EnderCompanionModel<T extends EnderCompanionEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(new ResourceLocation("endercompanion", "ender_companion"), "main");

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public EnderCompanionModel(ModelPart root) {
        this.head     = root.getChild("head");
        this.body     = root.getChild("body");
        this.rightArm = root.getChild("right_arm");
        this.leftArm  = root.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg  = root.getChild("left_leg");
    }

    /**
     * Build the layer definition.
     *
     * <p>All pivots are at anatomically correct joint positions.
     * Cube offsets are relative to their parent pivot.
     *
     * <p>Coordinate convention (standard Minecraft entity model):
     * <ul>
     *   <li>+Y = down (feet at y=24, head at y≈0)</li>
     *   <li>+X = entity's left</li>
     *   <li>+Z = entity's back</li>
     * </ul>
     */
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ── HEAD ──────────────────────────────────────────────────────────
        // Pivot at neck base (y=0). Cube goes -8..0 above (standard head).
        // UV: texOffs(0, 0) from Blockbench export.
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // ── BODY ──────────────────────────────────────────────────────────
        // Pivot at top of torso (y=0). Torso cube 8×12×4 goes downward 0..12.
        // UV: texOffs(0, 16) — standard torso UV from Blockbench.
        // The collar/hip decoration piece (cube_r2) is kept as a sub-child
        // at the top of the torso so it moves with body rotations.
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 16)
                        .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        // Collar decoration — child of body, sits just below the neck.
        // Two mirrored collar cubes that sit at the top of the torso.
        // UV from Blockbench cube_r2: texOffs(40,15) and texOffs(30,36).
        // Cube_r2 had a 32.5° forward lean; we replicate just the collar
        // geometry as a static decoration sub-child (no lean needed here
        // because the cubes already face forward by default).
        body.addOrReplaceChild("collar",
                CubeListBuilder.create()
                        .texOffs(40, 15).addBox(-3.0F, 0.0F, -2.0F, 3.0F, 4.0F, 3.0F,
                                new CubeDeformation(0.0F))
                        .texOffs(30, 36).addBox(0.0F, 0.0F, -2.0F, 3.0F, 4.0F, 3.0F,
                                new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // ── RIGHT ARM ────────────────────────────────────────────────────
        // Pivot at right shoulder: (-5, 2, 0). Slim arm 3×11×4.
        // Cube starts at -1 above pivot (-2 for 1px shoulder cap), extends 11 down.
        // UV: texOffs(16, 36) from Blockbench.
        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(16, 36)
                        .addBox(-3.0F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        // ── LEFT ARM ─────────────────────────────────────────────────────
        // Pivot at left shoulder: (5, 2, 0). Mirrored slim arm.
        // UV: texOffs(32, 0) from Blockbench.
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(0.0F, -2.0F, -2.0F, 3.0F, 11.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(5.0F, 2.0F, 0.0F));

        // ── RIGHT LEG ────────────────────────────────────────────────────
        // Pivot at right hip: (-1.9, 12, 0). Long leg 4×16×4.
        // Cube starts at pivot and extends 16 units down to feet at y=28 → total 28 ≈ 1.75 blocks.
        // UV: texOffs(24, 16) from Blockbench.
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(24, 16)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(-1.9F, 12.0F, 0.0F));

        // ── LEFT LEG ─────────────────────────────────────────────────────
        // Pivot at left hip: (1.9, 12, 0). Mirrored long leg.
        // UV: texOffs(0, 32) from Blockbench.
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(0, 32)
                        .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 16.0F, 4.0F,
                                new CubeDeformation(0.0F)),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        // ── Head look ───────────────────────────────────────────────────
        this.head.yRot = netHeadYaw  * Mth.DEG_TO_RAD;
        this.head.xRot = headPitch   * Mth.DEG_TO_RAD;

        // ── Biped walk cycle ────────────────────────────────────────────
        // Only modify rotation angles — never touch x/y/z positions here.
        float swing = limbSwing * 0.6662F;
        float amp   = limbSwingAmount * 0.5F;

        // Arms counter-swing to opposite legs (standard biped gait)
        this.rightArm.xRot = Mth.cos(swing + Mth.PI) * 2.0F * amp;
        this.leftArm.xRot  = Mth.cos(swing)           * 2.0F * amp;

        // Legs swing with slightly higher amplitude for long-leg model
        this.rightLeg.xRot = Mth.cos(swing)           * 1.4F * limbSwingAmount;
        this.leftLeg.xRot  = Mth.cos(swing + Mth.PI)  * 1.4F * limbSwingAmount;

        // Reset rotations that we don't animate (yRot/zRot on limbs)
        this.rightArm.yRot = 0.0F;
        this.rightArm.zRot = 0.0F;
        this.leftArm.yRot  = 0.0F;
        this.leftArm.zRot  = 0.0F;
        this.rightLeg.yRot = 0.0F;
        this.rightLeg.zRot = 0.0F;
        this.leftLeg.yRot  = 0.0F;
        this.leftLeg.zRot  = 0.0F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
