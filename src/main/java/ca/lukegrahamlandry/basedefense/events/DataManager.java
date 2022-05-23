package ca.lukegrahamlandry.basedefense.events;

import ca.lukegrahamlandry.basedefense.Config;
import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.material.MaterialGenerationHandler;
import ca.lukegrahamlandry.basedefense.material.MaterialGeneratorData;
import ca.lukegrahamlandry.basedefense.material.MaterialGeneratorDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Optional;
import java.util.Random;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DataManager {
    static MaterialGeneratorDataLoader gen;

    @SubscribeEvent
    public static void initCaps(AddReloadListenerEvent event){
        System.out.println("reload listener");

        MaterialGeneratorDataLoader optionsLoader = new MaterialGeneratorDataLoader();
        gen = optionsLoader;
        event.addListener(optionsLoader);

    }

    public static MaterialGeneratorData getMaterial(ResourceLocation type) {
        return gen.get(type);
    }
}
