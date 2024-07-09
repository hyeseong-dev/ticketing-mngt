package com.mgnt.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class RequestBodyFilter extends AbstractGatewayFilterFactory<RequestBodyFilter.Config> {

    public RequestBodyFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            Flux<DataBuffer> requestBody = exchange.getRequest().getBody();
            return DataBufferUtils.join(requestBody)
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        String originalBody = new String(bytes, StandardCharsets.UTF_8);
                        log.info("Original Request Body: {}", originalBody);

                        // 기존의 body를 그대로 사용합니다.
                        BodyInserter bodyInserter = BodyInserters.fromValue(originalBody);
                        HttpHeaders headers = new HttpHeaders();
                        headers.putAll(exchange.getRequest().getHeaders());
                        headers.remove(HttpHeaders.CONTENT_LENGTH);

                        CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
                        return bodyInserter.insert(outputMessage, new BodyInserterContext())
                                .then(Mono.defer(() -> {
                                    ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(
                                            exchange.getRequest()) {
                                        @Override
                                        public HttpHeaders getHeaders() {
                                            long contentLength = headers.getContentLength();
                                            HttpHeaders httpHeaders = new HttpHeaders();
                                            httpHeaders.putAll(super.getHeaders());
                                            if (contentLength > 0) {
                                                httpHeaders.setContentLength(contentLength);
                                            } else {
                                                httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
                                            }
                                            return httpHeaders;
                                        }

                                        @Override
                                        public Flux<DataBuffer> getBody() {
                                            return outputMessage.getBody();
                                        }
                                    };
                                    return chain.filter(exchange.mutate().request(decorator).build());
                                }));
                    });
        };
    }

    public static class Config {
        // 필요한 경우 설정 속성을 여기에 추가
    }
}
