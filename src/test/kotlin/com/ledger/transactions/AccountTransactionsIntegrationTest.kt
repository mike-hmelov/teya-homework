package com.ledger.transactions

import com.ledger.IntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@IntegrationTest
class AccountTransactionsIntegrationTest {

    @Autowired
    lateinit var context: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    private fun createAccountAndGetId(name: String? = null): String {
        val accountName = name ?: java.util.UUID.randomUUID().toString()
        val result = mockMvc.perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"$accountName\"}")
        )
            .andExpect(status().isCreated)
            .andReturn()

        val jsonResponse = result.response.contentAsString
        return com.jayway.jsonpath.JsonPath.read(jsonResponse, "$.id")
    }

    @Test
    fun `should process IN transaction and update balance`() {
        val accountId = createAccountAndGetId()

        mockMvc.perform(
            post("/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"IN\", \"amount\": 100.50}")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.amount").value(100.50))
            .andExpect(jsonPath("$.direction").value("IN"))
            .andExpect(jsonPath("$.created").isNotEmpty)

        // Verify balance
        mockMvc.perform(get("/accounts/$accountId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(100.50))
    }

    @Test
    fun `should process OUT transaction and update balance`() {
        val accountId = createAccountAndGetId()

        // Top up first
        mockMvc.perform(
            post("/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"IN\", \"amount\": 200.0}")
        )
            .andExpect(status().isCreated)

        // Withdraw
        mockMvc.perform(
            post("/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"OUT\", \"amount\": 50.0}")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.amount").value(50.0))
            .andExpect(jsonPath("$.direction").value("OUT"))

        // Verify balance
        mockMvc.perform(get("/accounts/$accountId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(150.0))
    }

    @Test
    fun `should return 428 Precondition Required when processing OUT transaction with insufficient funds`() {
        val accountId = createAccountAndGetId()

        // Attempt to withdraw without topping up
        mockMvc.perform(
            post("/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"OUT\", \"amount\": 50.0}")
        )
            .andExpect(status().isPreconditionRequired)
            .andExpect(jsonPath("$.detail").value("Insufficient funds"))
    }

    @Test
    fun `should get account transactions`() {
        val accountId = createAccountAndGetId()

        // Initially empty
        mockMvc.perform(get("/accounts/$accountId/transactions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isEmpty)

        // Top up
        mockMvc.perform(
            post("/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"IN\", \"amount\": 100.0}")
        )
            .andExpect(status().isCreated)

        // Get transactions
        mockMvc.perform(get("/accounts/$accountId/transactions"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].amount").value(100.0))
            .andExpect(jsonPath("$[0].direction").value("IN"))
    }

    @Test
    fun `should return 404 when processing transaction for non-existent account`() {
        mockMvc.perform(
            post("/accounts/non-existent-id/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"IN\", \"amount\": 100.0}")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("No such account"))
    }

    @Test
    fun `should return 404 when getting transactions for non-existent account`() {
        mockMvc.perform(get("/accounts/non-existent-id/transactions"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("No such account"))
    }
}
