package com.caiofilipe.openfinancehub.controller;

import com.caiofilipe.openfinancehub.dto.request.TransactionRequest;
import com.caiofilipe.openfinancehub.dto.response.TransactionResponse;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request, user));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> listAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.listByUser(user));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> listByAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.listByAccount(accountId, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.getById(id, user));
    }
}
