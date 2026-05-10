package com.caiofilipe.openfinancehub.controller;

import com.caiofilipe.openfinancehub.dto.request.BankAccountRequest;
import com.caiofilipe.openfinancehub.dto.response.BankAccountResponse;
import com.caiofilipe.openfinancehub.model.User;
import com.caiofilipe.openfinancehub.service.BankAccountService;
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
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<BankAccountResponse> create(
            @Valid @RequestBody BankAccountRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bankAccountService.create(request, user));
    }

    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> listAll(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bankAccountService.listByUser(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(bankAccountService.getById(id, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        bankAccountService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
