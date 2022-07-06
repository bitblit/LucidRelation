package com.erigir.lucid.modifier;


import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: chrweiss
 * Date: 8/12/13
 * Time: 3:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class CountingStringModifier implements IStringModifier {
    private String prefix;
    private AtomicLong counter = new AtomicLong(0);

    public CountingStringModifier(String prefix, AtomicLong counter) {
        this.prefix = prefix;
        this.counter = counter;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public AtomicLong getCounter() {
        return counter;
    }

    public void setCounter(AtomicLong counter) {
        this.counter = counter;
    }

    @Override
    public String modify(String match) {
        return (prefix == null) ? String.valueOf(counter.getAndIncrement()) : prefix + counter.getAndIncrement();
    }

}
