package ca.lukegrahamlandry.basedefense.init;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.item.MaterialGeneratorPlacer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemInit {
    public static final DeferredRegister<Item> ITEM = DeferredRegister.create(ForgeRegistries.ITEMS, ModMain.MOD_ID);

    public static final RegistryObject<Item> GEN1 = ITEM.register("fruit_gen", () -> new MaterialGeneratorPlacer(new ResourceLocation(ModMain.MOD_ID, "fruit"), 0));
    public static final RegistryObject<Item> GEN2 = ITEM.register("metal_gen", () -> new MaterialGeneratorPlacer(new ResourceLocation(ModMain.MOD_ID, "metal"), 0));

    @SubscribeEvent
    public static void creativeTab(CreativeModeTabEvent.Register event){
        event.registerCreativeModeTab(new ResourceLocation(ModMain.MOD_ID, "items"), builder -> {
            builder.title(Component.translatable("item_group." + ModMain.MOD_ID + ".items"))
                    .icon(() -> new ItemStack(Items.EMERALD))

                    .displayItems((enabledFlags, populator, hasPermissions) -> {
                        populator.accept(GEN1.get());
                        populator.accept(GEN2.get());
                    });
        });
    }
}
