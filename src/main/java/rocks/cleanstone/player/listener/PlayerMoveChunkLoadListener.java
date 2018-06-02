package rocks.cleanstone.player.listener;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.util.UUID;

import rocks.cleanstone.game.Position;
import rocks.cleanstone.game.world.generation.FlatWorldGenerator;
import rocks.cleanstone.game.world.region.chunk.vanilla.ChunkDataPacketFactory;
import rocks.cleanstone.net.packet.outbound.ChunkDataPacket;
import rocks.cleanstone.net.packet.outbound.UnloadChunkPacket;
import rocks.cleanstone.player.Player;
import rocks.cleanstone.player.event.PlayerMoveEvent;
import rocks.cleanstone.player.event.PlayerQuitEvent;

public class PlayerMoveChunkLoadListener {

    private final Multimap<UUID, Pair<Integer, Integer>> playerHasLoaded = ArrayListMultimap.create();

    private final FlatWorldGenerator flatWorldGenerator = new FlatWorldGenerator(); //TODO: Replace with getChunk(..)

    @Async("playerExec")
    @EventListener
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        final int chunkX = ((int) playerMoveEvent.getNewPosition().getX()) >> 4;
        final int chunkY = ((int) playerMoveEvent.getNewPosition().getZ()) >> 4;

        final Player player = playerMoveEvent.getPlayer();
        UUID uuid = player.getId().getUUID();

        if (isSameChunk(playerMoveEvent.getOldPosition(), playerMoveEvent.getNewPosition())
                && hasPlayerLoaded(uuid, chunkX, chunkY)) {
            return;
        }
        sendNewNearbyChunks(player, chunkX, chunkY);
    }

    protected synchronized void sendNewNearbyChunks(Player player, int chunkX, int chunkY) {
        final int sendDistance = 4;
        final int checkDistance = sendDistance * 2;

        UUID uuid = player.getId().getUUID();
        for (int relX = -checkDistance; relX < checkDistance; relX++) {
            for (int relY = -checkDistance; relY < checkDistance; relY++) {
                final int currentX = chunkX + relX;
                final int currentY = chunkY + relY;

                if (relX < -sendDistance || relX > sendDistance
                        || relY < -sendDistance || relY > sendDistance) { // TODO: Some weird flapping happens here
                    if (hasPlayerLoaded(uuid, currentX, currentY)) {
                        playerUnload(uuid, currentX, currentY);
                        sendChunkUnload(player, currentX, currentY);
                    }
                    continue;
                }

                if (!hasPlayerLoaded(uuid, currentX, currentY)) {
                    playerLoad(uuid, currentX, currentY);
                    sendChunkLoad(player, currentX, currentY);
                }
            }
        }
    }

    protected void sendChunkUnload(Player player, int x, int y) {
        UnloadChunkPacket unloadChunkPacket = new UnloadChunkPacket(x, y);
        player.sendPacket(unloadChunkPacket);
    }

    protected void sendChunkLoad(Player player, int x, int y) {
        ChunkDataPacket chunkDataPacket = ChunkDataPacketFactory.create(flatWorldGenerator.generateChunk(x, y), true);
        player.sendPacket(chunkDataPacket);
    }

    @Async("playerExec")
    @EventListener
    public void onPlayerDisconnect(PlayerQuitEvent playerQuitEvent) {
        playerUnloadAll(playerQuitEvent.getPlayer().getId().getUUID());
    }

    private boolean isSameChunk(Position oldPosition, Position newPosition) {
        final int oldChunkX = ((int) oldPosition.getX()) >> 4;
        final int oldChunkY = ((int) oldPosition.getZ()) >> 4;

        final int newChunkX = ((int) newPosition.getX()) >> 4;
        final int newChunkY = ((int) newPosition.getZ()) >> 4;

        return oldChunkX == newChunkX && oldChunkY == newChunkY;
    }

    private void playerLoad(UUID uuid, int chunkX, int chunkY) {
        playerHasLoaded.get(uuid).add(Pair.of(chunkX, chunkY));
    }

    private void playerUnload(UUID uuid, int chunkX, int chunkY) {
        playerHasLoaded.get(uuid).remove(Pair.of(chunkX, chunkY));
    }

    private boolean hasPlayerLoaded(UUID uuid, int chunkX, int chunkY) {
        return playerHasLoaded.get(uuid).contains(Pair.of(chunkX, chunkY));
    }

    private synchronized void playerUnloadAll(UUID uuid) {
        playerHasLoaded.removeAll(uuid);
    }
}