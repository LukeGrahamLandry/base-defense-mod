package ca.lukegrahamlandry.basedefense.game;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.game.block.BaseBlock;
import ca.lukegrahamlandry.basedefense.game.block.MaterialGeneratorBlock;
import ca.lukegrahamlandry.basedefense.game.block.TurretBlock;
import ca.lukegrahamlandry.basedefense.game.item.LootedGeneratorPlacer;
import ca.lukegrahamlandry.basedefense.game.item.TurretPlacer;
import ca.lukegrahamlandry.basedefense.game.tile.BaseTile;
import ca.lukegrahamlandry.basedefense.game.tile.MaterialGeneratorTile;
import ca.lukegrahamlandry.basedefense.game.tile.TurretTile;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModRegistry {
    public static final RegistryWrapper<Block> BLOCKS = RegistryWrapper.create(BuiltInRegistries.BLOCK, ModMain.MOD_ID);

    public static final Supplier<Block> LOOTED_GENERATOR_BLOCK = BLOCKS.register("looted_generator",
            () -> new MaterialGeneratorBlock(false, Block.Properties.of(Material.STONE).strength(50.0F, 1200.0F).noOcclusion()));

    public static final Supplier<Block> TERRAIN_GENERATOR_BLOCK = BLOCKS.register("terrain_generator",
            () -> new MaterialGeneratorBlock(true, Block.Properties.copy(Blocks.BEDROCK).noOcclusion()));

    public static final Supplier<Block> BASE_BLOCK = BLOCKS.register("base_block",
            () -> new BaseBlock(Block.Properties.of(Material.STONE).strength(5.0F, 1200.0F).noOcclusion()));

    public static final Supplier<Block> TURRET_BLOCK = BLOCKS.register("turret",
            () -> new TurretBlock(Block.Properties.of(Material.STONE).strength(5.0F, 1200.0F).noOcclusion()));



    public static final RegistryWrapper<EntityType<?>> ENTITY = RegistryWrapper.create(BuiltInRegistries.ENTITY_TYPE, ModMain.MOD_ID);

    public static final Supplier<EntityType<AttackTargetAvatar>> ATTACK_TARGET = ENTITY.register("attack", () -> EntityType.Builder.of((EntityType.EntityFactory<AttackTargetAvatar>) AttackTargetAvatar::new, MobCategory.MISC).build("attack"));


    public static final RegistryWrapper<BlockEntityType<?>> TILE_ENTITY_TYPES = RegistryWrapper.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModMain.MOD_ID);

    public static final Supplier<BlockEntityType<MaterialGeneratorTile>> MATERIAL_GENERATOR_TILE = TILE_ENTITY_TYPES.register("material_generator",
            () -> BlockEntityType.Builder.of(MaterialGeneratorTile::new, LOOTED_GENERATOR_BLOCK.get(), TERRAIN_GENERATOR_BLOCK.get()).build(null));

    public static final Supplier<BlockEntityType<BaseTile>> BASE_TILE = TILE_ENTITY_TYPES.register("base_block",
            () -> BlockEntityType.Builder.of(BaseTile::new, BASE_BLOCK.get()).build(null));

    public static final Supplier<BlockEntityType<TurretTile>> TURRET_TILE = TILE_ENTITY_TYPES.register("turret",
            () -> BlockEntityType.Builder.of(TurretTile::new, TURRET_BLOCK.get()).build(null));


    public static final RegistryWrapper<Item> ITEM = RegistryWrapper.create(BuiltInRegistries.ITEM, ModMain.MOD_ID);

    public static final Supplier<Item> LOOTED_GENERATOR_ITEM = ITEM.register("looted_generator", LootedGeneratorPlacer::new);
    public static final Supplier<Item> BASE_BLOCK_ITEM = ITEM.register("base_block", () -> new BlockItem(BASE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<Item> TERRAIN_GEN = ITEM.register("terrain_generator", () -> new BlockItem(TERRAIN_GENERATOR_BLOCK.get(), new Item.Properties()));
    public static final Supplier<Item> TURRET_ITEM = ITEM.register("turret", TurretPlacer::new);

    @SubscribeEvent
    public static void creativeTab(CreativeModeTabEvent.Register event){
        event.registerCreativeModeTab(new ResourceLocation(ModMain.MOD_ID, "items"), builder -> {
            builder.title(Component.translatable("item_group." + ModMain.MOD_ID + ".items"))
                    .icon(() -> new ItemStack(Items.EMERALD))

                    .displayItems((enabledFlags, populator, hasPermissions) -> {
                        populator.accept(LOOTED_GENERATOR_BLOCK.get());
                        populator.accept(TERRAIN_GENERATOR_BLOCK.get());
                        populator.accept(BASE_BLOCK.get());
                        populator.accept(TURRET_ITEM.get());
                    });
        });
    }

    // for classloading
    public static void init() {
    }
}
