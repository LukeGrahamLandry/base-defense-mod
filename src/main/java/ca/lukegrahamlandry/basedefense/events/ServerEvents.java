package ca.lukegrahamlandry.basedefense.events;

import ca.lukegrahamlandry.basedefense.Config;
import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.attacks.AttackTracker;
import ca.lukegrahamlandry.basedefense.init.BlockInit;
import ca.lukegrahamlandry.basedefense.material.MaterialGenerationHandler;
import ca.lukegrahamlandry.basedefense.material.TeamHandler;
import ca.lukegrahamlandry.basedefense.tile.MaterialGeneratorTile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    private static int timer = 0;
    private static boolean wasDay = false;
    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event){
        if (event.phase == TickEvent.Phase.START && !event.world.isClientSide() && event.world.dimension().equals(Level.OVERWORLD)){
            if (timer >= Config.getGenerationTimer()){
                MaterialGenerationHandler.get(event.world).distributeMaterials(event.world.getServer());
                timer = 0;

                handleAttacks(event.world);
            }
            timer++;
        }
    }

    private static void handleAttacks(Level overworld) {
        if (wasDay && !overworld.isDay()){
            System.out.println("start night");
            wasDay = false;
            AttackTracker.startAllAttacks(overworld);
        }
        if (!wasDay && overworld.isDay()){
            System.out.println("start day");
            wasDay = true;

        }
        AttackTracker.tick();
    }

    @SubscribeEvent
    public static void join(BlockEvent.BreakEvent event){
        if (!event.getWorld().isClientSide() && event.getState().getBlock() == BlockInit.MATERIAL_GENERATOR.get()){
            MaterialGeneratorTile.getAndDo((Level) event.getWorld(), event.getPos(), MaterialGeneratorTile::unBind);
        }
    }

}
