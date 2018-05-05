package rocks.cleanstone.net.minecraft.packet.outbound;

import rocks.cleanstone.game.world.region.Position;
import rocks.cleanstone.io.vanilla.nbt.NamedBinaryTag;
import rocks.cleanstone.net.minecraft.packet.MinecraftOutboundPacketType;
import rocks.cleanstone.net.minecraft.packet.enums.UpdateBlockAction;
import rocks.cleanstone.net.packet.Packet;
import rocks.cleanstone.net.packet.PacketType;

public class UpdateBlockEntityPacket implements Packet {

    private final Position location;
    private final UpdateBlockAction action;
    private final NamedBinaryTag nbtData;

    public UpdateBlockEntityPacket(Position location, byte action, NamedBinaryTag nbtData) {
        this.location = location;
        this.action = UpdateBlockAction.fromActionID(action);
        this.nbtData = nbtData;
    }

    public UpdateBlockEntityPacket(Position location, UpdateBlockAction action, NamedBinaryTag nbtData) {
        this.location = location;
        this.action = action;
        this.nbtData = nbtData;
    }

    public Position getLocation() {
        return location;
    }

    public UpdateBlockAction getAction() {
        return action;
    }

    public NamedBinaryTag getNbtData() {
        return nbtData;
    }

    @Override
    public PacketType getType() {
        return MinecraftOutboundPacketType.UPDATE_BLOCK_ENTITY;
    }
}
