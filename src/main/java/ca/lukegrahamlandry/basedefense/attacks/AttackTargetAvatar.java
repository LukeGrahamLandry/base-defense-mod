package ca.lukegrahamlandry.basedefense.attacks;

import ca.lukegrahamlandry.basedefense.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class AttackTargetAvatar extends LivingEntity {
    private AttackTargetable represents;

    public AttackTargetAvatar(EntityType<AttackTargetAvatar> p_20966_, Level p_20967_) {
            super(p_20966_, p_20967_);
    }

    public AttackTargetAvatar(Level level, AttackTargetable represents, BlockPos pos) {
        super(EntityInit.ATTACK_TARGET.get(), level);
        this.represents = represents;
        this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("thehealth", this.represents.maxHealth() - 1, AttributeModifier.Operation.ADDITION));
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static AttributeSupplier.Builder createLivingAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 1);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        boolean result = this.represents.damage(pSource, pAmount);
        if (result) super.hurt(pSource, pAmount);
        return result;
    }

    @Override
    public float getHealth() {
        return this.represents.health();
    }


    ////

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Arrays.asList(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
