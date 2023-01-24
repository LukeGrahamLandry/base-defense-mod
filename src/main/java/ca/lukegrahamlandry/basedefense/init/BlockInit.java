package ca.lukegrahamlandry.basedefense.init;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.block.MaterialGeneratorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockInit {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ModMain.MOD_ID);

    public static final RegistryObject<Block> MATERIAL_GENERATOR = BLOCKS.register("material_generator",
            () -> new MaterialGeneratorBlock(Block.Properties.of(Material.STONE).strength(50.0F, 1200.0F).noOcclusion()));
}
