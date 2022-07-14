package ca.lukegrahamlandry.basedefense.init;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.attacks.AttackTargetAvatar;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY = DeferredRegister.create(ForgeRegistries.ENTITIES, ModMain.MOD_ID);

    public static final RegistryObject<EntityType<AttackTargetAvatar>> ATTACK_TARGET = ENTITY.register("attack", () -> EntityType.Builder.of((EntityType.EntityFactory<AttackTargetAvatar>) AttackTargetAvatar::new, MobCategory.MISC).build("attack"));
 }
