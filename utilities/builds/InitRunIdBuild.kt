package utilities.builds

import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildSteps.python
import utilities.generateId


object InitBuild : BuildType({
    id("InitBuild")
    name = "Init build to generate run id"
    params {
        param("tc_run_id", "%tc_run_id%")
    }
    steps {
        generateId("tc_run_id")
    }
})