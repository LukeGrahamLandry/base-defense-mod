package ca.lukegrahamlandry.basedefense.material;

import net.minecraft.server.level.ServerPlayer;

public class MaterialStorageHandler {
    private static final MaterialCollection temp = new MaterialCollection();
    public static MaterialCollection get(ServerPlayer player){
        return temp; // TODO
    }
}
