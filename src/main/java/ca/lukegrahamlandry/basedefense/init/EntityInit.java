package ca.lukegrahamlandry.basedefense.init;

import ca.lukegrahamlandry.basedefense.ModMain;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITY = DeferredRegister.create(ForgeRegistries.ENTITIES, ModMain.MOD_ID);

}
