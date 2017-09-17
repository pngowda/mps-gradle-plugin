package de.itemis.mps.gradle

class ExecuteMPSBuildLanguages extends ExecuteMPSGeneratedAntScript {
    ExecuteMPSBuildLanguages() {
        targets 'clean', 'generate', 'assemble'
    }
}