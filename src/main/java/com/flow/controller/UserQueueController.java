package com.flow.controller;

import com.flow.dto.AllowUserResponse;
import com.flow.dto.AllowedUserResponse;
import com.flow.dto.RankNumberResponse;
import com.flow.dto.RegisterUserResponse;
import com.flow.service.UserQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {
    private final UserQueueService userQueueService;

    // 등록 할 수 있는 Api path
    @PostMapping("")
    public Mono<RegisterUserResponse> registerUser(@RequestParam(name = "queue", defaultValue = "default") String queue, @RequestParam(name = "user_id") Long userId){
        return userQueueService.registerWaitQueue(queue, userId)
                .map(i -> new RegisterUserResponse(i));
    }

    @PostMapping("/allow")
    public Mono<AllowUserResponse> allowUser(@RequestParam(name = "queue", defaultValue = "default") String queue, @RequestParam(name = "count") Long count){
        return userQueueService.allowUser(queue, count)
                .map(allowed -> new AllowUserResponse(count, allowed));
    }

    @GetMapping("/allowed")
    public Mono<AllowedUserResponse> isAllowedUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                   @RequestParam(name = "user_id") Long userId,
                                                   @RequestParam(name="token") String token){
        return userQueueService.isAllowedByToken(queue, userId, token)
                .map(allowed -> new AllowedUserResponse(allowed));
    }

    @GetMapping("/rank")
    public Mono<RankNumberResponse> getRankUser(@RequestParam(name = "queue", defaultValue = "default") String queue, @RequestParam(name = "user_id") Long userId){
        return userQueueService.getRank(queue, userId)
                .map(rank -> new RankNumberResponse(rank));
    }

    @GetMapping("/touch")
    Mono<?> touch(@RequestParam(name = "queue", defaultValue = "default") String queue, @RequestParam(name = "user_id") Long userId, ServerWebExchange exchange){
        return Mono.defer(() -> userQueueService.generateToken(queue, userId))
                .map(token ->{
                    exchange.getResponse()
                            .addCookie(ResponseCookie
                                    .from("user-queue-%s-token".formatted(queue), token)
                                    .maxAge(Duration.ofSeconds(300))
                                    .path("/")
                            .build());
                    return token;
                });
    }

}
