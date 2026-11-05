
package com.example.examplemod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public final class ModNetwork {

    private static final String PROTOCOL_VERSION = "1";

    private static final String BRACING_KEY =
            "faithfulbackport_bracing";

    private static final String CHARGE_KEY =
            "faithfulbackport_empty_hand_charge";

    private static final String READY_KEY =
            "faithfulbackport_empty_hand_charge_ready";

    private static final String EXPIRY_KEY =
            "faithfulbackport_empty_hand_charge_expiry";

    private static final SimpleChannel CHANNEL =
            NetworkRegistry.ChannelBuilder
                    .named(ExampleMod.id("main"))
                    .networkProtocolVersion(() -> PROTOCOL_VERSION)
                    .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                    .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                    .simpleChannel();

    private static int packetId = 0;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(
                        BracePacket.class,
                        packetId++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(BracePacket::encode)
                .decoder(BracePacket::decode)
                .consumerMainThread(BracePacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        LungePacket.class,
                        packetId++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(LungePacket::encode)
                .decoder(LungePacket::decode)
                .consumerMainThread(LungePacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        ChargeReadyPacket.class,
                        packetId++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(ChargeReadyPacket::encode)
                .decoder(ChargeReadyPacket::decode)
                .consumerMainThread(ChargeReadyPacket::handle)
                .add();

        CHANNEL.messageBuilder(
                        ChargeTransferAttackPacket.class,
                        packetId++,
                        NetworkDirection.PLAY_TO_SERVER
                )
                .encoder(ChargeTransferAttackPacket::encode)
                .decoder(ChargeTransferAttackPacket::decode)
                .consumerMainThread(ChargeTransferAttackPacket::handle)
                .add();
    }

    public static void sendBraceState(boolean bracing) {
        CHANNEL.sendToServer(new BracePacket(bracing));
    }

    public static void sendLungeRequest(float charge) {
        CHANNEL.sendToServer(new LungePacket(charge));
    }

    public static void sendChargeReady(boolean ready) {
        CHANNEL.sendToServer(new ChargeReadyPacket(ready));
    }

    public static void sendChargeTransferAttack(int targetId) {
        sendChargeTransferAttack(targetId, 1.0F);
    }

    public static void sendChargeTransferAttack(
            int targetId,
            float charge
    ) {
        CHANNEL.sendToServer(
                new ChargeTransferAttackPacket(targetId, charge)
        );
    }

    public static boolean hasValidChargeWindow(ServerPlayer player) {
        return player.getPersistentData().getBoolean(READY_KEY)
                && player.level().getGameTime()
                <= player.getPersistentData().getLong(EXPIRY_KEY);
    }

    public static void clearChargeWindow(ServerPlayer player) {
        player.getPersistentData().remove(READY_KEY);
        player.getPersistentData().remove(EXPIRY_KEY);
        player.getPersistentData().remove(CHARGE_KEY);
    }

    public static class BracePacket {
        private final boolean bracing;

        public BracePacket(boolean bracing) {
            this.bracing = bracing;
        }

        public static void encode(BracePacket packet, FriendlyByteBuf buffer) {
            buffer.writeBoolean(packet.bracing);
        }

        public static BracePacket decode(FriendlyByteBuf buffer) {
            return new BracePacket(buffer.readBoolean());
        }

        public static void handle(
                BracePacket packet,
                Supplier<NetworkEvent.Context> supplier
        ) {
            NetworkEvent.Context context = supplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player != null) {
                    player.getPersistentData().putBoolean(
                            BRACING_KEY,
                            packet.bracing
                    );
                }
            });

            context.setPacketHandled(true);
        }
    }

    public static class LungePacket {
        private final float charge;

        public LungePacket(float charge) {
            this.charge = charge;
        }

        public static void encode(LungePacket packet, FriendlyByteBuf buffer) {
            buffer.writeFloat(packet.charge);
        }

        public static LungePacket decode(FriendlyByteBuf buffer) {
            return new LungePacket(buffer.readFloat());
        }

        public static void handle(
                LungePacket packet,
                Supplier<NetworkEvent.Context> supplier
        ) {
            NetworkEvent.Context context = supplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null || !Float.isFinite(packet.charge)) {
                    return;
                }

                float charge = Math.max(
                        0.0F,
                        Math.min(1.0F, packet.charge)
                );

                LungeHandler.tryLunge(player, charge);
            });

            context.setPacketHandled(true);
        }
    }

    public static class ChargeReadyPacket {
        private final boolean ready;

        public ChargeReadyPacket(boolean ready) {
            this.ready = ready;
        }

        public static void encode(
                ChargeReadyPacket packet,
                FriendlyByteBuf buffer
        ) {
            buffer.writeBoolean(packet.ready);
        }

        public static ChargeReadyPacket decode(FriendlyByteBuf buffer) {
            return new ChargeReadyPacket(buffer.readBoolean());
        }

        public static void handle(
                ChargeReadyPacket packet,
                Supplier<NetworkEvent.Context> supplier
        ) {
            NetworkEvent.Context context = supplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null) {
                    return;
                }

                if (packet.ready) {
                    player.getPersistentData().putBoolean(READY_KEY, true);
                    player.getPersistentData().putLong(
                            EXPIRY_KEY,
                            player.level().getGameTime() + 20L
                    );
                } else {
                    clearChargeWindow(player);
                }
            });

            context.setPacketHandled(true);
        }
    }

    public static class ChargeTransferAttackPacket {
        private final int targetId;
        private final float charge;

        public ChargeTransferAttackPacket(int targetId, float charge) {
            this.targetId = targetId;
            this.charge = charge;
        }

        public static void encode(
                ChargeTransferAttackPacket packet,
                FriendlyByteBuf buffer
        ) {
            buffer.writeVarInt(packet.targetId);
            buffer.writeFloat(packet.charge);
        }

        public static ChargeTransferAttackPacket decode(FriendlyByteBuf buffer) {
            return new ChargeTransferAttackPacket(
                    buffer.readVarInt(),
                    buffer.readFloat()
            );
        }

        public static void handle(
                ChargeTransferAttackPacket packet,
                Supplier<NetworkEvent.Context> supplier
        ) {
            NetworkEvent.Context context = supplier.get();

            context.enqueueWork(() -> {
                ServerPlayer player = context.getSender();

                if (player == null
                        || player.isRemoved()
                        || player.isSpectator()
                        || player.getPersistentData().getBoolean(BRACING_KEY)
                        || !(player.getMainHandItem().getItem() instanceof SpearItem)
                        || !Float.isFinite(packet.charge)) {
                    System.out.println(
                            "[FaithfulBackport] Target attack rejected: "
                                    + "player state, bracing, held item, "
                                    + "or invalid charge."
                    );
                    return;
                }

                boolean transferredChargeValid =
                        hasValidChargeWindow(player);

                boolean fullSpearCharge =
                        packet.charge >= 0.99F;

                if (!transferredChargeValid && !fullSpearCharge) {
                    System.out.println(
                            "[FaithfulBackport] Target attack rejected: "
                                    + "neither full spear charge nor "
                                    + "valid transferred charge."
                    );
                    return;
                }

                Entity target = player.level().getEntity(packet.targetId);

                if (target == null
                        || target.isRemoved()
                        || target == player
                        || target.level() != player.level()
                        || target.isSpectator()
                        || target.distanceToSqr(player) > 36.0D
                        || !player.hasLineOfSight(target)) {
                    System.out.println(
                            "[FaithfulBackport] Target attack rejected: "
                                    + "target invalid, too far away, "
                                    + "or not visible."
                    );
                    return;
                }

                System.out.println(
                        "[FaithfulBackport] Server received target attack:"
                                + " target=" + target.getName().getString()
                                + ", charge=" + packet.charge
                                + ", transferredCharge=" + transferredChargeValid
                );

                try {
                    player.attack(target);
                    LungeHandler.tryLunge(player, packet.charge);
                } finally {
                    player.resetAttackStrengthTicker();
                    clearChargeWindow(player);
                }
            });

            context.setPacketHandled(true);
        }
    }
}
