package rocks.cleanstone.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;

public class CleanstoneEventPublisher {

    private static Logger LOGGER = LoggerFactory.getLogger(CleanstoneEventPublisher.class);
    @Autowired
    private ApplicationEventPublisher publisher;

    public <T> T publishEvent(T event, boolean rethrowErrors) {
        long preEventTime = System.currentTimeMillis();
        String eventName = event.getClass().getSimpleName();
        try {
            publisher.publishEvent(event);
        } catch (EventCancellationException e) {
            LOGGER.info("Event " + eventName + " was cancelled");
        } catch (Exception e) {
            if (!rethrowErrors)
                LOGGER.error("Failed to execute event listener for " + eventName, e);
            else throw e;
        }
        if (System.currentTimeMillis() - preEventTime > 50 && !eventName.startsWith("Async")) {
            LOGGER.warn("Listeners for non-async event " + eventName + " needed "
                    + (System.currentTimeMillis() - preEventTime)
                    + "ms to complete, this slows down the server");
        }
        return event;
    }

    /**
     * Prevents late event cancellation
     */
    @Order(value = EventAction.MODIFY + 100)
    @EventListener
    public void onCancellableEvent(CancellableEvent e) {
        e.setAllowCancelling(false);
    }
}
