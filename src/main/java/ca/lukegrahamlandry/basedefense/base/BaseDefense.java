package ca.lukegrahamlandry.basedefense.base;

import ca.lukegrahamlandry.basedefense.ModMain;
import ca.lukegrahamlandry.basedefense.base.attacks.AttackWaveType;
import ca.lukegrahamlandry.basedefense.base.config.SyncedConfigData;
import ca.lukegrahamlandry.basedefense.base.material.MaterialCollection;
import ca.lukegrahamlandry.basedefense.base.material.MaterialGeneratorType;
import ca.lukegrahamlandry.basedefense.base.teams.TeamManager;
import ca.lukegrahamlandry.basedefense.network.clientbound.OpenMaterialsGui;
import ca.lukegrahamlandry.lib.base.json.JsonHelper;
import ca.lukegrahamlandry.lib.config.ConfigWrapper;
import ca.lukegrahamlandry.lib.keybind.KeybindWrapper;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;

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
        JsonHelper.addTypeAdapter(MobEffectInstance.class, new EffectTypeAdapter());
        TeamManager.init();
        MaterialGeneratorType.init();
        BaseTier.init();
        MaterialShop.init();
        AttackWaveType.init();
        TurretTiers.init();
    }

    public static class EffectTypeAdapter implements JsonDeserializer<MobEffectInstance>, JsonSerializer<MobEffectInstance> {
        private static class Info {
            String effect = "minecraft:luck";
            int duration = 0;
            int amplifier = 0;
        }

        public MobEffectInstance deserialize(JsonElement data, Type type, JsonDeserializationContext ctx) throws JsonParseException {
            Info info = JsonHelper.get().fromJson(data, Info.class);

            MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(info.effect));
            if (effect == null) effect = MobEffects.LUCK;

            return new MobEffectInstance(effect, info.duration, info.amplifier);
        }

        public JsonElement serialize(MobEffectInstance obj, Type type, JsonSerializationContext ctx) {
            Info info = new Info();
            info.amplifier = obj.getAmplifier();
            info.duration = obj.getDuration();
            ResourceLocation effect = ForgeRegistries.MOB_EFFECTS.getKey(obj.getEffect());
            if (effect != null) info.effect = effect.toString();
            return JsonHelper.get().toJsonTree(info);
        }
    }
}
