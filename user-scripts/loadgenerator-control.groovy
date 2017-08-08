import jenkins.plugin.randomjobbuilder.LoadGeneration;

LoadGeneration.GeneratorController controller = LoadGeneration.getGeneratorController();
controller.setAutostart(true);

// Start all generators
for (LoadGeneration.LoadGenerator lg : controller.registeredGenerators.values()) {
    lg.start();
}

// Stop all load generators -- jobs still have to complete normally though
/*for (LoadGeneration.LoadGenerator lg : controller.registeredGenerators.values()) {
    lg.stop();
}*/

// Forcible halt all load generators and their tasks
/*
for (LoadGeneration.LoadGenerator lg : controller.registeredGenerators.values()) {
    controller.stopAbruptly(lg);
}
*/