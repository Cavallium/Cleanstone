package rocks.cleanstone.net.packet.protocol;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import rocks.cleanstone.net.packet.Packet;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public abstract class LayeredProtocol implements Protocol {

    private final List<ServerProtocolLayer> protocolLayers = Lists.newLinkedList();

    public void registerProtocolLayer(ServerProtocolLayer protocolLayer) {
        protocolLayers.add(protocolLayer);
        protocolLayers.sort(Comparator.naturalOrder());
    }

    public Collection<ServerProtocolLayer> getProtocolLayers() {
        return protocolLayers;
    }

    @Nullable
    public ServerProtocolLayer getServerLayerFromClientLayer(ClientProtocolLayer clientLayer) {
        return protocolLayers.stream().filter(
                serverLayer -> serverLayer.getCorrespondingClientLayer() == clientLayer
        ).findFirst().orElse(null);
    }

    @Override
    public PacketCodec getPacketCodec(Class<? extends Packet> packetClass,
                                      ClientProtocolLayer clientLayer) {
        return new PacketCodec() {
            @Override
            public Packet decode(ByteBuf byteBuf) throws IOException { // receive from client
                // downgrade ByteBuf from client version to supported server version

                for (ServerProtocolLayer serverLayer : protocolLayers) { // higher to lower
                    if (serverLayer.getCorrespondingClientLayer().getOrderedVersionNumber() >= clientLayer.getOrderedVersionNumber())
                        continue;
                    PacketCodec serverCodec = serverLayer.getPacketCodec(packetClass);
                    byteBuf = serverCodec.downgradeByteBuf(byteBuf);
                }
                // lowest=current serverLayer decodes byteBuf
                return protocolLayers.get(protocolLayers.size()).getPacketCodec(packetClass).decode(byteBuf);
            }

            @Override
            public ByteBuf encode(ByteBuf byteBuf, Packet packet) throws IOException { // send to client
                // upgrade POJO from supported server version to client version

                protocolLayers.sort(Comparator.reverseOrder());
                try {
                    boolean skippedFirst = false;
                    for (ServerProtocolLayer serverLayer : protocolLayers) { // lower to higher
                        if (!skippedFirst) {
                            skippedFirst = true;
                            serverLayer.getPacketCodec(packet.getClass()).encode(byteBuf, packet);
                            continue;
                        }
                        serverLayer.getPacketCodec(packetClass).upgradeByteBuf(byteBuf);
                        if (serverLayer.getCorrespondingClientLayer().getOrderedVersionNumber() == clientLayer.getOrderedVersionNumber()) {
                            return byteBuf;
                        }
                    }
                    throw new RuntimeException("Client layer higher than highest supported server layer");
                } finally {
                    protocolLayers.sort(Comparator.naturalOrder());
                }
            }

            @Override
            public ByteBuf downgradeByteBuf(ByteBuf nextLayerByteBuf) {
                throw new UnsupportedOperationException("downgradeByteBuf is not supported with " +
                        "VersionedProtocol");
            }

            @Override
            public ByteBuf upgradeByteBuf(ByteBuf previousLayerByteBuf) {
                throw new UnsupportedOperationException("upgradeByteBuf is not supported with " +
                        "VersionedProtocol");
            }
        };
    }
}
