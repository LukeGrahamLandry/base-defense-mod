package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.config.SyncedConfigData;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialsGui;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.lib.config.ConfigWrapper;
import ca.lukegrahamlandry.lib.keybind.KeybindWrapper;
import net.minecraft.server.level.ServerPlayer;

public class BaseDefense {
    public static final KeybindWrapper OPEN_MATERIALS_GUI =
            KeybindWrapper.of("materials_open", ModMain.MOD_ID, 77 /*M*/)
            .onPress((player -> {
                if (!player.level.isClientSide()){
                    new OpenMaterialsGui((ServerPlayer) player).sendToClient((ServerPlayer) player);
                }
            })).synced();

    public static final ConfigWrapper<SyncedConfigData> CONFIG = ConfigWrapper.synced(SyncedConfigData.class);

    public static void init() {
        JsonHelper.addTypeAdapter(MaterialCollection.class, new MaterialCollection.TypeAdapter());
        TeamManager.init();
        MaterialGeneratorType.init();
        BaseTier.init();
    }
}
