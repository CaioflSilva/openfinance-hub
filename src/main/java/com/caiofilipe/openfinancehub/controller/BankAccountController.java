package com.caiofilipe.openfinancehub.controller;

import com.caiofilipe.openfinancehub.dto.request.BankAccountRequest;
import com.caiofilipe.openfinancehub.dto.response.BankAccountResponse;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
@Tag(name = "Bank Accounts", description = "Gerenciamento de contas bancárias")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    @Operation(summary = "Criar nova conta bancária")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<BankAccountResponse> create(
            @Valid @RequestBody BankAccountRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccountService.create(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar contas do usuário autenticado")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
        @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public ResponseEntity<List<BankAccountResponse>> listAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bankAccountService.listByUser(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID (com ownership check)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conta encontrada"),
        @ApiResponse(responseCode = "403", description = "Conta pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<BankAccountResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bankAccountService.getById(id, user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover conta bancária")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Conta removida com sucesso"),
        @ApiResponse(responseCode = "403", description = "Conta pertence a outro usuário"),
        @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        bankAccountService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
