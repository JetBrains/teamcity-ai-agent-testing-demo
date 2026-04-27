import jetbrains.buildServer.configs.kotlin.BuildType

/**
 * TeamCity build configuration for one SWE-bench task.
 *
 * This is a regular TeamCity BuildType with one extra field: the SWE-bench
 * task id. Keeping the id here lets other build configurations depend on this
 * build and still know which dataset task/artifacts they are working with.
 */
class Task(val taskId: String) : BuildType()
