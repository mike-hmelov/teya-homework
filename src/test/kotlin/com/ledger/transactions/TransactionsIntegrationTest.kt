package com.ledger.transactions

import com.jayway.jsonpath.JsonPath
import com.ledger.IntegrationTest
import com.ledger.accounts.dao.AccountsRepository
import com.ledger.transactions.dao.TransactionsRepository
import org.junit.jupiter.api.Assertions.assertTrue
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
class TransactionsIntegrationTest {

    @Autowired
    lateinit var context: WebApplicationContext

    @Autowired
    lateinit var accountsRepository: AccountsRepository

    @Autowired
    lateinit var transactionsRepository: TransactionsRepository

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `should get transaction by id`() {
        // Using predefined transaction from Mary Jane's account
        val transactionId = "591bbbab-a984-4cb1-bb4f-643657617948"

        mockMvc.perform(get("/transactions/$transactionId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(transactionId))
            .andExpect(jsonPath("$.amount").value(500.0))
            .andExpect(jsonPath("$.direction").value("IN"))
    }

    @Test
    fun `should return 404 when getting non-existent transaction`() {
        mockMvc.perform(get("/transactions/non-existent-id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("No such transaction"))
    }

    @Test
    fun `should search transactions by direction`() {
        // Search IN - Should find 3 transactions (1 from John, 2 from Mary)
        var result = mockMvc.perform(
            post("/transactions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"IN\"}")
        )
            .andExpect(status().isOk)
            .andReturn()

        var directions = JsonPath.read<List<String>>(result.response.contentAsString, "$[*].direction")
        assertTrue(directions.all { it == "IN" })

        // Search OUT - Should find 1 transaction (Mary)
        result = mockMvc.perform(
            post("/transactions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"OUT\"}")
        )
            .andExpect(status().isOk)
            .andReturn()

        directions = JsonPath.read(result.response.contentAsString, "$[*].direction")
        assertTrue(directions.all { it == "OUT" })
    }

    @Test
    fun `should search transactions by amount range`() {
        // Min amount
        var result = mockMvc.perform(
            post("/transactions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minAmount\": 600.0}")
        )
            .andExpect(status().isOk)
            .andReturn()

        var amounts = JsonPath.read<List<Number>>(result.response.contentAsString, "$[*].amount")
        assertTrue(amounts.all { it.toDouble() >= 600.0 })

        // Max amount
        result = mockMvc.perform(
            post("/transactions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"maxAmount\": 400.0}")
        )
            .andExpect(status().isOk)
            .andReturn()

        amounts = JsonPath.read(result.response.contentAsString, "$[*].amount")
        assertTrue(amounts.all { it.toDouble() <= 400.0 })

        // Min and Max amount
        result = mockMvc.perform(
            post("/transactions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"minAmount\": 400.0, \"maxAmount\": 800.0}")
        )
            .andExpect(status().isOk)
            .andReturn()

        amounts = JsonPath.read(result.response.contentAsString, "$[*].amount")
        assertTrue(amounts.all { it.toDouble() in 400.0..800.0 })
    }

    @Test
    fun `should paginate search results`() {
        // Total predefined transactions: 4
        mockMvc.perform(
            post("/transactions/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"limit\": 2, \"offset\": 1}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }
}
