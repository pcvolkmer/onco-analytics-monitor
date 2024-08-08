package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.listener.ConsumerSeekAware.ConsumerSeekCallback
import reactor.core.publisher.Sinks

/**
 * Abstract class with common methods for all TopicMonitors
 *
 * This implements ConsumerSeekAware to seek any topic to the beginning and load all available topic records from start.
 *
 * @property statisticsEventProducer The event producer/sink to notify about saved condition
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 *
 * @see ConsumerSeekAware
 */
abstract class AbstractTopicMonitor(private val statisticsEventProducer: Sinks.Many<Statistics>) : TopicMonitor,
    ConsumerSeekAware {

    /**
     * This will send new/updated statistics
     *
     * @param statistics The statistics to be sent/updated
     */
    fun sendUpdatedStatistics(statistics: Statistics) {
        statisticsEventProducer.emitNext(statistics) { _, _ -> false }
    }

    /**
     * This will seek assigned Kafka Partitions back to the beginning for all inheriting classes
     */
    override fun onPartitionsAssigned(assignments: MutableMap<TopicPartition, Long>, callback: ConsumerSeekCallback) {
        callback.seekToBeginning(assignments.keys)
    }

}