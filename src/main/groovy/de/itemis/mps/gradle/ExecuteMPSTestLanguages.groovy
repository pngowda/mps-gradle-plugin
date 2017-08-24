class ExecuteMPSTestLanguages extends ExecuteMPSGeneratedAntScript {
    ExecuteMPSTestLanguages() {
        targets 'clean', 'generate', 'assemble', 'check'
    }
}
