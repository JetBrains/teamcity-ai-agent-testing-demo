package data

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File

val objectMapper = ObjectMapper()

val devData: List<Map<String, String>> = objectMapper.readValue(
    File("data/dev-00000-of-00001.json"),
    object: TypeReference<List<Map<String, String>>>() {}
)

val testData: List<Map<String,String>> = objectMapper.readValue(
    File("data/test-00000-of-00001.json"),
    object: TypeReference<List<Map<String, String>>>() {}
)

val devLiteData: List<Map<String,String>> = objectMapper.readValue(
    File("data/lite-dev-00000-of-00001.json"),
    object: TypeReference<List<Map<String, String>>>() {}
)

val testLiteData: List<Map<String,String>> = objectMapper.readValue(
    File("data/lite-test-00000-of-00001.json"),
    object: TypeReference<List<Map<String, String>>>() {}
)

val extendedData: List<Map<String, String>> = objectMapper.readValue(
    File("data/swe-bench-extended.json"),
    object: TypeReference<List<Map<String, String>>>() {}
)

val extendedDataWithGold: List<Map<String, String>> = objectMapper.readValue(
    File("data/swe-bench-extended-with-gold.json"),
    object: TypeReference<List<Map<String, String>>>() {}
)

val perplexityData: List<Map<String, String>> = objectMapper.readValue(
    File("data/test-perplexity-mode.json"),
    object: TypeReference<List<Map<String, String>>> () {}
)

val perplexityDataAgent: List<Map<String, String>> = objectMapper.readValue(
    File("data/test-perplexity-mode-agent.json"),
    object: TypeReference<List<Map<String, String>>> () {}
)

val badIdsTestLite = listOf(
//    "django__django-10914",
//    "django__django-11564",
//    "django__django-13660",
//    "matplotlib__matplotlib-18869",
    "psf__requests-2674",
    "psf__requests-863",
//    "pydata__xarray-4094",
//    "pydata__xarray-4493",
    "pytest-dev__pytest-5495",
//    "sympy__sympy-13146",
//    "sympy__sympy-13177",
    "sympy__sympy-22714"
)

val tasksForCICheck = listOf(
    "pytest-dev__pytest-6202",
    "pytest-dev__pytest-7571",
    "scikit-learn__scikit-learn-15100",
    "sphinx-doc__sphinx-8269",
    "sympy__sympy-17139",
    "sympy__sympy-19954",
    "sympy__sympy-23824",
)

val badIdsDev = readInstanceIds("data/slices/bad_ids_dev.json")

val badTestVerified = readInstanceIds("data/slices/bad_test_verified_common.json")

val testVerified500 = readInstanceIds("data/slices/test_verified_500.json")
   .filterNot { it in badTestVerified }
val badIdsExtended = readInstanceIds("data/slices/bad_ids_extended.json").toSet()
val extended100 = readInstanceIds("data/slices/extended_100.json").toSet()
val pydanticLast16 = readInstanceIds("data/slices/pydantic_last_16.json").toSet()
val pydanticFirst100 = readInstanceIds("data/slices/pydantic_first_100.json").toSet()
val extendedResolvedSubset = readInstanceIds("data/slices/extended_resolved_subset.json").toSet()
val testLiteFilteredInstances = testLiteData.filterNot { it["instance_id"] in badIdsTestLite }
val testVerifiedFilteredInstances = testData.filter { it["instance_id"] in testVerified500 }.filterNot { it["instance_id"] in badTestVerified }
val devFilteredInstances = devData.filterNot { it["instance_id"] in badIdsDev }
val extendedFilteredInstances = extendedData.filterNot { it["instance_id"] in badIdsExtended }
val extendedWithGoldFilteredInstances = extendedDataWithGold.filterNot { it["instance_id"] in badIdsExtended }
val extendedResolvedInstances = extendedFilteredInstances.filter { it["instance_id"] in extendedResolvedSubset }
val extendedNotResolvedInstances = extendedFilteredInstances.filterNot { it["instance_id"] in extendedResolvedSubset }

val devSub10 = readInstanceIds("data/slices/dev_sub_10.json")
    .filterNot { it in badIdsDev }
//val devSub35 = listOf("marshmallow-code__marshmallow-1164", "marshmallow-code__marshmallow-1343", "marshmallow-code__marshmallow-1810", "pvlib__pvlib-python-1072", "pvlib__pvlib-python-1154", "pvlib__pvlib-python-1191", "pvlib__pvlib-python-1216", "pvlib__pvlib-python-1224", "pvlib__pvlib-python-1273", "pvlib__pvlib-python-1349", "pvlib__pvlib-python-1589", "pvlib__pvlib-python-1606", "pvlib__pvlib-python-1738", "pvlib__pvlib-python-1740", "pvlib__pvlib-python-1854", "pvlib__pvlib-python-763", "pydicom__pydicom-1033", "pydicom__pydicom-1050", "pydicom__pydicom-1192", "pydicom__pydicom-1256", "pydicom__pydicom-1416", "pydicom__pydicom-1694", "pydicom__pydicom-800", "pylint-dev__astroid-1614", "pylint-dev__astroid-1719", "pylint-dev__astroid-1959", "pylint-dev__astroid-1962", "pyvista__pyvista-3710", "sqlfluff__sqlfluff-2419", "sqlfluff__sqlfluff-2907", "sqlfluff__sqlfluff-3066", "sqlfluff__sqlfluff-4753", "sqlfluff__sqlfluff-4778", "sqlfluff__sqlfluff-4834", "sqlfluff__sqlfluff-905")
//    .filterNot { it in badIdsDev }
val devSub53 = readInstanceIds("data/slices/dev_sub_53.json")
    .filterNot { it in badIdsDev }
val devSubResolved62 = readInstanceIds("data/slices/dev_sub_resolved_62.json")
//val devSubResolved25 = listOf("pydicom__pydicom-1256", "pylint-dev__astroid-1719", "pvlib__pvlib-python-1154", "marshmallow-code__marshmallow-1164", "pvlib__pvlib-python-1854", "pyvista__pyvista-4853", "pydicom__pydicom-1031", "pydicom__pydicom-965", "sqlfluff__sqlfluff-2998", "pylint-dev__astroid-1616", "pylint-dev__astroid-1962", "pvlib__pvlib-python-1349", "pylint-dev__astroid-1903", "pydicom__pydicom-1539", "marshmallow-code__marshmallow-1252", "pydicom__pydicom-1050", "pydicom__pydicom-1416", "pylint-dev__astroid-984", "pyvista__pyvista-4315", "pylint-dev__astroid-1959", "sqlfluff__sqlfluff-5206", "pvlib__pvlib-python-1224", "marshmallow-code__marshmallow-1810", "pvlib__pvlib-python-1165", "sqlfluff__sqlfluff-880")
val devSubPotential40 = readInstanceIds("data/slices/dev_sub_potential_40.json")
//val devSubPotential10 = listOf("pydicom__pydicom-793", "sqlfluff__sqlfluff-3354", "pyvista__pyvista-4406", "pylint-dev__astroid-934", "sqlfluff__sqlfluff-1517", "pydicom__pydicom-1428", "pvlib__pvlib-python-1739", "sqlfluff__sqlfluff-1625", "pylint-dev__astroid-1262", "marshmallow-code__marshmallow-2123")
val devSubResolved95 = readInstanceIds("data/slices/dev_sub_resolved_95.json")
    .filterNot { it in badIdsDev }
val devSubResolved35 = devSubResolved95.filterIndexed  { index, _ -> index % 3 == 0 } + listOf(
    devSubResolved95[1],
    devSubResolved95[22],
    devSubResolved95[52],
    devSubResolved95[82],
)
val devSubResolved67 = readInstanceIds("data/slices/dev_sub_resolved_67.json")
    .filterNot { it in badIdsDev }

val testLiteSubResolved148 = readInstanceIds("data/slices/test_lite_sub_resolved_148.json")
val testLiteSubPotential28 = readInstanceIds("data/slices/test_lite_sub_potential_28.json")
val testLiteSubResolved218 = readInstanceIds("data/slices/test_lite_sub_resolved_218.json")
    .filterNot { it in badIdsTestLite }
val testVerified399 = readInstanceIds("data/slices/test_verified_399.json")
    .filterNot { it in badTestVerified + tasksForCICheck }
val testVerified100 = readInstanceIds("data/slices/test_verified_100.json")
    .filterNot { it in badTestVerified + tasksForCICheck }

val testVerifiedAlwaysGood = readInstanceIds("data/slices/test_verified_always_good.json")
val testVerified280 = testVerified399.filterNot { it in testVerifiedAlwaysGood + tasksForCICheck }
val testVerified10 = testVerified280.filterNot { it.startsWith("django") || it.startsWith("matplotlib") }.filterIndexed  { index, _ -> index % 10 == 0 }

val checkExecutionInstanceIds = readInstanceIds("data/slices/check_execution_instance_ids.json")
val checkCacheInstanceIds = readInstanceIds("data/slices/check_cache_instance_ids.json")

val testPerplexityMode = perplexityData.mapNotNull { it["instance_id"] }
val testPerplexityModeAgent =  perplexityDataAgent.mapNotNull { it["instance_id"] }

// Instance ids for which docker images exist
val dockerizedInstanceIds = (testVerified500 + testPerplexityMode + testPerplexityModeAgent).toSet()

val publicImageDockerizedInstanceIds = readInstanceIds("data/slices/test_verified_500_with_public_images.json")