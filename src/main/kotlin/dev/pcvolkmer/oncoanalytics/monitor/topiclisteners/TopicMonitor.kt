package dev.pcvolkmer.oncoanalytics.monitor.topiclisteners

/**
 * TopicMonitor to listen to Kafka Topics
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
interface TopicMonitor {
    /**
     * Handle incoming Kafka Record
     *
     * @param topic The exact name of the topic
     * @param timestamp The timestamp the record has been published
     * @param key The records key
     * @param payload The records payload
     */
    fun handleTopicRecord(topic: String, timestamp: Long, key: String, payload: String)
}