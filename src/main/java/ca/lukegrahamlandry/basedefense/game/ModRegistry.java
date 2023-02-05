package ca.lukegrahamlandry.basedefense.game;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.game.block.BaseBlock;
import ca.lukegrahamlandry.basedefense.game.block.MaterialGeneratorBlock;
import ca.lukegrahamlandry.basedefense.game.block.MaterialShopBlock;
import ca.lukegrahamlandry.basedefense.game.item.MaterialGeneratorPlacer;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.lib.registry.RegistryWrapper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistry {
    public static final RegistryWrapper<Block> BLOCKS = RegistryWrapper.create(BuiltInRegistries.BLOCK, ModMain.MOD_ID);

    public static final Supplier<Block> MATERIAL_GENERATOR_BLOCK = BLOCKS.register("material_generator",
            () -> new MaterialGeneratorBlock(Block.Properties.of(Material.STONE).strength(50.0F, 1200.0F).noOcclusion()));

    public static final Supplier<Block> MATERIAL_SHOP_BLOCK = BLOCKS.register("material_shop",
            () -> new MaterialShopBlock(Block.Properties.of(Material.STONE).strength(5.0F, 1200.0F).noOcclusion()));

    public static final Supplier<Block> BASE_BLOCK = BLOCKS.register("base_block",
            () -> new BaseBlock(Block.Properties.of(Material.STONE).strength(5.0F, 1200.0F).noOcclusion()));



    public static final RegistryWrapper<EntityType<?>> ENTITY = RegistryWrapper.create(BuiltInRegistries.ENTITY_TYPE, ModMain.MOD_ID);

    public static final Supplier<EntityType<AttackTargetAvatar>> ATTACK_TARGET = ENTITY.register("attack", () -> EntityType.Builder.of((EntityType.EntityFactory<AttackTargetAvatar>) AttackTargetAvatar::new, MobCategory.MISC).build("attack"));


    public static final RegistryWrapper<BlockEntityType<?>> TILE_ENTITY_TYPES = RegistryWrapper.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModMain.MOD_ID);

    public static final Supplier<BlockEntityType<MaterialGeneratorTile>> MATERIAL_GENERATOR_TILE = TILE_ENTITY_TYPES.register("material_generator",
            () -> BlockEntityType.Builder.of(MaterialGeneratorTile::new, MATERIAL_GENERATOR_BLOCK.get()).build(null));


    public static final RegistryWrapper<Item> ITEM = RegistryWrapper.create(BuiltInRegistries.ITEM, ModMain.MOD_ID);

    public static final Supplier<Item> GEN1 = ITEM.register("fruit_gen", () -> new MaterialGeneratorPlacer(new ResourceLocation(ModMain.MOD_ID, "fruit"), 0));
    public static final Supplier<Item> GEN2 = ITEM.register("metal_gen", () -> new MaterialGeneratorPlacer(new ResourceLocation(ModMain.MOD_ID, "metal"), 0));
    public static final Supplier<Item> MATERIAL_SHOP_ITEM = ITEM.register("material_shop", () -> new BlockItem(MATERIAL_SHOP_BLOCK.get(), new Item.Properties()));
    public static final Supplier<Item> BASE_BLOCK_ITEM = ITEM.register("base_block", () -> new BlockItem(BASE_BLOCK.get(), new Item.Properties()));

    @SubscribeEvent
    public static void creativeTab(CreativeModeTabEvent.Register event){
        event.registerCreativeModeTab(new ResourceLocation(ModMain.MOD_ID, "items"), builder -> {
            builder.title(Component.translatable("item_group." + ModMain.MOD_ID + ".items"))
                    .icon(() -> new ItemStack(Items.EMERALD))

                    .displayItems((enabledFlags, populator, hasPermissions) -> {
                        populator.accept(GEN1.get());
                        populator.accept(GEN2.get());
                        populator.accept(MATERIAL_SHOP_BLOCK.get());
                        populator.accept(BASE_BLOCK.get());
                    });
        });
    }

    // for classloading
    public static void init() {
    }
}
