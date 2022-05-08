package ca.lukegrahamlandry.basedefense;

import ca.lukegrahamlandry.basedefense.init.BlockInit;
import ca.lukegrahamlandry.basedefense.init.EntityInit;
import ca.lukegrahamlandry.basedefense.init.ItemInit;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ModMain.MOD_ID)
public class ModMain {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "examplemod";

    public ModMain() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ItemInit.ITEM.register(modEventBus);
        BlockInit.BLOCK.register(modEventBus);
        EntityInit.ENTITY.register(modEventBus);
    }
}
