package utilities

import data.dockerizedInstanceIds
import data.publicImageDockerizedInstanceIds
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerSupport
import java.util.regex.Pattern


private const val PREFIX = "env"
private const val IMAGE_TAG = "v0.0.3-fix-collisions"
private const val REGISTRY = "registry.jetbrains.team/p/matterhorn/swe-bench"

fun hasDockerImage(instanceId: String) = (dockerizedInstanceIds).contains(instanceId)

fun isValidDockerImageName(imageName: String): Boolean {
    // The path consists of slash-separated components (up to 2 components typically).
    // Each component:
    // 1. May contain lowercase letters, digits, periods,
    //    one or two underscores, or one or more hyphens.
    // 2. Cannot start or end with a separator (period, underscore, or hyphen).
    val componentPattern = "[a-z0-9]([a-z0-9._-]*[a-z0-9])?"

    // Full pattern for the Docker image name
    val dockerImageNamePattern = Pattern.compile("^($componentPattern/)?$componentPattern")

    return dockerImageNamePattern.matcher(imageName).matches()
}


fun getDockerImagePublic(instanceId: String): String {
    /*
    for images from the https://github.com/smallcloudai/refact-bench repo
    they are downloaded and pushed to the https://jetbrains.team/p/matterhorn/packages/container/swebench-public-images
    but we only use the images for 'bad' tasks that are not working in the currently used repos provided by Nebius
    */
    val repoNamespace = instanceId.split("__").first()
    val simplifiedInstanceId = instanceId
        .split("__").last()
        .replace("/", "_")
        .replace("__", "_")

    return "registry.jetbrains.team/p/matterhorn/swebench-public-images/swebench_sweb.eval.x86_64.${repoNamespace}_1776_${simplifiedInstanceId}:latest"
}


fun getImageNameFromInstanceId(instanceId: String): String {
    val instanceIdWoAddTags = if ("_perplexity_" in instanceId) {
        instanceId.split("_perplexity_")[0]
    } else {
        instanceId
    }
    val imageName = instanceIdWoAddTags.replace("-_", "_").toLowerCase()

    if (!isValidDockerImageName(imageName)) {
        throw IllegalArgumentException("$imageName for $instanceId is not valid docker image name")
    }

    return imageName
}

fun getDockerFullImageName(instanceId: String): String {
    if (instanceId in publicImageDockerizedInstanceIds) {
        return getDockerImagePublic(instanceId)
    }

    val imageName = getImageNameFromInstanceId(instanceId)
    return "${REGISTRY}/${PREFIX}-${imageName}:${IMAGE_TAG}"
}