package ca.lukegrahamlandry.basedefense;

import ca.lukegrahamlandry.basedefense.base.BaseDefense;
import ca.lukegrahamlandry.basedefense.base.attacks.old.AttackTargetAvatar;
import ca.lukegrahamlandry.basedefense.commands.AttackWaveArgumentType;
import ca.lukegrahamlandry.basedefense.game.ModRegistry;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenBaseUpgradeGui;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialShopGui;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialsGui;
import ca.lukegrahamlandry.lib.network.NetworkWrapper;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod(ModMain.MOD_ID)
public class ModMain {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MOD_ID = "basedefense";

    public ModMain() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(ModMain::mobAttributes);

        ModRegistry.init();
        BaseDefense.init();

        ArgumentTypeInfos.registerByClass(AttackWaveArgumentType.class, SingletonArgumentInfo.contextFree(AttackWaveArgumentType::new));
        GeckoLib.initialize();
    }

    public static void mobAttributes(EntityAttributeCreationEvent event){
        event.put(ModRegistry.ATTACK_TARGET.get(), AttackTargetAvatar.createLivingAttributes().build());
    }
}
