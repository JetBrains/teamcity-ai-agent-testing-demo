import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.projectFeatures.awsConnection

version = "2025.03"

project {

    subProject(SWE_Bench_Lite)
    subProject(JetBrain_Junie_AI_Agent)
    subProject(Google_Gemini_CLI_AI_Agent)

    features {
        awsConnection() {
            id = "AiAgentTesting_BuildfarmAws"
            name = "Buildfarm AWS"
            regionName = "eu-west-1"
            credentialsType = static {
                accessKeyId = "AKIARLPX3CIT7X546WXD"
                secretAccessKey = "credentialsJSON:10a5256d-4fb6-47d6-9900-4092445cc797"
            }
            allowInBuilds = false
            stsEndpoint = "https://sts.eu-west-1.amazonaws.com"
        }

        amazonEC2CloudProfile {
            id = "amazon-4"
            name = "DemoAgents"
            terminateIdleMinutes = 15
            region = AmazonEC2CloudProfile.Regions.EU_WEST_DUBLIN
            awsConnectionId = "AiAgentTesting_BuildfarmAws"
        }

        amazonEC2CloudImage {
            id = "PROJECT_EXT_57"
            profileId = "amazon-4"
            name = "DemoAgent4Cpu16Gb (A)"
            customizeLaunchTemplate = true
            source = LaunchTemplate(templateId = "lt-005ed01c6ea5ac26d", version = AmazonEC2CloudImage.LATEST_VERSION)
        }

        amazonEC2CloudImage {
            id = "PROJECT_EXT_58"
            profileId = "amazon-4"
            name = "DemoAgent4Cpu16Gb (B)"
            customizeLaunchTemplate = true
            source = LaunchTemplate(templateId = "lt-0719b0d941ad9aa6c", version = AmazonEC2CloudImage.DEFAULT_VERSION)
        }

        amazonEC2CloudImage {
            id = "PROJECT_EXT_59"
            profileId = "amazon-4"
            name = "DemoAgent4Cpu16Gb (C)"
            customizeLaunchTemplate = true
            source = LaunchTemplate(templateId = "lt-0d189a202db3b8d79", version = AmazonEC2CloudImage.DEFAULT_VERSION)
        }

        sharedResource {
            id = "HuggingFaceConnections"
            name = "HuggingFaceConnection"
            resourceType = quoted(20)
        }

        buildTypeCustomChart {
            id = "TaskSolvingSuccessRateChart"
            title = "Task Solving Success Rate"
            seriesTitle = "Serie"
            format = CustomChart.Format.TEXT
            series = listOf(
                CustomChart.Serie(title = "Success Rate", key = CustomChart.SeriesKey("SWEBLite_10x_SuccessRate"))
            )
        }

        buildTypeChartsOrder {
            id = "ChartsOrder"
            order = listOf(
                "TaskSolvingSuccessRateChart",
                "_Root:SuccessRate",
                "_Root:TestCount",
                "_Root:InspectionStats",
                "_Root:CodeCoverage",
                "_Root:MaxTimeToFixTestGraph",
                "_Root:BuildDurationNetTime",
                "_Root:TimeSpentInQueue",
                "_Root:VisibleArtifactsSize"
            )
        }
    }
}
