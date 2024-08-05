package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.listener.ConsumerSeekAware.ConsumerSeekCallback
import reactor.core.publisher.Sinks

abstract class TopicMonitor(private val statisticsEventProducer: Sinks.Many<Statistics>) : ConsumerSeekAware {

    abstract fun handleTopicRecord(topic: String, timestamp: Long, key: String, payload: String)

    fun sendUpdatedStatistics(statistics: Statistics) {
        statisticsEventProducer.emitNext(statistics) { _, _ -> false }
    }

    override fun onPartitionsAssigned(assignments: MutableMap<TopicPartition, Long>, callback: ConsumerSeekCallback) {
        callback.seekToBeginning(assignments.keys)
    }

}