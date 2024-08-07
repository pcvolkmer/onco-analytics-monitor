package dev.pcvolkmer.oncoanalytics.monitor.conditions

/**
 * A statistics summary
 *
 * @property name The name of the statistics e.g. the source of data
 * @property entries A list of entries containing information for each ICD10 code group
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
data class Statistics(
    val name: String,
    val entries: List<StatisticsEntry>
)

/**
 * A statistics entry
 *
 * @property name The name of the ICD10 code group
 * @property count Number of recognized oBDS or FHIR messages for the OCD10 code group
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
data class StatisticsEntry(val name: String, val count: Int)