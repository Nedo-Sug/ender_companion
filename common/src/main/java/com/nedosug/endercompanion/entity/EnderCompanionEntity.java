package com.nedosug.endercompanion.entity;

import com.nedosug.endercompanion.EnderCompanionMod;
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

import javax.annotation.Nullable;
import java.util.UUID;

public class EnderCompanionEntity extends PathfinderMob {

    private static final String NBT_OWNER_UUID = "OwnerUUID";
    private static final String NBT_FRIENDSHIP_LEVEL = "FriendshipLevel";
    private static final String NBT_COMPANION_BACKPACK = "CompanionBackpack";

    private static final int DEFAULT_FRIENDSHIP = 20;
    private static final int MIN_FRIENDSHIP = 0;
    private static final int MAX_FRIENDSHIP = 100;
    private static final int HIGH_FRIENDSHIP_THRESHOLD = 70;

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
        int clamped = Math.max(MIN_FRIENDSHIP, Math.min(MAX_FRIENDSHIP, level));
        if (clamped != this.friendshipLevel) {
            EnderCompanionMod.LOGGER.info(
                    "[EnderCompanion-Mechanics] Отношения изменены: {} -> {} (из {})",
                    this.friendshipLevel, clamped, level);
        }
        this.friendshipLevel = clamped;
    }

    public void modifyFriendship(int amount) {
        EnderCompanionMod.LOGGER.info(
                "[EnderCompanion-Mechanics] modifyFriendship({}) вызван. Текущие отношения: {}",
                amount, this.friendshipLevel);
        this.setFriendshipLevel(this.friendshipLevel + amount);
    }

    public boolean isHighFriendshipUnlocked() {
        return this.friendshipLevel > HIGH_FRIENDSHIP_THRESHOLD;
    }

    public void tryDropGift() {
        if (!this.isHighFriendshipUnlocked()) {
            EnderCompanionMod.LOGGER.info(
                    "[EnderCompanion-Mechanics] tryDropGift: уровень дружбы {} ниже порога {} — подарок не выпадает.",
                    this.friendshipLevel, HIGH_FRIENDSHIP_THRESHOLD);
            return;
        }

        if (this.random.nextFloat() >= 0.10f) {
            EnderCompanionMod.LOGGER.info(
                    "[EnderCompanion-Mechanics] tryDropGift: бросок не прошёл (шанс 10%) — подарка нет.");
            return;
        }

        EnderCompanionMod.LOGGER.info("[EnderCompanion-Mechanics] tryDropGift: бросок прошёл — выдаём подарок!");

        ItemStack giftStack;

        if (this.random.nextFloat() < 0.30f) {
            int rareChoice = this.random.nextInt(3);
            switch (rareChoice) {
                case 0 -> giftStack = new ItemStack(Items.PHANTOM_MEMBRANE, 1);
                case 1 -> giftStack = new ItemStack(Items.CRYING_OBSIDIAN, 1);
                default -> giftStack = new ItemStack(Items.SHULKER_SHELL, 1);
            }
        } else {
            int commonChoice = this.random.nextInt(3);
            switch (commonChoice) {
                case 0 -> giftStack = new ItemStack(Items.ENDER_PEARL, 1 + this.random.nextInt(2));
                case 1 -> giftStack = new ItemStack(Items.CHORUS_FRUIT, 2 + this.random.nextInt(2));
                default -> giftStack = new ItemStack(Items.OBSIDIAN, 1);
            }
        }

        this.spawnAtLocation(giftStack);
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);

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
                        1, 0.0D, 0.0D, 0.0D, 0.0D
                );
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        String attackerName = source.getEntity() != null
                ? source.getEntity().getName().getString()
                : "<нет источника>";
        EnderCompanionMod.LOGGER.info(
                "[EnderCompanion-Mechanics] Сущность получила урон от: {} (урон: {})", attackerName, amount);

        if (source.getEntity() instanceof Player player && player.getUUID().equals(this.ownerUUID)) {
            int before = this.friendshipLevel;
            EnderCompanionMod.LOGGER.info(
                    "[EnderCompanion-Mechanics] Ударил владелец. Текущие отношения ДО удара: {}", before);
            this.modifyFriendship(-10);
            EnderCompanionMod.LOGGER.info(
                    "[EnderCompanion-Mechanics] Отношения УМЕНЬШИЛИСЬ на 10. Стало: {}", this.friendshipLevel);
            this.playSound(SoundEvents.ENDERMAN_SCREAM, 0.5F, 1.2F);
        } else {
            EnderCompanionMod.LOGGER.info(
                    "[EnderCompanion-Mechanics] Атакующий не является владельцем — отношения не меняются.");
        }
        return super.hurt(source, amount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        EnderCompanionMod.LOGGER.info(
                "[EnderCompanion-Mechanics] Взаимодействие от {} рукой {} с предметом {} (сторона: {})",
                player.getName().getString(), hand, itemStack, this.level().isClientSide ? "клиент" : "сервер");

        if (dev.architectury.platform.Platform.isModLoaded("travelersbackpack")) {
            if (!this.hasBackpack() && isTravelersBackpack(itemStack)) {
                if (!this.level().isClientSide) {
                    ItemStack backpackCopy = itemStack.copy();
                    backpackCopy.setCount(1);
                    this.setBackpackStack(backpackCopy);

                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }

                    this.modifyFriendship(10);
                    this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 1.0F, 1.0F);

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
                                    1, 0.0D, 0.0D, 0.0D, 0.0D
                            );
                        }
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.hasBackpack() && itemStack.is(Items.SHEARS)) {
                if (!this.level().isClientSide) {
                    this.spawnAtLocation(this.backpackStack);
                    this.clearBackpack();
                    this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }

            if (this.hasBackpack() && itemStack.isEmpty() && player.isShiftKeyDown()) {
                if (!this.level().isClientSide) {
                    openBackpackInventory(player);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
        }

        int friendshipGain = 0;
        boolean isValidGift = false;

        if (itemStack.is(Items.CHORUS_FRUIT) || itemStack.is(Items.ENDER_PEARL)) {
            friendshipGain = 5;
            isValidGift = true;
        } else if (itemStack.is(net.minecraft.tags.ItemTags.SMALL_FLOWERS) ||
                   itemStack.is(net.minecraft.tags.ItemTags.TALL_FLOWERS)) {
            friendshipGain = 3;
            isValidGift = true;
        } else if (itemStack.is(Items.COOKIE) || itemStack.is(Items.SWEET_BERRIES) ||
                   itemStack.is(Items.GLOW_BERRIES) || itemStack.is(Items.MELON_SLICE)) {
            friendshipGain = 4;
            isValidGift = true;
        } else if (itemStack.is(Items.DIAMOND) || itemStack.is(Items.EMERALD)) {
            friendshipGain = 8;
            isValidGift = true;
        }

        EnderCompanionMod.LOGGER.info(
                "[EnderCompanion-Mechanics] Проверка подарка: предмет={}, подходит={}, прибавка к отношениям={}",
                itemStack.getItem(), isValidGift, friendshipGain);

        if (isValidGift) {
            if (!this.level().isClientSide) {
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }

                EnderCompanionMod.LOGGER.info(
                        "[EnderCompanion-Mechanics] Принят подарок {} от {}. Отношения ДО: {}",
                        itemStack.getItem(), player.getName().getString(), this.friendshipLevel);
                this.modifyFriendship(friendshipGain);

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
                                1, 0.0D, 0.0D, 0.0D, 0.0D
                        );
                    }
                }

                this.playSound(SoundEvents.PLAYER_LEVELUP, 0.5F, 1.5F);
                this.tryDropGift();
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    private boolean isTravelersBackpack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                .getKey(stack.getItem()).toString();
        return itemId.startsWith("travelersbackpack:");
    }

    private void openBackpackInventory(Player player) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }

        try {
            Class<?> menuProviderClass = Class.forName(
                    "com.tiviacz.travelersbackpack.inventory.TravelersBackpackMenuProvider");
            Object menuProvider = menuProviderClass
                    .getConstructor(ItemStack.class, int.class)
                    .newInstance(this.backpackStack, -1);
            serverPlayer.openMenu((net.minecraft.world.MenuProvider) menuProvider);
            this.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.7F, 1.0F);
            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.info(
                    "Opened Traveler's Backpack menu for player {}", serverPlayer.getName().getString());
        } catch (ClassNotFoundException e) {
            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.warn(
                    "Could not find Traveler's Backpack menu classes. The mod version might be incompatible.");
            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("chat.endercompanion.backpack.incompatible"));
        } catch (NoSuchMethodException e) {
            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.warn(
                    "Could not find Traveler's Backpack menu constructor. API might have changed.");
            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("chat.endercompanion.backpack.method_not_found"));
        } catch (Exception e) {
            com.nedosug.endercompanion.EnderCompanionMod.LOGGER.error(
                    "Error opening Traveler's Backpack menu via reflection", e);
            serverPlayer.sendSystemMessage(
                    net.minecraft.network.chat.Component.translatable("chat.endercompanion.backpack.error"));
        }
    }
}
