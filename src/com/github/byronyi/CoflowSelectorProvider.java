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

    final SelectorProvider selectorProvider = DefaultSelectorProvider.create();

    @Override
    public DatagramChannel openDatagramChannel() throws IOException {
        return new CoflowDatagramChannel(this);
    }

    @Override
    public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException {
        return new CoflowDatagramChannel(this, family);
    }

    @Override
    public Pipe openPipe() throws IOException {
        return selectorProvider.openPipe();
    }

    @Override
    public AbstractSelector openSelector() throws IOException {
        return new CoflowSelector(this);
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
