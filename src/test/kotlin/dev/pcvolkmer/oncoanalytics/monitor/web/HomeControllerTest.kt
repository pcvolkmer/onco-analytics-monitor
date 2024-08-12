package dev.pcvolkmer.oncoanalytics.monitor.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

@WebFluxTest(controllers = [HomeController::class])
class HomeControllerTest {

    private lateinit var webClient: WebTestClient

    @BeforeEach
    fun setup(
        @Autowired webClient: WebTestClient,
    ) {
        this.webClient = webClient
    }

    @Test
    fun shouldGetStartPage() {
        webClient
            .get().uri("/")
            .exchange()
            .expectAll(
                { spec -> spec.expectStatus().isOk },
                { spec -> spec.expectHeader().contentType(MediaType.TEXT_HTML) },
                { spec ->
                    spec.expectBody()
                        .consumeWith {
                            assertThat(it.responseBody).isNotNull()
                            assertThat(String(it.responseBody!!)).contains("<h1>onco-analytics-monitor</h1>")
                        }
                }
            )
    }

}