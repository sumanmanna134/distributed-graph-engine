package com.offlix.distributed_graph_engine.graph.core;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class GraphLock {
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void readLock(Runnable action){
        log.debug("Read Lock trying to acquire");
        lock.readLock().lock();
        log.debug("Read lock acquired");
        try{
            action.run();
        }finally {
            lock.readLock().unlock();
            log.debug("Read Lock released");
        }
    }

    public void writeLock(Runnable action){
        log.debug("Write Lock trying to acquire");
        lock.writeLock().lock();
        log.debug("Write lock acquired");
        try{
            action.run();
        }finally {
            lock.writeLock().unlock();
            log.debug("Write Lock released");
        }
    }


}
