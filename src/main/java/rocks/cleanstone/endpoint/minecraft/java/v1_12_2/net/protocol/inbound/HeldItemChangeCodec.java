package rocks.cleanstone.endpoint.minecraft.java.v1_12_2.net.protocol.inbound;

import io.netty.buffer.ByteBuf;
import rocks.cleanstone.endpoint.minecraft.java.net.packet.inbound.HeldItemChangePacket;
import rocks.cleanstone.net.protocol.Codec;
import rocks.cleanstone.net.protocol.InboundPacketCodec;

@Codec
public class HeldItemChangeCodec implements InboundPacketCodec<HeldItemChangePacket> {

    @Override
    public HeldItemChangePacket decode(ByteBuf byteBuf) {
        short slot = byteBuf.readShort();

        return new HeldItemChangePacket(slot);
    }
}
