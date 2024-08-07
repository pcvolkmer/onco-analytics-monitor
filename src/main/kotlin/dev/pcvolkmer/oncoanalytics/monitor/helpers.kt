package dev.pcvolkmer.oncoanalytics.monitor

import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionRepository
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import dev.pcvolkmer.oncoanalytics.monitor.conditions.StatisticsEntry

val allKeys = listOf(
    "C00-C14",
    "C15",
    "C16",
    "C18-C21",
    "C22",
    "C23-C24",
    "C25",
    "C32",
    "C33-C34",
    "C43",
    "C50, D05",
    "C53, D06",
    "C54-C55",
    "C56, D39.1",
    "C61",
    "C62",
    "C64",
    "C67, D09.0, D41.4",
    "C70-C72",
    "C73",
    "C81",
    "C82-C88, C96",
    "C90",
    "C91-C95",
    "Other"
)

/**
 * Fetch statistics using given ConditionRepository
 *
 * @see ConditionRepository
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
fun fetchStatistics(name: String, source: ConditionRepository): Statistics {
    fun mapIcd10Code(code: String): String {
        val c = when (code) {
            "D39.1", "D09.0", "D41.4" -> code
            else -> code.split('.').first()
        }

        return when (c) {
            "C00", "C01", "C02", "C03", "C04", "C05", "C06", "C07", "C08", "C09", "C10", "C11", "C12", "C13", "C14" -> "C00-C14"
            "C15" -> "C15"
            "C16" -> "C16"
            "C18", "C19", "C20", "C21" -> "C18-C21"
            "C22" -> "C22"
            "C23", "C24" -> "C23-C24"
            "C25" -> "C25"
            "C32" -> "C32"
            "C33", "C34" -> "C33-C34"
            "C43" -> "C43"
            "C50", "D05" -> "C50, D05"
            "C53", "D06" -> "C53, D06"
            "C54", "C55" -> "C54-C55"
            "C56", "D39.1" -> "C56, D39.1"
            "C61" -> "C61"
            "C62" -> "C62"
            "C64" -> "C64"
            "C67", "D09.0", "D41.4" -> "C67, D09.0, D41.4"
            "C70", "C71", "C72" -> "C70-C72"
            "C73" -> "C73"
            "C81" -> "C81"
            "C82", "C83", "C84", "C85", "C86", "C87", "C88", "C96" -> "C82-C88, C96"
            "C90" -> "C90"
            "C91", "C92", "C93", "C94", "C95" -> "C91-C95"
            else -> "Other"
        }
    }

    val entries = source.findAll()
        .groupBy { mapIcd10Code(it.icd10) }
        .mapValues { it.value.size }

    return Statistics(
        name,
        allKeys.map { StatisticsEntry(it, 0) }.map { StatisticsEntry(it.name, entries.getOrDefault(it.name, 0)) })
}