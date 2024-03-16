package com.so.sorpc.core.api;

import java.util.List;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-17
 */
public interface Router<T> {

    List<T> choose(List<T> providers);

    Router Default  =  p -> p;
}
