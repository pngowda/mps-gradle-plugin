package de.itemis.mps.gradle

class ExecuteMPSTestLanguages extends ExecuteMPSGeneratedAntScript {
    ExecuteMPSTestLanguages() {
        targets 'clean', 'generate', 'assemble', 'check'
    }
}
