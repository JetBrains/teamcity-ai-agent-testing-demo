package utilities

import jetbrains.buildServer.configs.kotlin.BuildFeatures
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests


fun BuildFeatures.pullRequestsFeature() {
    pullRequests {
        vcsRootExtId = "Matterhorn_CoreCi_HttpsGithubComJetBrainsMatterhornCore"
        provider = github {
            authType = vcsRoot()
            filterAuthorRole = PullRequests.GitHubRoleFilter.EVERYBODY
        }
    }
}


fun BuildFeatures.loginToSpaceRegistryFeature() {
    dockerSupport {
        loginToRegistry = on {
            dockerRegistryId = "PROJECT_EXT_5596"
        }
    }
}
