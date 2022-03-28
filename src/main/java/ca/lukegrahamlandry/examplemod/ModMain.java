package ca.lukegrahamlandry.examplemod;

import ca.lukegrahamlandry.examplemod.init.BlockInit;
import ca.lukegrahamlandry.examplemod.init.EntityInit;
import ca.lukegrahamlandry.examplemod.init.ItemInit;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.stream.Collectors;

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
