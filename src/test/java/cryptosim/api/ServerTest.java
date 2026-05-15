package cryptosim.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerTest {
    private static Javalin app;
    private static int port;
    private static HttpClient client;

    @BeforeAll
    static void startServer() {
        app = Server.createApp();
        app.start(0);
        port = app.port();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopServer() {
        app.stop();
    }

    @Test
    void postSimulate_validConfig_returnsResultWithStats() throws Exception {
        String requestJson = """
                {
                  "seed": 42,
                  "numWallets": 5,
                  "initialBalancePerWallet": 1000,
                  "difficultyBits": 12,
                  "durationMs": 500,
                  "transactionIntervalMs": 50,
                  "blockIntervalMs": 100,
                  "mempoolMaxSize": 100,
                  "maxTransactionsPerBlock": 10
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/simulate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode body = mapper.readTree(response.body());

        assertTrue(body.has("blockchain"), "Response should contain blockchain");
        assertTrue(body.has("wallets"), "Response should contain wallets");
        assertTrue(body.has("minerAddress"), "Response should contain minerAddress");
        assertTrue(body.has("stats"), "Response should contain stats");

        JsonNode stats = body.get("stats");
        assertTrue(stats.get("totalBlocks").asLong() > 0, "Should have mined blocks");
        assertTrue(stats.get("submittedTransactions").asLong() > 0, "Should have submitted txs");

        System.out.println("[ServerTest] Sample response stats:");
        System.out.println("  Total blocks:      " + stats.get("totalBlocks").asLong());
        System.out.println("  Submitted:         " + stats.get("submittedTransactions").asLong());
        System.out.println("  Confirmed:         " + stats.get("confirmedTransactions").asLong());
        System.out.println("  Total fees:        " + stats.get("totalFeesPaid").asLong());
    }

    @Test
    void postSimulate_invalidConfig_returnsErrorStatus() throws Exception {
        String invalidJson = """
                {
                  "seed": 42,
                  "numWallets": 1,
                  "initialBalancePerWallet": 1000,
                  "difficultyBits": 12,
                  "durationMs": 500,
                  "transactionIntervalMs": 50,
                  "blockIntervalMs": 100,
                  "mempoolMaxSize": 100,
                  "maxTransactionsPerBlock": 10
                }
                """;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/simulate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertTrue(response.statusCode() >= 400,
                "Expected error status, got " + response.statusCode());
    }
}
