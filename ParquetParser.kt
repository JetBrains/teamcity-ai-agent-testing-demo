import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.example.data.Group
import org.apache.parquet.hadoop.ParquetReader
import org.apache.parquet.hadoop.example.GroupReadSupport
import org.apache.parquet.hadoop.util.HadoopInputFile
import org.apache.parquet.schema.GroupType
import java.io.File

val objectMapper = ObjectMapper()

const val PROJECT_ROOT_PATH = "/Users/andrei.efimov/IdeaProjects/swebench_matterhorn/.teamcity"
const val PARQUET_DEV_FILE_PATH = "$PROJECT_ROOT_PATH/parquet/dev-00000-of-00001.parquet"
const val PARQUET_TEST_FILE_PATH = "$PROJECT_ROOT_PATH/parquet/test-00000-of-00001.parquet"

const val JSON_DEV_FILE_PATH = "$PROJECT_ROOT_PATH/parquet/dev-00000-of-00001.json"
const val JSON_TEST_FILE_PATH = "$PROJECT_ROOT_PATH/parquet/test-00000-of-00001.json"

// use for testing
// or for converting parquet files to json
fun main() {
  val result =
    ParquetParser.readParquetFile(File(PARQUET_DEV_FILE_PATH))

  println(result[0].keys)
  println(result[0]["repo"])

  convertFiles()
}

fun convertFiles() {
  convertDevParquetToJson()
  convertTestParquetToJson()
}

fun convertDevParquetToJson() {
  convertParquetFileToJson(
    File(PARQUET_DEV_FILE_PATH),
    File(JSON_DEV_FILE_PATH).also {
      try {
        it.delete()
      } catch (_: Exception) {
        // ignored
      }
    }
  )
}

fun convertTestParquetToJson() {
  convertParquetFileToJson(
    File(PARQUET_TEST_FILE_PATH),
    File(JSON_TEST_FILE_PATH).also {
      try {
        it.delete()
      } catch (_: Exception) {
        // ignored
      }
    }
  )
}

fun convertParquetFileToJson(parquetFile: File, jsonFile: File) {
  val result = ParquetParser.readParquetFile(parquetFile)

  jsonFile.writeText(
    objectMapper.writeValueAsString(result)
  )
}

object ParquetParser {
  fun readParquetFile(parquetFile: File): List<Map<String, String>> {
    val result: MutableList<Map<String, String>> = mutableListOf()

    if (parquetFile.exists().not()) {
      return result
    }

    val config = Configuration()
    var reader: ParquetReader<Group>? = null
    val inputFile = HadoopInputFile.fromPath(org.apache.hadoop.fs.Path(parquetFile.absolutePath), config)

    // Read support provides methods for interpreting data
    val readSupport = GroupReadSupport()

    try {
      // Building a ParquetReader using the read support object
      reader = ParquetReader.builder(readSupport, inputFile.path).build()

      var group: Group? = reader.read()
      while (group != null) {
        result.add(readRow(group.type, group))
        // Repeat for other columns and types as necessary
        group = reader.read()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      try {
        reader?.close()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }

    return result
  }

  private fun readRow(
    schema: GroupType,
    group: Group
  ): MutableMap<String, String> {
    val row: MutableMap<String, String> = mutableMapOf()
    for (field in schema.fields) {
      val fieldName = field.name
      // Assuming all types are strings
      val value: String = group.getString(fieldName, 0)
      row[fieldName] = value
    }
    return row
  }
}
