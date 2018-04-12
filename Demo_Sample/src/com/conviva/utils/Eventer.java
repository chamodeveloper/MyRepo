// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Class that manages a list of event handlers and allows dispatching event to all the handlers.
 */
public class Eventer {
    private List<Callable<Void>> _handlers;

    /// Create object that will store all the handlers
    public Eventer() {
        _handlers = new ArrayList<Callable<Void>>();
    }

    /// Deletes all event handlers
    public void Cleanup() {
        _handlers.clear();
    }

    /// Add a handler
    public void AddHandler(Callable<Void> handler) {
        _handlers.add(handler);
    }

    /// Find and remove handler
    public void DeleteHandler(Callable<Void> handler) {
        _handlers.remove(handler);
    }

    /// Call all the handlers
    public void DispatchEvent() {
        try {
            for (Callable<Void> c : _handlers) {
                c.call();
            }
        } catch (Exception e) {

        }
    }
}
