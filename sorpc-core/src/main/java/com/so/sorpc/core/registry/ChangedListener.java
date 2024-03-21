package com.so.sorpc.core.registry;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-18
 */
public interface ChangedListener {

    void fire(Event event);
}
