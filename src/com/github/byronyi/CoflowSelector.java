package com.github.byronyi;

import sun.nio.ch.SelectorImpl;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.util.Set;

public class CoflowSelector extends AbstractSelector {

    private AbstractSelector selector = null;

    CoflowSelector(CoflowSelectorProvider proxy) throws IOException {
        super(proxy);
        selector = proxy.selectorProvider.openSelector();

        System.out.println("Selector " + selector +
                " opened in thread " + Thread.currentThread().getName());

    }

    @Override
    protected void implCloseSelector() throws IOException {

        System.out.println("Selector closed in thread " +
                Thread.currentThread().getName());

        ((SelectorImpl) selector).implCloseSelector();
    }

    @Override
    protected SelectionKey register(AbstractSelectableChannel ch,
                                    int ops, Object att) {

        System.out.println("Channel " + ch.getClass() + " registered with "
                + att.getClass() + " in thread " +
                Thread.currentThread().getName());

        try {
            return ch.register(selector, ops, att);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<SelectionKey> keys() {

        System.out.println("keys() called in " +
                Thread.currentThread().getName());

        return selector.keys();
    }

    @Override
    public Set<SelectionKey> selectedKeys() {

        System.out.println("selectedKeys() called in thread " +
                Thread.currentThread().getName());

        return selector.selectedKeys();
    }

    @Override
    public int selectNow() throws IOException {

        System.out.println("selectNow() called in thread " +
                Thread.currentThread().getName());

        return selector.selectNow();
    }

    @Override
    public int select(long timeout) throws IOException {

        System.out.println("select(" + timeout + ") called in thread " +
                Thread.currentThread().getName());

        return selector.select(timeout);
    }

    @Override
    public int select() throws IOException {

        System.out.println("select() called in thread " +
                Thread.currentThread().getName());

        return selector.select();
    }

    @Override
    public Selector wakeup() {

        System.out.println("wakeup() called in thread " +
                Thread.currentThread().getName());

        return selector.wakeup();
    }
}
