package com.so.sorpc.core.registry;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author someecho <linghan.ma@gmail.com>
 * Created on 2024-03-18
 */
@Data
@AllArgsConstructor
public class Event {
    List<String> data;
}
