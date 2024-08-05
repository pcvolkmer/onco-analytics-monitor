package dev.pcvolkmer.oncoanalytics.monitor.conditions

data class Statistics(
    val name: String,
    val entries: List<StatisticsEntry>
)

data class StatisticsEntry(val name: String, val count: Int)