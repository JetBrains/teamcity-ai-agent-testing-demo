import jetbrains.buildServer.configs.kotlin.BuildType

class Task(val taskId: String, init: BuildType.() -> Unit) : BuildType(init)