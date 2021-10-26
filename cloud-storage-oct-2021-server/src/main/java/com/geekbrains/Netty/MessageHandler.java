package com.geekbrains.Netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.AbstractDnsMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractDnsMessage>{
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractDnsMessage msg) throws Exception {
        log.debug("received : {}" , msg);
        ctx.writeAndFlush(msg);
    }
}
