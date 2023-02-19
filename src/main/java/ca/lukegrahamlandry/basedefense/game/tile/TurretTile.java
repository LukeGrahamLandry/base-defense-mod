package ca.lukegrahamlandry.basedefense.game.tile;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.network.clientbound.ClientPacketHandlers;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.lib.network.ClientSideHandler;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class TurretTile extends BlockEntity implements GeoBlockEntity {
    public TurretTile(BlockPos pPos, BlockState pBlockState) {
        super(ModRegistry.TURRET_TILE.get(), pPos, pBlockState);
    }

    ///// Shooting /////

    long nextShot = 0;
    static final Vec3 rotZero = new Vec3(0, 0, 1);
    LivingEntity target = null;
    public void serverTick() {
        Vec3 bulletSource = new Vec3(this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1.5, this.getBlockPos().getZ() + 0.5);

        if (level.getGameTime() > nextShot){
            nextShot = level.getGameTime() + getStats().shotDelayTicks;

            AABB box = new AABB(this.getBlockPos()).inflate(getStats().rangeInBlocks);
            List<LivingEntity> possibleTargets = level.getEntitiesOfClass(LivingEntity.class, box, (e) -> {
                if (!(e instanceof Enemy)) return false;

                // Check if it has line of sight
                Vec3 targetCenter = e.getBoundingBox().getCenter();
                HitResult ray = this.level.clip(new ClipContext(bulletSource, targetCenter, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
                return ray.getType() != HitResult.Type.BLOCK;
            });

            target = level.getNearestEntity(possibleTargets, TargetingConditions.DEFAULT, null, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
            if (target == null || !target.isAlive()){
                syncIsShooting(false, 0);  // TODO: default facing based on how you placed it
                return;
            }

            target.hurt(DamageSource.GENERIC, getStats().damage);
            for (MobEffectInstance effect : getStats().potionEffects){
                target.addEffect(effect);
            }
            if (getStats().flameSeconds > 0) target.setSecondsOnFire(getStats().flameSeconds);
        }

        // Rotation
        if (target != null && target.isAlive()){
            Vec3 lookAtTarget = bulletSource.subtract(target.getBoundingBox().getCenter()).normalize();
            Vec3 hLookAtTarget = lookAtTarget.subtract(0, lookAtTarget.y, 0);
            double dot = hLookAtTarget.dot(rotZero);
            float dir = (float) Math.toDegrees(Math.acos(dot));
            if (hLookAtTarget.x < 0){
                dir = 360 - dir;
            }
            syncIsShooting(true, dir);
        }
    }

    ///// Data Save & Sync /////

    public static class Stats {
        int damage = 2;
        int rangeInBlocks = 5;
        int shotDelayTicks = 20;
        int flameSeconds = 0;
        List<MobEffectInstance> potionEffects = new ArrayList<>();
        String color = "none";
        float rotationDegreesPerTick = 10F;
    }

    public static class Data {
        Stats stats = new Stats();

        // Saving this does nothing. On the server it does nothing because it recalculates targets every time anyway.
        // It gets synced to the client and put here. Used for deciding which animation to play.
        public boolean isShooting = false;
        public float hRotCurrent = 0;
        public float hRotTarget = 0;
    }

    public Data data = new Data();

    private Stats getStats(){
        return this.data.stats;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putString("wlData", JsonHelper.get().toJson(this.data));
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        safeLoadData(pTag);
    }

    @Override  // TODO: only done automatically on chunk update so have to send it myself when they upgrade
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putString("wlData", JsonHelper.get().toJson(this.data));
        return tag;
    }

    private void safeLoadData(CompoundTag pTag){
        if (pTag.contains("wlData")){
            try {
                this.data = JsonHelper.get().fromJson(pTag.getString("wlData"), Data.class);
            } catch (JsonSyntaxException e){
                ModMain.LOGGER.error("Failed to load TurretTile data.");
                ModMain.LOGGER.error(pTag.getString("wlData"));
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        safeLoadData(tag);
        updateTexture();
        this.hRotCurrent = this.data.hRotCurrent;
    }

    public static class AnimUpdate implements ClientSideHandler {
        public float hRotTarget;
        public boolean isShooting;
        public BlockPos pos;

        AnimUpdate(BlockPos pos, boolean isShooting, float hRotTarget) {
            this.isShooting = isShooting;
            this.pos = pos;
            this.hRotTarget = hRotTarget;
        }

        @Override
        public void handle() {
            ClientPacketHandlers.updateTurret(this);
        }
    }

    private void syncIsShooting(boolean b, float hRotTarget) {
        if (this.data.isShooting != b || !Mth.equal(hRotTarget, this.data.hRotTarget)) {
            new AnimUpdate(this.getBlockPos(), b, hRotTarget).sendToTrackingClients(this);
        }
        this.data.isShooting = b;
        this.data.hRotTarget = hRotTarget;
    }

    ///// Animation //////

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation SHOOTING_ANIM = RawAnimation.begin().thenPlay("shooting");
    private static final RawAnimation STOPPING_ANIM = RawAnimation.begin().thenPlay("stopping");
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenPlay("idle");

    private PlayState animation(AnimationState<TurretTile> state){
        if (data.isShooting){
            state.setAnimation(SHOOTING_ANIM);
        } else {
            state.setAnimation(this.animTick > 0 ? STOPPING_ANIM : IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "shoot", this::animation));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    boolean wasShooting = false;
    int animTick = 0;
    public void tick() {
        if (!this.hasLevel()) return;  // Can't happen but im afraid

        if (this.level.isClientSide()){
            if (animTick > 0) animTick--;
            if (!data.isShooting && wasShooting){
                animTick = 40;  // length of <stopping> from turret.animation.json
            }
            wasShooting = data.isShooting;
            hRotLast = hRotCurrent;
            if (hRotLast > 360) hRotLast = hRotLast - 360;
            if (hRotCurrent > 360) hRotCurrent = hRotCurrent - 360;
            if (hRotLast < 0) hRotLast = hRotLast + 360;
            if (hRotCurrent < 0) hRotCurrent = hRotCurrent + 360;
        } else {
            this.serverTick();
        }

        float dif = this.data.hRotTarget - hRotCurrent;
        if (dif > 180) dif = dif - 360;
        if (dif < -180) dif = dif + 360;
        if (Mth.abs(dif) < this.getStats().rotationDegreesPerTick){
            hRotCurrent = this.data.hRotTarget;
        } else {
            hRotCurrent += this.getStats().rotationDegreesPerTick * Mth.sign(dif);
        }
    }

    float hRotLast = 0;
    float hRotCurrent = 0;
    public float hRotDeg(float partialTick) {
        return Mth.lerp(partialTick, hRotLast, hRotCurrent);
    }

    ///// Texture Variants /////

    private ResourceLocation texture = TEXTURE;
    private void updateTexture(){
        texture = COLOR_TEXTURES.getOrDefault(this.data.stats.color, TEXTURE);
    }
    private static final ResourceLocation TEXTURE = new ResourceLocation(ModMain.MOD_ID, "textures/turret/turret_placed.png");
    private static final Map<String, ResourceLocation> COLOR_TEXTURES = new HashMap<>();
    static {
        for (DyeColor color : DyeColor.values()){
            COLOR_TEXTURES.put(color.getName().replace("_", ""), new ResourceLocation(ModMain.MOD_ID, "textures/turret/turret_placed_" + color.getName().replace("_", "") + ".png"));
        }
    }
    public ResourceLocation getTexture() {
        return this.texture;
    }
}
