package com.github.byronyi;

import sun.nio.ch.DefaultSelectorProvider;

import java.io.IOException;
import java.net.ProtocolFamily;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class CoflowSelectorProvider extends SelectorProvider {

    final private SelectorProvider defaultProvider =
        DefaultSelectorProvider.create();

    protected SelectorProvider getDefaultProvider() {
        return defaultProvider;
    }

    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        return defaultProvider.openDatagramChannel();
    }

    @Override
    public DatagramChannel openDatagramChannel(ProtocolFamily family)
        throws IOException {
        return defaultProvider.openDatagramChannel(family);
    }

    @Override
    public Pipe openPipe() throws IOException {
        return defaultProvider.openPipe();
    }

    @Override
    public AbstractSelector openSelector() throws IOException {
        return defaultProvider.openSelector();
    }

    @Override
    public SocketChannel openSocketChannel() throws IOException {
        return new CoflowSocketChannel(this);
    }

    @Override
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        return new CoflowServerSocketChannel(this);
    }
}
