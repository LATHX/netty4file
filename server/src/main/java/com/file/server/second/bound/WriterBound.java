package com.file.server.second.bound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.AbstractNioByteChannel;
import io.netty.channel.nio.AbstractNioChannel;

import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;

public class WriterBound extends AbstractNioChannel {
    protected WriterBound(Channel parent, SelectableChannel ch, int readInterestOp) {
        super(parent, ch, readInterestOp);
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return null;
    }

    @Override
    protected SocketAddress localAddress0() {
        return null;
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return null;
    }

    @Override
    protected void doBind(SocketAddress socketAddress) throws Exception {

    }

    @Override
    protected void doDisconnect() throws Exception {

    }

    @Override
    protected void doWrite(ChannelOutboundBuffer channelOutboundBuffer) throws Exception {

    }

    @Override
    protected boolean doConnect(SocketAddress socketAddress, SocketAddress socketAddress1) throws Exception {
        return false;
    }

    @Override
    protected void doFinishConnect() throws Exception {

    }

    @Override
    public ChannelConfig config() {
        return null;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public ChannelMetadata metadata() {
        return null;
    }
}
