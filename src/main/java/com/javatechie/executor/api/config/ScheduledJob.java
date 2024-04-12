package com.javatechie.executor.api.config;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public
class ScheduledJob {
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    @Scheduled(fixedRate = 5000)
    void execute() {
        if (enabled.get()) {
            // run spring batch here.
            try {
                Thread.sleep(6*10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void toggle() {
        enabled.set(!enabled.get());
    }



}