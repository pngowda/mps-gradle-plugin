package de.itemis.mps.gradle

import groovy.xml.MarkupBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class GenerateLibrariesXml extends DefaultTask {
    @InputFile
    File defaults

    File overrides

    @OutputFile
    File destination

    GenerateLibrariesXml() {
        description = 'Generates libraries.xml for MPS'
    }

    void setOverrides(Object overrides) {
        this.overrides = project.file(overrides)
        if (overrides != null) {
            inputs.file(overrides)
        }
    }

    @TaskAction
    def generate() {
        Properties properties = new Properties()
        defaults.withInputStream { properties.load(it) }
        if (overrides.exists()) {
            overrides.withInputStream { properties.load(it) }
        }
        destination.withWriter { writer ->
            def xml = new MarkupBuilder(writer)
            xml.project(version: 4) {
                component(name: 'ProjectLibraryManager') {
                    option(name: 'libraries') {
                        map() {
                            for (key in properties.keySet().toSorted()) {
                                entry(key: key) {
                                    value() {
                                        Library() {
                                            option(name: 'name', value: key)
                                            option(name: 'path', value: properties[key])
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
