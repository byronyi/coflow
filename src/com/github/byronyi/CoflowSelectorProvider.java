package com.github.byronyi;

import sun.nio.ch.DefaultSelectorProvider;
import sun.nio.ch.SelectorProviderImpl;

import java.io.IOException;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class CoflowSelectorProvider extends SelectorProviderImpl {

    final SelectorProvider selectorProvider = DefaultSelectorProvider.create();

    @Override
    public AbstractSelector openSelector() throws IOException {
        return new CoflowSelector(this);
    }
}
