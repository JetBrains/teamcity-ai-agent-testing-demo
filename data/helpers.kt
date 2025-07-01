package data

import com.fasterxml.jackson.core.type.TypeReference
import java.io.File


fun readInstanceIds(path: String): List<String> {
    val file = File(path)
    return objectMapper.readValue(file, object: TypeReference<List<String>>() {})
}
