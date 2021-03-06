package rocks.cleanstone.endpoint.minecraft.java.net.pluginchannel;

import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PluginChannelRegistry {

    private final Map<String, PluginChannel<? extends PluginChannel.PluginMessage>> pluginChannels = new ConcurrentHashMap<>();

    public PluginChannelRegistry(List<? extends PluginChannel<?>> pluginChannels) {
        pluginChannels.forEach(this::registerPluginChannel);
    }

    public void registerPluginChannel(PluginChannel<? extends PluginChannel.PluginMessage> pluginChannel) {
        if (pluginChannels.containsKey(pluginChannel.getName())) {
            throw new RuntimeException("PluginChanel is already registered");
        }

        pluginChannels.put(pluginChannel.getName(), pluginChannel);
    }

    @SuppressWarnings("unchecked")
    public <T extends PluginChannel.PluginMessage> PluginChannel<T> getPluginChannel(T pluginMessage) {
        return (PluginChannel<T>) pluginChannels.values().stream()
                .filter(pluginChannel -> messageTypeMatches(pluginMessage, pluginChannel))
                .findAny().orElse(null);
    }

    private <T extends PluginChannel.PluginMessage> boolean messageTypeMatches(T pluginMessage, PluginChannel<?> pluginChannel) {
        Class<?> messageClass = GenericTypeResolver.resolveTypeArgument(pluginChannel.getClass(), PluginChannel.class);

        return pluginMessage.getClass().equals(messageClass);
    }

    public PluginChannel<? extends PluginChannel.PluginMessage> getPluginChannel(String name) {
        return pluginChannels.get(name);
    }
}
