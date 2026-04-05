// SessionRefreshedEvent.java
package com.goldapp.psoni.event;

import org.springframework.context.ApplicationEvent;

public class SessionRefreshedEvent extends ApplicationEvent {
    public SessionRefreshedEvent(Object source) {
        super(source);
    }


}