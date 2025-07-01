//package utilities.builds.ci
//
//import data.checkCacheInstanceIds
//import data.checkExecutionInstanceIds
//import jetbrains.buildServer.configs.kotlin.Project
//import utilities.SliceAggregatorType
//import utilities.sliceAggregator
//
//
//fun Project.matterhornCoreCI() {
//    subProject {
//        id("MatterhornCore_CI")
//        name = "Matterhorn Core CI"
//
//        checkAgentExecution(checkExecutionInstanceIds)
//        checkRedisCache(checkCacheInstanceIds)
//    }
//}