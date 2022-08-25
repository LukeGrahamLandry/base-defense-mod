package ca.lukegrahamlandry.basedefense.init;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.item.MaterialGeneratorPlacer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEM = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);


    public static final RegistryObject<Item> GEN1 = ITEM.register("fruit_gen", () -> new MaterialGeneratorPlacer(new ResourceLocation(ModMain.MOD_ID, "fruit"), 0));
    public static final RegistryObject<Item> GEN2 = ITEM.register("metal_gen", () -> new MaterialGeneratorPlacer(new ResourceLocation(ModMain.MOD_ID, "metal"), 0));


    public static Item.Properties props(){
        return new Item.Properties().tab(ModCreativeTab.instance);
    }

    public static class ModCreativeTab extends CreativeModeTab {
        public static final ModCreativeTab instance = new ModCreativeTab(CreativeModeTab.TABS.length, ModMain.MOD_ID);
        private ModCreativeTab(int index, String label) {
            super(index, label);
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.EMERALD);
        }
    }
}
