// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.session;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores arriving events in a queue to send them together.
 */
public class EventQueue {
    private List<Map<String, Object>> _events = null;
    private int _nextSeqNumber = 0;

    /**
     * Create an event queue
     */
    public EventQueue() {
        _events = new ArrayList<Map<String, Object>>();
    }

    /**
     * Append a single event into the queue
     * @param type event type
     * @param data Event data
     * @param timeSinceSessionStart time since session started.
     */
    public void enqueueEvent(String type, Map<String, Object> data,
            int timeSinceSessionStart) {
        data.put("t", type);
        data.put("st", timeSinceSessionStart);
        data.put("seq", _nextSeqNumber);
        _nextSeqNumber++;
        _events.add(data);
    }

    /**
     *  Returns number of events in queue
     * @return size of event queue
     */
    public int size() {
        return _events.size();
    }

    /**
     * Return all the events currently queued, and empty out the queue
     * @return List of all queued events
     */
    public List<Map<String, Object>> flushEvents() {
        List<Map<String, Object>> currentEvents = _events;
        _events = new ArrayList<Map<String, Object>>();
        return currentEvents;
    }
}
