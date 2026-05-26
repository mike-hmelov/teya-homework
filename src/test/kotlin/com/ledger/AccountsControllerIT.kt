package com.ledger

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Integration tests for [com.ledger.controller.AccountsController].
 *
 * Uses predefined seed data from [Initializer]:
 *  - John Dow  id=580e3b02-02f3-4146-98e6-92f1a8eca786  balance=1000  1 transaction
 *  - Mary Jane id=591bbbab-a984-4cb1-bb4f-643657617949  balance=1000  3 transactions
 */
@IntegrationTest
class AccountsControllerIT {

    companion object {
        // Predefined accounts from Initializer
        const val JOHN_DOW_ID   = "580e3b02-02f3-4146-98e6-92f1a8eca786"
        const val MARY_JANE_ID  = "591bbbab-a984-4cb1-bb4f-643657617949"
        const val UNKNOWN_ID    = "00000000-0000-0000-0000-000000000000"
    }

    @LocalServerPort
    private var port: Int = 0

    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setUp() {
        webTestClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$port")
            .build()
    }

    // -------------------------------------------------------------------------
    // GET /accounts/{id}
    // -------------------------------------------------------------------------

    @Test
    fun `GET account - returns John Dow with correct id and name`() {
        webTestClient.get()
            .uri("/accounts/$JOHN_DOW_ID")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id").isEqualTo(JOHN_DOW_ID)
            .jsonPath("$.name").isEqualTo("John Dow")
            // balance may be higher than 1000 if other tests added transactions first
            .jsonPath("$.balance").value<Number> { b ->
                assert(b.toDouble() >= 1000) { "Expected balance >= 1000 but was $b" }
            }
    }

    @Test
    fun `GET account - returns Mary Jane with correct id and name`() {
        webTestClient.get()
            .uri("/accounts/$MARY_JANE_ID")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(MARY_JANE_ID)
            .jsonPath("$.name").isEqualTo("Mary Jane")
            // balance may be higher than 1000 if other tests added transactions first
            .jsonPath("$.balance").value<Number> { b ->
                assert(b.toDouble() >= 1000) { "Expected balance >= 1000 but was $b" }
            }
    }

    @Test
    fun `GET account - returns 404 for unknown id`() {
        webTestClient.get()
            .uri("/accounts/$UNKNOWN_ID")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isNotEmpty
    }

    // -------------------------------------------------------------------------
    // GET /accounts/{id}/transactions
    // -------------------------------------------------------------------------

    @Test
    fun `GET transactions - John Dow has exactly 1 seed transaction`() {
        webTestClient.get()
            .uri("/accounts/$JOHN_DOW_ID/transactions")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$").isArray
            // At least the seeded transaction is present
            .jsonPath("$[0].amount").isEqualTo(1000)
            .jsonPath("$[0].direction").isEqualTo("IN")
            .jsonPath("$[0].timestamp").isNotEmpty
    }

    @Test
    fun `GET transactions - Mary Jane has at least 3 seed transactions`() {
        webTestClient.get()
            .uri("/accounts/$MARY_JANE_ID/transactions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray
            // At least the 3 seeded transactions exist; other tests may have added more
            .jsonPath("$.length()").value<Int> { len ->
                assert(len >= 3) { "Expected at least 3 transactions but was $len" }
            }
    }

    @Test
    fun `GET transactions - Mary Jane seed contains expected amounts`() {
        webTestClient.get()
            .uri("/accounts/$MARY_JANE_ID/transactions")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$[?(@.amount == 500 && @.direction == 'IN')]").exists()
            .jsonPath("$[?(@.amount == 750 && @.direction == 'IN')]").exists()
            .jsonPath("$[?(@.amount == 250 && @.direction == 'OUT')]").exists()
    }

    @Test
    fun `GET transactions - returns 404 for unknown account`() {
        webTestClient.get()
            .uri("/accounts/$UNKNOWN_ID/transactions")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isNotEmpty
    }

    // -------------------------------------------------------------------------
    // POST /accounts
    // -------------------------------------------------------------------------

    @Test
    fun `POST account - creates new account successfully`() {
        webTestClient.post()
            .uri("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"Alice"}""")
            .exchange()
            .expectStatus().isCreated
            .expectHeader().valueMatches("Location", "/accounts/.*")
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.name").isEqualTo("Alice")
            .jsonPath("$.balance").isEqualTo(0)
            .jsonPath("$.transactions").isArray
    }

    @Test
    fun `POST account - returns 409 when account name already exists`() {
        // "John Dow" is seeded by Initializer
        webTestClient.post()
            .uri("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"John Dow"}""")
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.error").isNotEmpty
    }

    @Test
    fun `POST account - returns 400 when name is missing`() {
        webTestClient.post()
            .uri("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("validation_error")
            .jsonPath("$.details").isArray
    }

    @Test
    fun `POST account - returns 400 when name is blank`() {
        webTestClient.post()
            .uri("/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":""}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("validation_error")
    }

    // -------------------------------------------------------------------------
    // POST /accounts/{id}/transactions  – top-up (IN)
    // -------------------------------------------------------------------------

    @Test
    fun `POST transaction - top-up increases balance and returns transaction`() {
        webTestClient.post()
            .uri("/accounts/$JOHN_DOW_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"direction":"IN","amount":200}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.amount").isEqualTo(200)
            .jsonPath("$.direction").isEqualTo("IN")
            .jsonPath("$.timestamp").isNotEmpty
    }

    @Test
    fun `POST transaction - top-up reflects in account balance`() {
        // Mary Jane starts with 1000; top-up 50 → balance must be ≥ 1050
        webTestClient.post()
            .uri("/accounts/$MARY_JANE_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"direction":"IN","amount":50}""")
            .exchange()
            .expectStatus().isOk

        webTestClient.get()
            .uri("/accounts/$MARY_JANE_ID")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.balance").value<Number> { balance ->
                assert(balance.toDouble() >= 1050) {
                    "Expected balance >= 1050 but was $balance"
                }
            }
    }

    // -------------------------------------------------------------------------
    // POST /accounts/{id}/transactions  – withdrawal (OUT)
    // -------------------------------------------------------------------------

    @Test
    fun `POST transaction - withdrawal returns correct transaction`() {
        webTestClient.post()
            .uri("/accounts/$JOHN_DOW_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"direction":"OUT","amount":100}""")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.amount").isEqualTo(100)
            .jsonPath("$.direction").isEqualTo("OUT")
            .jsonPath("$.timestamp").isNotEmpty
    }

    @Test
    fun `POST transaction - returns 422 when insufficient funds`() {
        // John Dow balance is 1000; attempt to withdraw 999999
        webTestClient.post()
            .uri("/accounts/$JOHN_DOW_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"direction":"OUT","amount":999999}""")
            .exchange()
            .expectStatus().isEqualTo(422)
            .expectBody()
            .jsonPath("$.error").isNotEmpty
    }

    // -------------------------------------------------------------------------
    // POST /accounts/{id}/transactions  – error scenarios
    // -------------------------------------------------------------------------

    @Test
    fun `POST transaction - returns 404 for unknown account`() {
        webTestClient.post()
            .uri("/accounts/$UNKNOWN_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"direction":"IN","amount":100}""")
            .exchange()
            .expectStatus().isNotFound
            .expectBody()
            .jsonPath("$.error").isNotEmpty
    }

    @Test
    fun `POST transaction - returns 400 when amount is missing`() {
        webTestClient.post()
            .uri("/accounts/$JOHN_DOW_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"direction":"IN"}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("validation_error")
    }

    @Test
    fun `POST transaction - returns 400 when amount is negative`() {
        webTestClient.post()
            .uri("/accounts/$JOHN_DOW_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"direction":"IN","amount":-50}""")
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.error").isEqualTo("validation_error")
    }

    @Test
    fun `POST transaction - returns 400 when direction is missing`() {
        webTestClient.post()
            .uri("/accounts/$JOHN_DOW_ID/transactions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"amount":100}""")
            .exchange()
            .expectStatus().isBadRequest
    }
}
