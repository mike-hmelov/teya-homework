package com.ledger.accounts

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
class AccountsIntegrationTest {

    @Autowired
    lateinit var context: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build()
    }

    @Test
    fun `should create a new account successfully`() {
        val randomName = "test_account_${System.currentTimeMillis()}"
        mockMvc.perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"$randomName\"}")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNotEmpty)
            .andExpect(jsonPath("$.name").value(randomName))
            .andExpect(jsonPath("$.balance").value(0))
    }

    @Test
    fun `should return 409 Conflict when creating an account that already exists`() {
        // Create first account
        mockMvc.perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"duplicate_account\"}")
        )
            .andExpect(status().isCreated)

        // Try to create the same account again
        mockMvc.perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"duplicate_account\"}")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.detail").value("Account with this name already exists"))
    }

    @Test
    fun `should get an existing account by id`() {
        // Create first account and extract id
        val result = mockMvc.perform(
            post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"existing_account\"}")
        )
            .andExpect(status().isCreated)
            .andReturn()

        val jsonResponse = result.response.contentAsString
        val id = jsonResponse.substringAfter("\"id\":\"").substringBefore("\"")

        // Get the account by id
        mockMvc.perform(get("/accounts/$id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.name").value("existing_account"))
            .andExpect(jsonPath("$.balance").value(0))
    }

    @Test
    fun `should return 404 Not Found when getting a non-existent account`() {
        mockMvc.perform(get("/accounts/non-existent-id"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.detail").value("Not such account"))
    }
}
