package cryptosim.api;

import io.javalin.Javalin;

public final class Server {

    private static final int DEFAULT_PORT = 7000;

    public static void main(String[] args) {
        int port = resolvePort();
        Javalin app = createApp();
        app.start(port);

        System.out.println("Crypto Simulator API running on http://localhost:" + port);
    }

    public static Javalin createApp() {
        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(rule -> rule.anyHost());
            });
            config.staticFiles.add("/public", io.javalin.http.staticfiles.Location.CLASSPATH);
        });

        SimulationController controller = new SimulationController();
        app.post("/simulate", controller::runSimulation);

        return app;
    }

    private static int resolvePort() {
        String envPort = System.getenv("PORT");
        if (envPort == null) {
            return DEFAULT_PORT;
        }
        try {
            return Integer.parseInt(envPort);
        } catch (NumberFormatException e) {
            System.err.println("Invalid PORT env value: " + envPort + ", using default");
            return DEFAULT_PORT;
        }
    }
}
