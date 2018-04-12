// Copyright (c) 2013, Conviva Inc.  All rights reserved.
package com.conviva.utils;

import java.util.LinkedList;
import java.util.List;

/**
 * An implementation for storing a fixed number of samples in first-in, first-out order.
 */
public class SlidingWindow<T> {
    private int _capacity = 0;
    private List<T> _slots = null;

    /// Create a sliding window at specified capacity
    public SlidingWindow (int capacity) {
        if(capacity > 0) {
            _capacity = capacity;
            _slots = new LinkedList<T>();
        }
    }

    /// Returns size of sliding window
    public int size() {
        return _slots.size();
    }

    /// Create a sample to the sliding window and remove the oldest sample if
    /// capacity is reached
    public void add(T elem) {
        _slots.add(0, elem);
        if(_slots.size() > _capacity) {
            _slots.remove(_slots.size()-1);
        }
    }

    /// Retrieve all the samples
    public List<T> getSlots() {
        return _slots;
    }

    /// Clear all the samples
    public void clear() {
        _slots = new LinkedList<T>();
    }
}
