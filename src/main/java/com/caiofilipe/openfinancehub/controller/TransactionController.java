package com.caiofilipe.openfinancehub.controller;

import com.caiofilipe.openfinancehub.dto.request.TransactionRequest;
import com.caiofilipe.openfinancehub.dto.response.TransactionResponse;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Transactions", description = "Criação e consulta de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Criar transação (publicada em fila RabbitMQ para processamento assíncrono)")
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar todas as transações do usuário autenticado")
    public ResponseEntity<List<TransactionResponse>> listAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.listByUser(user));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Listar transações de uma conta específica")
    public ResponseEntity<List<TransactionResponse>> listByAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.listByAccount(accountId, user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.getById(id, user));
    }
}
