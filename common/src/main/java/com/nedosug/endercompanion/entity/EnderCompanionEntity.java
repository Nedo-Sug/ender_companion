package com.nedosug.endercompanion.entity;

import com.nedosug.endercompanion.entity.ai.FollowPlayerGoal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class EnderCompanionEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    private static final String NBT_OWNER_UUID = "OwnerUUID";
    private static final String NBT_FRIENDSHIP_LEVEL = "FriendshipLevel";
    private static final String NBT_COMPANION_BACKPACK = "CompanionBackpack";

    private static final int DEFAULT_FRIENDSHIP = 20;
    private static final int MIN_FRIENDSHIP = 0;
    private static final int MAX_FRIENDSHIP = 100;
    private static final int HIGH_FRIENDSHIP_THRESHOLD = 70;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private UUID ownerUUID;
    private int friendshipLevel = DEFAULT_FRIENDSHIP;
    private ItemStack backpackStack = ItemStack.EMPTY;

    public EnderCompanionEntity(EntityType<? extends EnderCompanionEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowPlayerGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
    }

    private <E extends EnderCompanionEntity> PlayState movementPredicate(AnimationState<E> state) {
        if (state.isMoving()) {
            return state.setAndContinue(WALK_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setOwnerUUID(@Nullable UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Nullable
    public Player getOwner() {
        if (this.ownerUUID == null) {
            return null;
        }
        return this.level().getPlayerByUUID(this.ownerUUID);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.ownerUUID != null) {
            tag.putUUID(NBT_OWNER_UUID, this.ownerUUID);
        }
        tag.putInt(NBT_FRIENDSHIP_LEVEL, this.friendshipLevel);

        if (!this.backpackStack.isEmpty()) {
            CompoundTag backpackTag = new CompoundTag();
            this.backpackStack.save(backpackTag);
            tag.put(NBT_COMPANION_BACKPACK, backpackTag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(NBT_OWNER_UUID)) {
            this.ownerUUID = tag.getUUID(NBT_OWNER_UUID);
        }
        if (tag.contains(NBT_FRIENDSHIP_LEVEL)) {
            this.friendshipLevel = tag.getInt(NBT_FRIENDSHIP_LEVEL);
        } else {
            this.friendshipLevel = DEFAULT_FRIENDSHIP;
        }

        if (tag.contains(NBT_COMPANION_BACKPACK)) {
            this.backpackStack = ItemStack.of(tag.getCompound(NBT_COMPANION_BACKPACK));
        } else {
            this.backpackStack = ItemStack.EMPTY;
        }
    }

    public boolean hasBackpack() {
        return !this.backpackStack.isEmpty();
    }

    public ItemStack getBackpackStack() {
        return this.backpackStack;
    }

    public void setBackpackStack(ItemStack stack) {
        this.backpackStack = stack.copy();
    }

    public void clearBackpack() {
        this.backpackStack = ItemStack.EMPTY;
    }

    public int getFriendshipLevel() {
        return this.friendshipLevel;
    }

    public void setFriendshipLevel(int level) {
        this.friendshipLevel = Math.max(MIN_FRIENDSHIP, Math.min(MAX_FRIENDSHIP, level));
    }

    public void modifyFriendship(int amount) {
        this.setFriendshipLevel(this.friendshipLevel + amount);
    }

    public boolean isHighFriendshipUnlocked() {
        return this.friendshipLevel > HIGH_FRIENDSHIP_THRESHOLD;
    }

    public void tryDropGift() {
        // Only works when high friendship is unlocked
        if (!this.isHighFriendshipUnlocked()) {
            return;
        }

        // 10% chance to drop a gift
        if (this.random.nextFloat() >= 0.10f) {
            return;
        }

        ItemStack giftStack;

        // 30% chance for rare items, 70% for common
        if (this.random.nextFloat() < 0.30f) {
            // Rare items pool
            int rareChoice = this.random.nextInt(3);
            switch (rareChoice) {
                case 0 -> giftStack = new ItemStack(Items.PHANTOM_MEMBRANE, 1);
                case 1 -> giftStack = new ItemStack(Items.CRYING_OBSIDIAN, 1);
                default -> giftStack = new ItemStack(Items.SHULKER_SHELL, 1);
            }
        } else {
            // Common items pool
            int commonChoice = this.random.nextInt(3);
            switch (commonChoice) {
                case 0 -> giftStack = new ItemStack(Items.ENDER_PEARL, 1 + this.random.nextInt(2)); // 1-2
                case 1 -> giftStack = new ItemStack(Items.CHORUS_FRUIT, 2 + this.random.nextInt(2)); // 2-3
                default -> giftStack = new ItemStack(Items.OBSIDIAN, 1);
            }
        }

        // Drop the gift
        this.spawnAtLocation(giftStack);

        // Play teleportation sound
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

        // Spawn portal particles (as if she brought it from the End)
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double offsetX = (this.random.nextDouble() - 0.5D) * 1.5D;
                double offsetY = this.random.nextDouble() * 1.5D;
                double offsetZ = (this.random.nextDouble() - 0.5D) * 1.5D;
                serverLevel.sendParticles(
                        ParticleTypes.PORTAL,
                        this.getX() + offsetX,
                        this.getY() + offsetY + 0.5D,
                        this.getZ() + offsetZ,
                        1,
                        0.0D, 0.0D, 0.0D,
                        0.0D
                );
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Check if damage is from owner
        if (source.getEntity() instanceof Player player && player.getUUID().equals(this.ownerUUID)) {
            this.modifyFriendship(-10);
            this.playSound(SoundEvents.ENDERMAN_SCREAM, 0.5F, 1.2F);
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // Traveler's Backpack integration (soft dependency)
        if (dev.architectury.platform.Platform.isModLoaded("travelersbackpack")) {
            // Check if player is trying to equip a backpack
            if (!this.hasBackpack() && isTravelersBackpack(itemStack)) {
                if (!this.level().isClientSide) {
                    // Take backpack from player
                    ItemStack backpackCopy = itemStack.copy();
                    backpackCopy.setCount(1);
                    this.setBackpackStack(backpackCopy);

                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                    // Increase friendship
                    this.modifyFriendship(10);

                    // Play equip sound
                    this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

                    // Spawn happy particles
                    if (this.level() instanceof ServerLevel serverLevel) {
                        for (int i = 0; i < 5; i++) {
                            double offsetX = (this.random.nextDouble() - 0.5D) * 0.5D;
                            double offsetY = this.random.nextDouble() * 0.5D + 0.5D;
                            double offsetZ = (this.random.nextDouble() - 0.5D) * 0.5D;
                            serverLevel.sendParticles(
                                    ParticleTypes.HEART,
                                    this.getX() + offsetX,
                                    this.getY() + offsetY + 1.0D,
                                    this.getZ() + offsetZ,
                                    1,
                                    0.0D, 0.0D, 0.0D,
                                    0.0D
                            );
                        }
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Check if player is trying to remove backpack with shears
            if (this.hasBackpack() && itemStack.is(Items.SHEARS)) {
                if (!this.level().isClientSide) {
                    // Drop backpack
                    this.spawnAtLocation(this.backpackStack);
                    this.clearBackpack();

                    // Play sound
                    this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            // Check if player is trying to open backpack (shift + empty hand)
            if (this.hasBackpack() && itemStack.isEmpty() && player.isShiftKeyDown()) {
                if (!this.level().isClientSide) {
                    // Try to open backpack inventory
                    openBackpackInventory(player);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        // Regular gift system
        int friendshipGain = 0;
        boolean isValidGift = false;

        // Check gift categories
        if (itemStack.is(Items.CHORUS_FRUIT) || itemStack.is(Items.ENDER_PEARL)) {
            // Native End items
            friendshipGain = 5;
            isValidGift = true;
        } else if (itemStack.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS) ||
                   itemStack.is(net.minecraft.tags.ItemTags.TALL_FLOWERS)) {
            // Flowers
            friendshipGain = 3;
            isValidGift = true;
        } else if (itemStack.is(Items.COOKIE) || itemStack.is(Items.SWEET_BERRIES) ||
                   itemStack.is(Items.GLOW_BERRIES) || itemStack.is(Items.MELON_SLICE)) {
            // Sweets
            friendshipGain = 4;
            isValidGift = true;
        } else if (itemStack.is(Items.DIAMOND) || itemStack.is(Items.EMERALD)) {
            // Precious gems
            friendshipGain = 8;
            isValidGift = true;
        }

        if (isValidGift) {
            if (!this.level().isClientSide) {
                // Consume item
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }

                // Increase friendship
                this.modifyFriendship(friendshipGain);

                // Spawn heart particles
                if (this.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 7; i++) {
                        double offsetX = (this.random.nextDouble() - 0.5D) * 0.5D;
                        double offsetY = this.random.nextDouble() * 0.5D + 0.5D;
                        double offsetZ = (this.random.nextDouble() - 0.5D) * 0.5D;
                        serverLevel.sendParticles(
                                ParticleTypes.HEART,
                                this.getX() + offsetX,
                                this.getY() + offsetY + 1.0D,
                                this.getZ() + offsetZ,
                                1,
                                0.0D, 0.0D, 0.0D,
                                0.0D
                        );
                    }
                }

                // Play happy sound
                this.playSound(SoundEvents.PLAYER_LEVELUP, 0.5F, 1.5F);

                // Try to give a gift back if high friendship
                this.tryDropGift();
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    /**
     * Safe check if item is a Traveler's Backpack without hard dependency.
     * Uses string comparison to avoid NoClassDefFoundError.
     */
    private boolean isTravelersBackpack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(stack.getItem()).toString();

        return itemId.startsWith("travelersbackpack:");
    }

    /**
     * Opens the backpack inventory for the player.
     * Uses reflection to avoid hard dependency on Traveler's Backpack classes.
     */
    private void openBackpackInventory(Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }

        try {
            // Try to find Traveler's Backpack screen opening method via reflection
            // Typical path: com.tiviacz.travelersbackpack.inventory.menu.TravelersBackpackMenu

            Class<?> menuClass = Class.forName("com.tiviacz.travelersbackpack.inventory.menu.TravelersBackpackMenu");
            Class<?> menuProviderClass = Class.forName("com.tiviacz.travelersbackpack.inventory.TravelersBackpackMenuProvider");

            // Create menu provider for the backpack ItemStack
            // Constructor: TravelersBackpackMenuProvider(ItemStack backpack, int slotIndex)
            Object menuProvider = menuProviderClass.getConstructor(ItemStack.class, int.class)
                    .newInstance(this.backpackStack, -1); // -1 means not in player inventory

            // Open menu for player
            // ServerPlayer.openMenu(MenuProvider)
            serverPlayer.openMenu((net.minecraft.world.MenuProvider) menuProvider);

            // Play open sound
            this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.7F, 1.0F);

            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.info(
                    "Opened Traveler's Backpack menu for player {}", serverPlayer.getName().getString());

        } catch (ClassNotFoundException e) {
            // Traveler's Backpack classes not found - mod might be outdated or API changed
            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.warn(
                    "Could not find Traveler's Backpack menu classes. The mod version might be incompatible.");

            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("chat.endercompanion.backpack.incompatible")
            );

        } catch (NoSuchMethodException e) {
            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.warn(
                    "Could not find Traveler's Backpack menu constructor. API might have changed.");

            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("chat.endercompanion.backpack.method_not_found")
            );

        } catch (Exception e) {
            // Generic error during reflection - log and continue without crashing
            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.error(
                    "Error opening Traveler's Backpack menu via reflection", e);

            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("chat.endercompanion.backpack.error")
            );
        }
    }
}
