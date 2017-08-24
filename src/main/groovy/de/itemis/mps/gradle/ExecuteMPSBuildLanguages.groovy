class ExecuteMPSBuildLanguages extends ExecuteMPSGeneratedAntScript {
    ExecuteMPSBuildLanguages() {
        targets 'clean', 'generate', 'assemble'
    }
}