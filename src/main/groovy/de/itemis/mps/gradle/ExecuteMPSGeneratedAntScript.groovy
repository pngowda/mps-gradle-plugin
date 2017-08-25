import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.apache.tools.ant.Project
import org.apache.tools.ant.ProjectHelper
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class RunMbeddrAntScript extends DefaultTask {
    Object script
    Object cmdargs
    List<String> targets = Collections.emptyList()
    def propertyMap=[keyA: 'valueA']
    def writePropMap=[keyB: 'valueB']
    def inputFileList=new ArrayList()
    def outputFileList=new ArrayList()
    List<File> resolvedInputPath =new ArrayList<File>()
    List<File> resolvedOutputPath =new ArrayList<File>()
    boolean isIncremental=true

    def targets(String... targets) {
        this.targets = Arrays.asList(targets)
    }

    @InputFiles
    def getInputFiles(){
        def buildFilePath= project.file(script)
        def ioFile=new File(buildFilePath.getParent()+"\\"+buildFilePath.getName().split("\\.")[0]+"_Input_Output.xml")
        if(ioFile.exists()){
            getProperties(buildFilePath)
            parseAntBuildXmlFileInput(ioFile)
        }

        FileCollection files = getProject().files();
        for (f in resolvedInputPath ) {
            files = files.plus(getProject().fileTree(new File(f)));
        }
        return files;
    }

    @OutputFiles
    def getOutputFiles(){
        def buildFilePath= project.file(script)
        def ioFile=new File(buildFilePath.getParent()+"\\"+buildFilePath.getName().split("\\.")[0]+"_Input_Output.xml")
        if(ioFile.exists()){
            getProperties(buildFilePath)
            parseAntBuildXmlFileOutput(ioFile)
        }
        FileCollection files = getProject().files();
        for (f in resolvedOutputPath ) {
            files = files.plus(getProject().fileTree(new File(f)));
        }
        return files;
    }

    def getProperties(ioFilePath){
        def antProject = new Project()
        ProjectHelper.configureProject(antProject, ioFilePath)
        propertyMap = antProject.getProperties()
        propertyMap.each { keyA, valueA -> writePropMap.put("\${" + "$keyA" + "}", "$valueA") }
    }

    def parseAntBuildXmlFileInput(ioFilePath){
        def parsedProjectXml = new XmlSlurper().parse(ioFilePath)
        parsedProjectXml.target.input.file.each { file -> inputFileList.add(file.@path) }
        inputFileList.each {
            if (it.toString().contains("\$")) {
                def toResolveString = it.toString().split("/").getAt(0)
                def resolvedPath = it.toString().replace(toResolveString, writePropMap.get(toResolveString))
                resolvedInputPath.add(resolvedPath)
            }
        }
    }

    def parseAntBuildXmlFileOutput(ioFilePath){
        def parsedProjectXml = new XmlSlurper().parse(ioFilePath)
        parsedProjectXml.target.output.file.each { file -> outputFileList.add(file.@path) }
        outputFileList.each {
            if (it.toString().contains("\$")) {
                def toResolveString = it.toString().split("/").getAt(0)
                def resolvedPath = it.toString().replace(toResolveString, writePropMap.get(toResolveString))
                resolvedOutputPath.add(resolvedPath)
            }
        }
    }

    @TaskAction
    def build(IncrementalTaskInputs inputs) {
        println "boolean value: "+isIncremental
        println inputs.incremental ? "CHANGED inputs considered out of date"
                                   : "ALL inputs considered out of date"
        if (!inputs.incremental) {
            isIncremental=false
        }
        inputs.outOfDate { change ->
            println "out of date: ${change.file.name}"
        }
        inputs.removed { change ->
            println "removed: ${change.file.name}"
        }
        if(!isIncremental) {
            spawnAnt()
        } else {
            println "nothing changed so skipping the task"
        }

    }

    def spawnAnt() {
        project.javaexec {
            main 'org.apache.tools.ant.launch.Launcher'
            workingDir project.rootDir

            classpath project.configurations.ant_lib.fileCollection({
                true
            }) + project.files("$project.jdk_home/lib/tools.jar")

            args(*['-verbose', *cmdargs , '-buildfile', project.file(script), *targets])
        }
    }
}
