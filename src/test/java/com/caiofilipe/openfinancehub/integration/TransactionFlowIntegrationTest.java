package com.caiofilipe.openfinancehub.integration;

import com.caiofilipe.openfinancehub.IntegrationTest;
import com.caiofilipe.openfinancehub.dto.request.BankAccountRequest;
import com.caiofilipe.openfinancehub.dto.request.RegisterRequest;
import com.caiofilipe.openfinancehub.dto.request.TransactionRequest;
import com.caiofilipe.openfinancehub.model.AccountType;
import com.caiofilipe.openfinancehub.model.TransactionCategory;
import com.caiofilipe.openfinancehub.model.TransactionType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionFlowIntegrationTest extends IntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void fullFlow_createTransaction_updatesAccountBalance() throws Exception {
        // 1. Register
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Flow User");
        reg.setEmail("flow-" + UUID.randomUUID() + "@test.com");
        reg.setPassword("Senha123");

        MvcResult regResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString())
                .get("token").asText();

        String authHeader = "Bearer " + token;

        // 2. Create bank account (balance 1000)
        BankAccountRequest accReq = new BankAccountRequest();
        accReq.setBankName("Test Bank");
        accReq.setAccountNumber("ACC-" + UUID.randomUUID().toString().substring(0, 8));
        accReq.setAgency("0001");
        accReq.setAccountType(AccountType.CHECKING);
        accReq.setBalance(new BigDecimal("1000.00"));

        MvcResult accResult = mockMvc.perform(post("/api/bank-accounts")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andReturn();

        String accountId = objectMapper.readTree(accResult.getResponse().getContentAsString())
                .get("id").asText();

        // 3. Create DEBIT transaction of 300
        TransactionRequest txReq = new TransactionRequest();
        txReq.setBankAccountId(UUID.fromString(accountId));
        txReq.setAmount(new BigDecimal("300.00"));
        txReq.setDescription("Test debit");
        txReq.setType(TransactionType.DEBIT);
        txReq.setCategory(TransactionCategory.FOOD);

        mockMvc.perform(post("/api/transactions")
                        .header("Authorization", authHeader)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(txReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amount").value(300.00));

        // 4. Verify account balance updated to 700
        MvcResult updatedAcc = mockMvc.perform(get("/api/bank-accounts/" + accountId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode updatedBalance = objectMapper.readTree(updatedAcc.getResponse().getContentAsString())
                .get("balance");
        assertThat(updatedBalance.decimalValue()).isEqualByComparingTo("700.00");

        // 5. Verify transaction appears in list
        mockMvc.perform(get("/api/transactions/account/" + accountId)
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
