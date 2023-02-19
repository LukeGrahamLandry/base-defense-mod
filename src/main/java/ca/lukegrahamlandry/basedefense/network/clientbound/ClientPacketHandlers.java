package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.client.gui.BaseUpgradeScreen;
import ca.lukegrahamlandry.basedefense.client.gui.GeneratorUpgradeScreen;
import ca.lukegrahamlandry.basedefense.client.gui.PlayerMaterialsScreen;
import ca.lukegrahamlandry.basedefense.client.gui.ShopScreen;
import ca.lukegrahamlandry.basedefense.game.tile.TurretTile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.logging.Level;

public class ClientPacketHandlers {
    public static void openBaseUpgradeScreen(OpenBaseUpgradeGui p) {
        Minecraft.getInstance().setScreen(new BaseUpgradeScreen(p.storage, p.upgradeCost, p.nextLevel, p.itemsUnlockedNextLevel, p.pos, p.rfPerTick));
    }

    public static void openGeneratorScreen(OpenMaterialGeneratorGui p) {
        Minecraft.getInstance().setScreen(new GeneratorUpgradeScreen(p.tier, p.type, p.currentProduction, p.nextProduction, p.upgradeCost, p.playerMaterials, p.pos));
    }

    public static void openMaterialsScreen(OpenMaterialsGui p) {
        Minecraft.getInstance().setScreen(new PlayerMaterialsScreen(p.storage, p.production));
    }

    public static void openShipScreen(OpenMaterialShopGui p) {
        Minecraft.getInstance().setScreen(new ShopScreen(p.storage, p.baseTier));
    }

    public static void updateTurret(TurretTile.AnimUpdate p){
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockEntity tile = level.getBlockEntity(p.pos);
        if (tile instanceof TurretTile){
            ((TurretTile) tile).data.isShooting = p.isShooting;
            ((TurretTile) tile).data.hRotTarget = p.hRotTarget;
        }
    }

    public static void upgradeTurret(TurretTile.StatsUpdate p) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        BlockEntity tile = level.getBlockEntity(p.pos);
        if (tile instanceof TurretTile){
            ((TurretTile) tile).data.type = p.type;
            ((TurretTile) tile).data.tier = p.tier;
            ((TurretTile) tile).updateTexture();
        }
    }
}
