package com.stalker;

import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import java.util.UUID;

public class StalkerEntity extends PathfinderMob {
    static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(StalkerEntity.class, EntityDataSerializers.BOOLEAN);
    static final Item[] FOODS = {Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_MUTTON,
        Items.BREAD, Items.APPLE, Items.CARROT, Items.BAKED_POTATO, Items.COOKED_SALMON, Items.COOKED_COD};
    static final int WAIT = 1600;  // 1:20 min
    static final int RUN = 60;     // 3 s para correr

    UUID target; Item item = Items.COOKED_BEEF; int amount = 30, t = 0, angryT = 0;

    public StalkerEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.6)
            .add(Attributes.FOLLOW_RANGE, 64);
    }

    @Override protected void defineSynchedData() { super.defineSynchedData(); entityData.define(ANGRY, false); }
    public boolean isAngry() { return entityData.get(ANGRY); }
    @Override public boolean hurt(DamageSource s, float a) { return false; }
    @Override public boolean isPushable() { return false; }

    void start(ServerPlayer p) {
        target = p.getUUID();
        item = FOODS[random.nextInt(FOODS.length)];
        amount = item == Items.COOKED_BEEF ? 30 : 10 + random.nextInt(26);
        broadcast("§4[Stalker] §fQuiere §e" + amount + "x " + item.getDescription().getString() + "§f. Tienes 1:20.");
    }

    void broadcast(String msg) {
        for (ServerPlayer o : getServer().getPlayerList().getPlayers()) o.sendSystemMessage(Component.literal(msg));
    }

    @Override public InteractionResult mobInteract(Player pl, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        int have = 0;
        for (ItemStack s : pl.getInventory().items) if (s.is(item)) have += s.getCount();
        if (have < amount) {
            pl.displayClientMessage(Component.literal("§cTe faltan " + (amount - have) + " " + item.getDescription().getString()), true);
        } else {
            int left = amount;
            for (ItemStack s : pl.getInventory().items) if (s.is(item)) { int r = Math.min(left, s.getCount()); s.shrink(r); left -= r; }
            broadcast("§4[Stalker] §fSe fue...");
            discard();
        }
        return InteractionResult.SUCCESS;
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        Player p = target == null ? null : level().getPlayerByUUID(target);
        if (p == null || !p.isAlive()) { discard(); return; }
        if (!isAngry()) {
            getLookControl().setLookAt(p, 30, 30);
            if (++t >= WAIT) {
                entityData.set(ANGRY, true);
                broadcast("§4[Stalker] §c¡CORRE!");
                for (ServerPlayer o : getServer().getPlayerList().getPlayers())
                    o.playNotifySound(StalkerMod.ANGRY.get(), SoundSource.HOSTILE, 1f, 1f);
            }
        } else {
            getNavigation().moveTo(p, 3.0);
            if (++angryT >= RUN) { teleportTo(p.getX(), p.getY(), p.getZ()); p.kill(); discard(); }
        }
    }
}
