package cryptosim.api;

import cryptosim.simulation.SimulationConfig;
import cryptosim.simulation.SimulationResult;
import cryptosim.simulation.Simulator;
import io.javalin.http.Context;

public final class SimulationController {
    public void runSimulation(Context ctx) throws InterruptedException {
        SimulationConfig config = ctx.bodyAsClass(SimulationConfig.class);
        SimulationResult result = new Simulator(config).run();
        ctx.json(result);
    }
}
