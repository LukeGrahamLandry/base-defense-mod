package ca.lukegrahamlandry.basedefense.init;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.block.MaterialGeneratorBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModMain.MOD_ID);

    public static final RegistryObject<Block> MATERIAL_GENERATOR = BLOCKS.register("material_generator",
            () -> new MaterialGeneratorBlock(Block.Properties.of(Material.STONE).strength(50.0F, 1200.0F).noOcclusion()));



    @Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus= Mod.EventBusSubscriber.Bus.MOD)
    public static class EventListener {
        @SubscribeEvent
        public static void generateBlockItems(final RegistryEvent.Register<Item> event) {
            final IForgeRegistry<Item> registry = event.getRegistry();
            BLOCKS.getEntries().stream().filter(block -> !(block.get() instanceof LiquidBlock)).map(RegistryObject::get).forEach(block -> {
                final BlockItem blockItem = new BlockItem(block,  ItemInit.props());
                blockItem.setRegistryName(block.getRegistryName());
                registry.register(blockItem);
            });
            ModMain.LOGGER.debug("Registered BlockItems");
        }
    }

}
