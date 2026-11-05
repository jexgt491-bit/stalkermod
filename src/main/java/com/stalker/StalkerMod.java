package com.stalker;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

@Mod("stalkermod")
public class StalkerMod {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "stalkermod");
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "stalkermod");
    public static final RegistryObject<EntityType<StalkerEntity>> STALKER = ENTITIES.register("stalker",
        () -> EntityType.Builder.of(StalkerEntity::new, MobCategory.MONSTER).sized(1f, 2.5f).build("stalker"));
    public static final RegistryObject<SoundEvent> APPEAR = SOUNDS.register("appear",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("stalkermod", "appear")));
    public static final RegistryObject<SoundEvent> ANGRY = SOUNDS.register("angry",
        () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("stalkermod", "angry")));

    public StalkerMod() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus);
        SOUNDS.register(bus);
    }

    @Mod.EventBusSubscriber(modid = "stalkermod", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBus {
        @SubscribeEvent
        public static void attrs(EntityAttributeCreationEvent e) {
            e.put(STALKER.get(), StalkerEntity.attributes().build());
        }
    }

    @Mod.EventBusSubscriber(modid = "stalkermod", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientBus {
        @SubscribeEvent
        public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
            e.registerEntityRenderer(STALKER.get(), StalkerRenderer::new);
        }
    }

    // Aparición aleatoria: solo de noche, con cooldown (~20 min reales) entre apariciones
    @Mod.EventBusSubscriber(modid = "stalkermod")
    public static class Events {
        @SubscribeEvent
        public static void tick(TickEvent.PlayerTickEvent e) {
            if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
            var level = p.level();
            if (!level.isNight() || level.random.nextInt(12000) != 0) return;
            long now = level.getGameTime();
            if (now < p.getPersistentData().getLong("stalkerNext")) return;
            p.getPersistentData().putLong("stalkerNext", now + 24000);

            Vec3 look = p.getLookAngle();
            StalkerEntity s = STALKER.get().create(level);
            s.moveTo(p.getX() + look.x * 4, p.getY(), p.getZ() + look.z * 4);
            s.start(p);
            level.addFreshEntity(s);
            for (ServerPlayer o : p.getServer().getPlayerList().getPlayers())
                o.playNotifySound(APPEAR.get(), SoundSource.HOSTILE, 1f, 1f);
        }
    }
}
