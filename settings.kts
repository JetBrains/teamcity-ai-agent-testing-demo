import data.*
import jetbrains.buildServer.configs.kotlin.project
import jetbrains.buildServer.configs.kotlin.version
import jetbrains.buildServer.configs.kotlin.ParameterDisplay

import utilities.*
import utilities.builds.InitBuild

version = "2024.12"

project {

    params {
        param("script", "with_test_patch_with_golden_patch.sh")
        param("env.MATTERHORN_REDIS", "electrojun-wsgl0v.serverless.euw1.cache.amazonaws.com:6379")
        param("env.MATTERHORN_DEFAULT_MODEL_PROVIDER", "OpenAI")
        param("env.TIMEOUT_FOR_INDEXING_MINUTES", "3")
        param("env.REPLACE_PATH_TO_PROJECT_BY", "/Users/JetBrainsElectricJunior__/Work/")
//      token name: matterhorn-tc-swepython
        param("env.YT_PROXY", "%env.YT_NEUMANN_PROXY%")
        param("env.YT_TOKEN", "%env.YT_NEUMANN_TOKEN%")
        param("env.YT_CONFIG_PATCHES", "{proxy={force_ipv4=%true}}")
        text("env.IS_TEAMCITY_EVALUATION", "true", display = ParameterDisplay.HIDDEN)
    }

//    matterhornCoreCI()
    sliceAggregator("ValidateDevSlice", "Validate dev instances", devFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.VALIDATION)
    sliceAggregator("ValidateTestSlice", "Validate test instances", testData.mapNotNull { it["instance_id"] }, SliceAggregatorType.VALIDATION)
    sliceAggregator("ValidateTestLiteSlice", "Validate test-lite instances", testLiteFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.VALIDATION)
    sliceAggregator("ValidateExtendedSlice", "Validate swebench extended instances", extendedFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.VALIDATION)
    sliceAggregator("ValidateTestVerified", "Validate swebench test verified 500 instances", testVerifiedFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.VALIDATION)
    sliceAggregator("ValidateBadTask", "Validate swebench bad instances", badTestVerified + badIdsTestLite + badIdsDev, SliceAggregatorType.VALIDATION)
    sliceAggregator("ValidatePublicDockerImageTasks", "Validate public docker image tasks", publicImageDockerizedInstanceIds, SliceAggregatorType.VALIDATION)

    sliceAggregator("RunTestLite", "Run agent and evaluation on test-lite instances", testLiteFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunTestLiteO1", "Run agent and evaluation on test-lite instances with o1-model", testLiteFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.EVALUATION_O1)

    sliceAggregator("RunExtended", "Run agent and evaluation on swebench extended instances", extendedFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.EVALUATION_O1)
    sliceAggregator("RunExtendedFirst300", "Run agent and evaluation on 300 swebench extended instances", extendedFilteredInstances.mapNotNull { it["instance_id"] }.take(300), SliceAggregatorType.EVALUATION)
    sliceAggregator("RunExtendedSecond300", "Run agent and evaluation on 300 swebench extended instances from 301 to 600", extendedFilteredInstances.mapNotNull { it["instance_id"] }.drop(300).take(300), SliceAggregatorType.EVALUATION)
    sliceAggregator("RunExtendedWithGoldFirst300", "Run agent and evaluation on 300 swebench extended instances with gold", extendedWithGoldFilteredInstances.mapNotNull { it["instance_id"] }.take(300), SliceAggregatorType.EVALUATION)
    sliceAggregator("RunExtendedWithGoldSecond300", "Run agent and evaluation on 300 swebench extended instances from 301 to 600 with gold", extendedWithGoldFilteredInstances.mapNotNull { it["instance_id"] }.drop(300).take(300), SliceAggregatorType.EVALUATION)
    sliceAggregator("RunExtendedRandom100", "Run agent and evaluation on 100 random swebench extended instances", extendedFilteredInstances.mapNotNull { it["instance_id"] }.shuffled().take(100), SliceAggregatorType.EVALUATION_O1)
    sliceAggregator("RunExtended100", "Run agent and evaluation on 100 specific swebench extended instances", extended100.toList(), SliceAggregatorType.EVALUATION_O1)
    sliceAggregator("RunPydanticLast16", "Run agent and evaluation on last 16 issues in pydantic repo", pydanticLast16.toList(), SliceAggregatorType.EVALUATION)
    sliceAggregator("RunPydanticFirst100", "Run agent and evaluation on first 100 issues in pydantic repo", pydanticFirst100.toList(), SliceAggregatorType.EVALUATION)

    sliceAggregator("RunDev", "Run agent and evaluation on dev instances", devFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunDevO1", "Run agent and evaluation on dev instances with o1-model", devFilteredInstances.mapNotNull { it["instance_id"] }, SliceAggregatorType.EVALUATION_O1)
    sliceAggregator("RunDevSub35", "Run agent and evaluation on 35 dev instances", devSubResolved35, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunDevSub10", "Run agent and evaluation on 10 dev instances", devSub10, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunDevSub95", "Run agent and evaluation on 95 dev instances. New", devSubResolved95, SliceAggregatorType.EVALUATION)
    sliceAggregatorMatrix("RunDevSub95x3", "Run agent and evaluation on 95 dev instances 3 times per instance. New", devSubResolved95, SliceAggregatorType.EVALUATION, 3)

    sliceAggregator("RunTestVerified50", "Run agent and evaluation on 10 verified test instances", testVerified10, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunTestVerified399", "Run agent and evaluation on 399 verified test instances. New", testVerified399, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunTestVerified280", "Run agent and evaluation on 280 verified test instances. New2", testVerified280, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunTestVerified280Quota", "Run agent and evaluation on 280 verified test instances with quota. New2", testVerified280, SliceAggregatorType.EVALUATION_O1)
    sliceAggregator("RunTestVerified500", "Run agent and evaluation on 500 verified test instances", testVerified500.filterNot { it in badTestVerified }, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunTestVerified399Quota", "Run agent and evaluation on 399 verified test instances with quota. New", testVerified399, SliceAggregatorType.EVALUATION_O1)
    sliceAggregator("RunTestVerified500Quota", "Run agent and evaluation on 500 verified test instances with quota", testVerified500.filterNot { it in badTestVerified }, SliceAggregatorType.EVALUATION_O1)

    sliceAggregator("RunTestPerplexityMode", "Run agent on PerplexityMode instances in CHAT mode", testPerplexityMode, SliceAggregatorType.EXECUTION)
    sliceAggregator("RunTestPerplexityModeAgent", "Run agent on PerplexityMode instances in ISSUE mode", testPerplexityModeAgent, SliceAggregatorType.EXECUTION)

    sliceAggregator("CreateCaches4CI7", "zzz. Create caches for CI check", tasksForCICheck, SliceAggregatorType.EVALUATION)
    sliceAggregator("RunTestPublicDockerImage", "Run agent and evaluation on tasks with public docker image instances", publicImageDockerizedInstanceIds, SliceAggregatorType.EVALUATION)

    subProject {
        id("SWEBench_Matterhorn_Instances_Executions")
        name = "Instances to validate"

        for (instance in devData + testData) {
            createBuildTypeForValidation(this, instance)
        }
    }

    subProject {
        id("SWEBench_Extended_Instances_Validate")
        name = "Instances Extended to validate"

        for (instance in extendedFilteredInstances) {
            createBuildTypeForValidation(this, instance)
        }
    }

    subProject {
        id("SWEBench_Matterhorn_Instances_ExecutionsPlusEvaluations")
        name = "Instances to execute and evaluate"

        for (instance in (testLiteFilteredInstances + devFilteredInstances + testVerifiedFilteredInstances + extendedFilteredInstances).toSet()) {
            createBuildTypeForEvaluation(this, instance)
        }
    }

    subProject {
        id("SWEBench_Extended_Instances_With_Gold_ExecutionsPlusEvaluations")
        name = "Instances with gold to execute and evaluate"

        for (instance in extendedWithGoldFilteredInstances) {
            createBuildTypeForEvaluation(this, instance)
        }
    }

    subProject {
        id("SWEBench_Extended_Instances_ExecutionsPlusEvaluations")
        name = "Instances to execute and evaluate extended"

        for (instance in extendedFilteredInstances) {
            createBuildTypeForEvaluation(this, instance, respectQuota = true)
        }
    }

    subProject {
        id("SWEBench_Matterhorn_Instances_ExecutionsPlusEvaluations_o1")
        name = "Instances to execute and evaluate o1"

        for (instance in (testLiteFilteredInstances + devFilteredInstances + testVerifiedFilteredInstances).toSet()) {
            createBuildTypeForEvaluation(this, instance, respectQuota = true)
        }
    }

    subProject {
        id("SWEBench_Matterhorn_Instances_ExecutionsPlusEvaluationsSeeds")
        name = "Instances to execute and evaluate seeds"
        val allInstances = (devFilteredInstances ).toSet()
        val neededInstanceIdsDev = (devSubResolved95).toSet()

        for (instance in allInstances.filter { neededInstanceIdsDev.contains(it["instance_id"]) }) {
            for (seed in 0..2) {
                createBuildTypeForEvaluation(this, instance, seed)
            }
        }
    }

    subProject {
        id("SWEBench_Matterhorn_PerplexityMode_Instances_Executions")
        name = "Perplexity mode instances to execute in CHAT mode"

        for (instance in perplexityData.toSet()) {
            createBuildTypeForExecution(this, instance)
        }
    }

    subProject {
        id("SWEBench_Matterhorn_PerplexityMode_Instances_Executions_Agent")
        name = "Perplexity mode instances to execute in ISSUE mode"

        for (instance in perplexityDataAgent.toSet()) {
            createBuildTypeForExecution(this, instance)
        }
    }

    this.buildType(InitBuild)
}
