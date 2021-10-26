package com.geekbrains.Netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class StringHandler extends SimpleChannelInboundHandler<String> {

    private final SimpleDateFormat format;

    public StringHandler() {
        format = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String s) throws Exception {
        log.debug("received {}", s);
        String result = "[" + format.format(new Date()) + "] " + s;
        ctx.write(result);
    }


}
