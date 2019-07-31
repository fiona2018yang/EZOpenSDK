package com.videogo.draw;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Frank on 2016/11/3.
 */
public class Subject {

    private Vector<DrawEventListener> repository = new Vector<DrawEventListener>();

    public void addEventListener(DrawEventListener listener) {
        this.repository.addElement(listener);
    }

    public void removeEventListener(DrawEventListener listener){
        this.repository.removeElement(listener);
    }

    public void notifyEvent(DrawEvent event) {
        Enumeration<DrawEventListener> en = this.repository.elements();
        while (en.hasMoreElements()) {
            DrawEventListener listener = en.nextElement();
            listener.handleDrawEvent(event);
        }
    }
}
