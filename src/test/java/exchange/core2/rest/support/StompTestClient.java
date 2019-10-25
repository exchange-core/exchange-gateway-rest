/*
 * Copyright 2019 Maksim Zheravin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exchange.core2.rest.support;

import exchange.core2.rest.model.api.StompApiTick;
import exchange.core2.rest.model.api.StompOrderUpdate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.core.Is;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static exchange.core2.rest.support.TestService.STOMP_TOPIC_ORDERS_PREFIX;
import static exchange.core2.rest.support.TestService.STOMP_TOPIC_TICKS_PREFIX;


@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StompTestClient {

    private BlockingDeque<StompApiTick> stompTicksQueue;
    private BlockingDeque<StompOrderUpdate> stompOrderUpdateQueue;

    public static StompTestClient create(String symbolName, List<Long> uids, int randomServerPort) throws ExecutionException, InterruptedException {

        final BlockingDeque<StompApiTick> stompTicksQueue = new LinkedBlockingDeque<>();
        final BlockingDeque<StompOrderUpdate> stompOrderUpdatesQueue = new LinkedBlockingDeque<>();

        final StandardWebSocketClient webSocketClient = new StandardWebSocketClient();
        final SockJsClient sockJsClient = new SockJsClient(Collections.singletonList(new WebSocketTransport(webSocketClient)));
        final WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        final String url = "ws://localhost:" + randomServerPort + "/ticks-websocket";

        final StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe(STOMP_TOPIC_TICKS_PREFIX + symbolName, this);
                uids.forEach(uid -> session.subscribe(STOMP_TOPIC_ORDERS_PREFIX + uid, new OrderUpdatesHandler(uid, stompOrderUpdatesQueue)));
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                log.warn("Stomp Error:", exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
//                super.handleTransportError(session, exception);
                log.warn("Stomp Transport Error: {}", exception.getMessage());
            }

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return StompApiTick.class;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void handleFrame(StompHeaders stompHeaders, Object o) {
                stompTicksQueue.add((StompApiTick) o);
            }
        };


        log.info("CONNECTING...");
        final StompSession stompSession = stompClient.connect(url, sessionHandler).get();
        log.info("CONNECTED: sessionId={}", stompSession.getSessionId());

        return new StompTestClient(stompTicksQueue, stompOrderUpdatesQueue);
    }

    public StompApiTick pollTick() throws InterruptedException {
        return stompTicksQueue.poll(3, TimeUnit.SECONDS);
    }

    public boolean hasTicks() {
        return !stompTicksQueue.isEmpty();
    }

    public Map<Long, List<StompOrderUpdate>> pollOrderUpdates(int num) {
        return Stream.generate(
                () -> {
                    try {
                        return stompOrderUpdateQueue.poll(3, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .limit(num)
                .collect(Collectors.groupingBy(StompOrderUpdate::getUid, Collectors.toList()));
    }

    @RequiredArgsConstructor
    private static class OrderUpdatesHandler extends StompSessionHandlerAdapter {
        private final long uid;
        private final BlockingDeque<StompOrderUpdate> stompOrderUpdateQueue;

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return StompOrderUpdate.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            log.info("Handle StompOrderUpdate: {}", o);
            StompOrderUpdate orderUpdate = (StompOrderUpdate) o;
            org.hamcrest.MatcherAssert.assertThat(orderUpdate.getUid(), Is.is(uid));
            stompOrderUpdateQueue.add(orderUpdate);
        }
    }
}
