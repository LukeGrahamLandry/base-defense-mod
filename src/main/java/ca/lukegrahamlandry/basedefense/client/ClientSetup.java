package ca.lukegrahamlandry.basedefense.client;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.client.renderer.BaseBlockRenderer;
import ca.lukegrahamlandry.basedefense.client.renderer.GeneratorRenderer;
import ca.lukegrahamlandry.basedefense.client.renderer.TurretRenderer;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.game.tile.TurretTile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistry.MATERIAL_GENERATOR_TILE.get(), GeneratorRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.BASE_TILE.get(), BaseBlockRenderer::new);
        event.registerBlockEntityRenderer(ModRegistry.TURRET_TILE.get(), TurretRenderer::new);
    }
}
