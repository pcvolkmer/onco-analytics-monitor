package dev.pcvolkmer.oncoanalytics.monitor.conditions

/**
 * Interface declaring a data repository to hold (latest) conditions
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
interface ConditionRepository {

    /**
     * Save condition if no newer condition (by id and version) is present
     *
     * @param condition The condition to be saved
     * @return Will return true if successful
     */
    fun saveIfNewerVersion(condition: Condition): Boolean

    /**
     * Save condition without any checks
     *
     * @param condition The condition to be saved
     * @return Will return true if successful
     */
    fun save(condition: Condition): Boolean

    /**
     * Return all saved conditions
     */
    fun findAll(): List<Condition>

}