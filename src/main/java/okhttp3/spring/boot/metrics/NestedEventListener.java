package okhttp3.spring.boot.metrics;

import okhttp3.EventListener;

import java.util.List;

public class NestedEventListener extends EventListener {

    private List<EventListener> eventListeners;

    public NestedEventListener(List<EventListener> eventListeners) {
        this.eventListeners = eventListeners;
    }




}
