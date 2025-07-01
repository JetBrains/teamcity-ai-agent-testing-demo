package utilities

import data.objectMapper
import jetbrains.buildServer.configs.kotlin.*

fun Project.instanceExecutor(id: String, name: String, instanceData: List<Map<String, String>>) {
    buildType {
        this.id(id)
        this.name = name

        features {
            matrix {
                param(
                    "Instance", instanceData.map {
                        value(
                            value = objectMapper.writeValueAsString(it).replace("%", "%%"),
                            label = it["instance_id"]
                        )
                    }
                )
            }
        }
    }
}
