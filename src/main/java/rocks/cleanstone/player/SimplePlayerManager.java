package rocks.cleanstone.player;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import rocks.cleanstone.core.CleanstoneServer;
import rocks.cleanstone.game.Identity;
import rocks.cleanstone.game.chat.message.Text;
import rocks.cleanstone.game.entity.Entity;
import rocks.cleanstone.net.Connection;
import rocks.cleanstone.net.packet.Packet;
import rocks.cleanstone.player.event.AsyncPlayerInitializationEvent;
import rocks.cleanstone.player.event.AsyncPlayerTerminationEvent;
import rocks.cleanstone.player.event.PlayerJoinEvent;
import rocks.cleanstone.player.event.PlayerQuitEvent;
import rocks.cleanstone.storage.player.PlayerDataSource;
import rocks.cleanstone.storage.player.PlayerDataSourceCreationException;
import rocks.cleanstone.storage.player.PlayerDataSourceFactory;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Component
public class SimplePlayerManager implements PlayerManager {

    private final Collection<Player> onlinePlayers = new CopyOnWriteArraySet<>();
    private final Collection<Player> terminatingPlayers = new CopyOnWriteArraySet<>();
    private final Collection<Identity> playerIDs = Sets.newConcurrentHashSet();
    private final PlayerDataSource playerDataSource;

    public SimplePlayerManager(PlayerDataSourceFactory playerDataSourceFactory) throws PlayerDataSourceCreationException {
        this.playerDataSource = playerDataSourceFactory.get();
    }

    @Override
    public Collection<Player> getOnlinePlayers() {
        return Collections.unmodifiableCollection(onlinePlayers);
    }

    @Override
    @Nullable
    public Player getOnlinePlayer(Identity id) {
        return onlinePlayers.stream()
                .filter(player -> player.getID().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    @Nullable
    public Player getOnlinePlayer(Connection connection) {
        return onlinePlayers.stream()
                .filter(player -> player instanceof OnlinePlayer)
                .filter(player -> ((OnlinePlayer) player).getConnection() == connection)
                .findAny().orElse(null);
    }

    @Override
    @Nullable
    public Player getOnlinePlayer(String name) {
        return onlinePlayers.stream()
                .filter(player -> player.getID().getName().equalsIgnoreCase(name))
                .findAny().orElse(null);
    }

    @Nullable
    @Override
    public Player getOnlinePlayer(Entity entity) {
        return onlinePlayers.stream()
                .filter(player -> player.getEntity() == entity)
                .findAny().orElse(null);
    }

    @Override
    public Collection<Identity> getAllPlayerIDs() {
        return Collections.unmodifiableCollection(playerIDs);
    }

    @Override
    public Identity getPlayerID(UUID uuid, String accountName) {
        return playerIDs.stream().filter(id -> id.getUUID().equals(uuid)).findFirst()
                .orElse(registerNewPlayerID(uuid, accountName));
    }

    @Override
    public PlayerDataSource getPlayerDataSource() {
        return playerDataSource;
    }

    private Identity registerNewPlayerID(UUID uuid, String accountName) {
        Identity id = new SimplePlayerIdentity(uuid, accountName);
        playerIDs.add(id);
        return id;
    }

    @Override
    public boolean isPlayerOperator(Identity playerID) {
        List<String> ops = CleanstoneServer.getInstance().getMinecraftConfig().getOps();
        return ops.contains(playerID.getName()) || ops.contains(playerID.getUUID().toString()); //TODO: Make this beauty <3
    }

    @Override
    public void broadcastPacket(Packet packet, Player... broadcastExemptions) {
        Collection<Player> exemptions = Arrays.asList(broadcastExemptions);
        getOnlinePlayers().stream().filter(p -> !exemptions.contains(p))
                .forEach(onlinePlayer -> onlinePlayer.sendPacket(packet));
    }

    @Override
    public void initializePlayer(Player player) {
        Preconditions.checkState(onlinePlayers.add(player),
                "Cannot initialize already initialized player " + player);
        log.info("Initializing player " + player);
        try {
            CleanstoneServer.publishEvent(new AsyncPlayerInitializationEvent(player), true);
        } catch (Exception e) {
            log.error("Error occurred during player initialization for " + player, e);
            player.kick(Text.of("Error occurred during player initialization"));
            return;
        }
        CleanstoneServer.publishEvent(new PlayerJoinEvent(player));
    }

    @Override
    public void terminatePlayer(Player player) {
        Preconditions.checkState(onlinePlayers.contains(player),
                "Cannot terminate already terminated / non-initialized player " + player);
        Preconditions.checkState(terminatingPlayers.add(player),
                "Already terminating player " + player);

        log.info("Terminating player " + player);
        CleanstoneServer.publishEvent(new PlayerQuitEvent(player));
        CleanstoneServer.publishEvent(new AsyncPlayerTerminationEvent(player));
        onlinePlayers.remove(player);
        terminatingPlayers.remove(player);
    }

    @Override
    public boolean isTerminating(Player player) {
        return terminatingPlayers.contains(player);
    }

    @PreDestroy
    void destroy() {
        playerDataSource.close();
    }
}
