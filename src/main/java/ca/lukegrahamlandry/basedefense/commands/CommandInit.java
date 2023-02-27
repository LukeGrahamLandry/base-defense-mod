package ca.lukegrahamlandry.basedefense.commands;

import ca.lukegrahamlandry.basedefense.ModMain;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ModMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandInit {
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event){
        AttackWaveCommand.register(event.getDispatcher());
        BaseTeamCommand.register(event.getDispatcher());
    }
}