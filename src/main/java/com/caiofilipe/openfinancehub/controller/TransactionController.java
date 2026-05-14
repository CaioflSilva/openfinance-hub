package com.caiofilipe.openfinancehub.controller;

import com.caiofilipe.openfinancehub.dto.request.TransactionRequest;
import com.caiofilipe.openfinancehub.dto.response.TransactionResponse;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Criação e consulta de transações financeiras")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Criar transação (publicada em fila RabbitMQ para processamento assíncrono)")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transação criada e enfileirada"),
        @ApiResponse(responseCode = "403", description = "Conta pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada"),
        @ApiResponse(responseCode = "422", description = "Saldo insuficiente")
    })
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar todas as transações do usuário autenticado (paginado)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<Page<TransactionResponse>> listAll(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.listByUser(user, pageable));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Listar transações de uma conta específica (paginado)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página retornada com sucesso"),
        @ApiResponse(responseCode = "403", description = "Conta pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Conta bancária não encontrada")
    })
    public ResponseEntity<Page<TransactionResponse>> listByAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(transactionService.listByAccount(accountId, user, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transação encontrada"),
        @ApiResponse(responseCode = "403", description = "Transação pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })
    public ResponseEntity<TransactionResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.getById(id, user));
    }
}
