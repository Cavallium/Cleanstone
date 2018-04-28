package rocks.cleanstone.net.minecraft.packet.outbound;

import rocks.cleanstone.net.packet.Packet;
import rocks.cleanstone.net.packet.PacketType;
import rocks.cleanstone.net.minecraft.packet.MinecraftOutboundPacketType;

import java.util.Map;

public class StatisticsPacket implements Packet {

    private final Map<String, Integer> statistics;

    public StatisticsPacket(Map<String, Integer> statistics) {
        this.statistics = statistics;
    }

    public Map<String, Integer> getStatistics() {
        return statistics;
    }

    @Override
    public PacketType getType() {
        return MinecraftOutboundPacketType.STATISTICS;
    }
}