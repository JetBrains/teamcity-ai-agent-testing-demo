//package utilities.builds.ci
//
//import jetbrains.buildServer.configs.kotlin.Project
//import jetbrains.buildServer.configs.kotlin.buildSteps.script
//import utilities.agentRequirements
//import utilities.matterhornCoreGitHubRepo
//import utilities.runIdParam
//import utilities.sweBenchGitRepo
//
//
//fun Project.checkRedisCache(instanceIds: List<String>) {
//    buildType {
//        id("Check_Redis_Cache")
//        name = "Check Redis cache"
//
//        params {
//            param("reverse.dep.*.env.MATTERHORN_CAN_WRITE_TO_REDIS", "false")
//            param("env.TEAMCITY_TOKEN", "%TeamCityToken%")
//            param("env.TEAMCITY_SERVER_URL", "%teamcity.serverUrl%")
//            runIdParam()
//        }
//
//        sweBenchGitRepo(SWE_BENCH_DIR)
//        matterhornCoreGitHubRepo()
//
//        artifactRules = """
//            artifacts/**/* => .
//        """.trimIndent()
//
//        steps {
//            script {
//                name = "Check Redis cache"
//                workingDir = SWE_BENCH_DIR
//                scriptContent = """
//                    #!/bin/bash
//                    set -e
//                    python3 -m venv .venv
//                    source .venv/bin/activate
//                    pip install -r requirements.txt
//                    python -m scripts.ci.check_redis_cache --build_id="%teamcity.build.id%" --run_id="%teamcity.run.id%"
//                """.trimIndent()
//            }
//        }
//
//        executionDeps(instanceIds)
//        agentRequirements()
//
//        commitStatusPublisherFeature()
//        pullRequestsFeature()
//        vcsTrigger()
//    }
//}