package com.ashish.hackathon.ignitedroids.concurrency;

import org.eclipse.jgit.lib.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentMetricsFinder implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(ConcurrentMetricsFinder.class);

    private final Ref firstRef;
    private final Ref secondRef;

    public ConcurrentMetricsFinder(Ref firstRef, Ref secondRef) {
        this.firstRef = firstRef;
        this.secondRef = secondRef;
    }

    @Override
    public void run() {

    }
}
