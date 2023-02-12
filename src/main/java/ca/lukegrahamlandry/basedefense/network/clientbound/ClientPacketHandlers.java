package ca.lukegrahamlandry.basedefense.network.clientbound;

import ca.lukegrahamlandry.basedefense.client.gui.BaseUpgradeScreen;
import ca.lukegrahamlandry.basedefense.client.gui.GeneratorUpgradeScreen;
import ca.lukegrahamlandry.basedefense.client.gui.PlayerMaterialsScreen;
import ca.lukegrahamlandry.basedefense.client.gui.ShopScreen;
import net.minecraft.client.Minecraft;

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
}
