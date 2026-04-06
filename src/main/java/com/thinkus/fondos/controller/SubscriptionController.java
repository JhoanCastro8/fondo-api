package com.thinkus.fondos.controller;

import com.thinkus.fondos.dto.*;
import com.thinkus.fondos.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public ResponseEntity<SubscriptionResponseDTO> subscribe(
            @RequestBody @Valid SubscribeRequestDTO request) {

        return ResponseEntity.ok(subscriptionService.subscribe(request));
    }

    @DeleteMapping
    public ResponseEntity<String> cancel(
            @RequestBody @Valid CancelSubscriptionRequestDTO request) {

        subscriptionService.cancel(request.getClientId(), request.getFundId());
        return ResponseEntity.ok("Cancelación exitosa");
    }

    @PostMapping("/transactions")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactions(
            @RequestBody @Valid TransactionRequestDTO request) {

        return ResponseEntity.ok(
                subscriptionService.getTransactions(request.getClientId())
        );
    }
}
