package com.videogo.draw;

import java.util.EventListener;

/**
 * Created by Frank on 2016/11/3.
 */
public interface DrawEventListener extends EventListener {

    void handleDrawEvent(DrawEvent event);
}