package rocks.cleanstone.net.minecraft.packet.outbound;

import rocks.cleanstone.net.minecraft.packet.MinecraftOutboundPacketType;
import rocks.cleanstone.net.minecraft.packet.inbound.InTabCompletePacket;
import rocks.cleanstone.net.packet.Packet;
import rocks.cleanstone.net.packet.PacketType;

import java.util.List;

public class OutTabCompletePacket implements Packet {

    private final InTabCompletePacket inTabCompletePacket;
    private final List<String> matches;

    public OutTabCompletePacket(InTabCompletePacket inTabCompletePacket, List<String> matches) {
        this.inTabCompletePacket = inTabCompletePacket;
        this.matches = matches;
    }

    public List<String> getMatches() {
        return matches;
    }

    public InTabCompletePacket getInTabCompletePacket() {
        return inTabCompletePacket;
    }

    @Override
    public PacketType getType() {
        return MinecraftOutboundPacketType.TAB_COMPLETE;
    }
}
