package ca.lukegrahamlandry.basedefense;

import ca.lukegrahamlandry.basedefense.attacks.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.init.*;
import com.mojang.logging.LogUtils;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ModMain.MOD_ID)
public class ModMain {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "basedefense";

    public ModMain() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ItemInit.ITEM.register(modEventBus);
        BlockInit.BLOCKS.register(modEventBus);
        EntityInit.ENTITY.register(modEventBus);
        TileTypeInit.TILE_ENTITY_TYPES.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(ModMain::mobAttributes);
    }

    public static void mobAttributes(EntityAttributeCreationEvent event){
        event.put(EntityInit.ATTACK_TARGET.get(), AttackTargetAvatar.createLivingAttributes().build());
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkInit.registerPackets();
    }
}
