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
package exchange.core2.rest.controllers;


import exchange.core2.rest.commands.HelloMessage;
import exchange.core2.rest.commands.admin.StompApiNotificationMessage;
import exchange.core2.rest.model.api.StompApiTick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Controller
public class GreetingController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/hello")
    @SendTo("/topic/notifications")
    public StompApiNotificationMessage greeting(HelloMessage message) throws Exception {
        log.debug("Greeting 1 {}", message);
        Thread.sleep(200); // simulated delay
        log.debug("Greeting 2 {}", message);
        return new StompApiNotificationMessage("Hello, " + HtmlUtils.htmlEscape(message.getName()) + "!");
    }

//    @PostConstruct
//    public void start() {
//        CompletableFuture.supplyAsync(() -> {
//
//            while (true) {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                log.debug("Sending heartbit...");
//                simpMessagingTemplate.convertAndSend("/topic/notifications", new StompApiNotificationMessage(Instant.now().toString()));
//
//                Random rnd = ThreadLocalRandom.current();
//                simpMessagingTemplate.convertAndSend(
//                        "/topic/ticks/XBTCUSDT",
//                        new StompApiTick(BigDecimal.valueOf(rnd.nextFloat()), rnd.nextInt(100), System.currentTimeMillis()));
//            }
//
//        });
//
//    }

}