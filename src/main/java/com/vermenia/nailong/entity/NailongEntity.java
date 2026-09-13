package com.vermenia.nailong.entity;

import com.vermenia.nailong.registry.NailongItems;
import com.vermenia.nailong.inventory.NailongMenu;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.event.EventHooks;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Apple-tamed companion with owner following and a short, non-destructive flame attack. */
public final class NailongEntity extends TamableAnimal implements GeoEntity, RangedAttackMob {
    public static final float MODEL_SCALE = 2.5F * 16.0F / 68.0F;
    private static final double MAX_HEALTH = 100.0D;
    private static final float FIRE_RANGE = 8.0F;
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.nailong.idle");
    private static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.nailong.walk");
    private static final RawAnimation FIRE = RawAnimation.begin().thenPlay("animation.nailong.fire");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private final SimpleContainer inventory = new SimpleContainer(9);
    private int autoEatCooldown = 0;

    public NailongEntity(EntityType<? extends NailongEntity> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.FOLLOW_RANGE, 12.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.15D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new FollowOwnerGoal(this, 1.15D, 6.0F, 3.0F));
        this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 20, FIRE_RANGE));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, LivingEntity.class,
                10, true, false, this::canAttack));
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.is(NailongItems.NAILONG_SPAWN_EGG.get())) {
            return InteractionResult.PASS;
        }
        if (heldItem.is(Items.APPLE)) {
            if (!this.level().isClientSide) {
                if (!this.isTame()) {
                    if (!EventHooks.onAnimalTame(this, player)) {
                        heldItem.consume(1, player);
                        this.tame(player);
                        this.navigation.stop();
                        this.setTarget(null);
                        this.setCustomNameVisible(true);
                        this.level().broadcastEntityEvent(this, (byte)7);
                        player.displayClientMessage(Component.literal("奶龙喜欢你的苹果，决定跟着你啦！"), true);
                    }
                } else if (this.getHealth() < this.getMaxHealth()) {
                    heldItem.consume(1, player);
                    this.heal(4.0F);
                    this.level().broadcastEntityEvent(this, (byte)7);
                    int current = (int)this.getHealth();
                    int max = (int)this.getMaxHealth();
                    player.displayClientMessage(Component.literal("奶龙吃掉了苹果，生命值：" + current + "/" + max), true);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        if (this.isTame() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return NailongEntity.this.getName();
                }

                @Override
                public AbstractContainerMenu createMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
                    return new NailongMenu(containerId, playerInventory, inventory, NailongEntity.this);
                }
            }, buf -> buf.writeVarInt(this.getId()));
            return InteractionResult.SUCCESS;
        }
        if (!this.level().isClientSide) {
            player.displayClientMessage(Component.literal("奶龙歪了歪头。在聊天里叫我的名字奶龙，我就能听见啦！"), true);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.isTame()) {
            if (this.autoEatCooldown > 0) {
                this.autoEatCooldown--;
            } else if (this.getHealth() < this.getMaxHealth() * 0.8F) {
                for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                    ItemStack stack = this.inventory.getItem(i);
                    if (stack.is(Items.APPLE)) {
                        stack.shrink(1);
                        this.heal(4.0F);
                        this.level().broadcastEntityEvent(this, (byte)7);
                        this.autoEatCooldown = 10;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        CompoundTag inventoryTag = new CompoundTag();
        for (int i = 0; i < this.inventory.getContainerSize(); i++) {
            ItemStack stack = this.inventory.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                stack.save(this.registryAccess(), itemTag);
                inventoryTag.put("Slot" + i, itemTag);
            }
        }
        tag.put("Inventory", inventoryTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Older saves persist the original 24-point base attribute. Preserve current health
        // and modifiers while upgrading that default; explicit custom base values are retained.
        var health = this.getAttribute(Attributes.MAX_HEALTH);
        if (health != null && health.getBaseValue() == 24.0D) {
            health.setBaseValue(MAX_HEALTH);
        }
        if (tag.contains("Inventory")) {
            CompoundTag inventoryTag = tag.getCompound("Inventory");
            for (int i = 0; i < this.inventory.getContainerSize(); i++) {
                if (inventoryTag.contains("Slot" + i)) {
                    ItemStack stack = ItemStack.parseOptional(this.registryAccess(), inventoryTag.getCompound("Slot" + i));
                    this.inventory.setItem(i, stack);
                }
            }
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        // Apples are handled above as taming/healing items, rather than breeding food.
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return this.isTame() && target instanceof Enemy && !this.isAlliedTo(target)
                && super.canAttack(target);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        if (!(this.level() instanceof ServerLevel server) || !target.isAlive()
                || !this.canAttack(target) || this.distanceToSqr(target) > FIRE_RANGE * FIRE_RANGE
                || !this.getSensing().hasLineOfSight(target)) return;
        Vec3 start = this.getEyePosition().add(0, -0.2D, 0);
        Vec3 end = target.getBoundingBox().getCenter();
        HitResult obstruction = server.clip(new ClipContext(start, end,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (obstruction.getType() != HitResult.Type.MISS) return;

        this.triggerAnim("attack", "fire");
        this.playSound(SoundEvents.BLAZE_SHOOT, 0.7F, 1.2F);
        Vec3 delta = end.subtract(start);
        int steps = Math.max(1, (int)Math.ceil(delta.length() * 5));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = start.add(delta.scale((double)i / steps));
            server.sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z,
                    2, 0.08D, 0.08D, 0.08D, 0.01D);
        }
        // Only the selected hostile receives damage; the flame does not create burning blocks.
        if (target.hurt(this.damageSources().mobAttack(this), 6.0F)) {
            target.igniteForSeconds(4.0F);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "locomotion", 5, state -> {
            boolean moving = state.isMoving() && this.onGround();
            return state.setAndContinue(moving ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "attack", 2,
                state -> software.bernie.geckolib.animation.PlayState.STOP)
                .triggerableAnim("fire", FIRE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ALLAY_AMBIENT_WITH_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ALLAY_DEATH;
    }
}
