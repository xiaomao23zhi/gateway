package com.github.xiaomao23zhi.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.nacos.shaded.com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;

import javax.annotation.PostConstruct;

@Configuration
@Slf4j
public class RateLimitConfig {

    @PostConstruct
    public void initBlockHandlers() {

        BlockRequestHandler blockRequestHandler = (serverWebExchange, throwable) -> {

            String body = "{\"errorCode\":429, \"message\": \"Rate Limit\"}";

            if (throwable instanceof ParamFlowException) {

                ParamFlowException paramFlowException = (ParamFlowException) throwable;

                ParamFlowRule paramFlowRule = paramFlowException.getRule();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("resource", paramFlowRule.getResource());
                jsonObject.addProperty("limitParm", ((ParamFlowException) throwable).getLimitParam());
                jsonObject.addProperty("count", paramFlowRule.getCount());
                jsonObject.addProperty("durationInSec", paramFlowRule.getDurationInSec());

                body = jsonObject.toString();
            }

            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(body));
        };
        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }
}
