package dev.pcvolkmer.oncoanalytics.monitor.conditions

import org.apache.commons.codec.digest.DigestUtils

/**
 * Type to be used for the condition ID
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
@JvmInline
value class ConditionId(val value: String) {
    companion object {
        fun fromPatientIdAndTumorId(patientId: String, tumorId: String): ConditionId {
            return ConditionId(DigestUtils.sha256Hex("$patientId-$tumorId"))
        }
    }
}

/**
 * A condition
 *
 * @property id The conditions unique ID
 * @property icd10 The ICD10 code of the related diagnosis
 * @property version (if present) The version of the related oBDS message or zero
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 */
data class Condition(val id: ConditionId, val icd10: String, val version: Int = 0)

/**
 * In memory implementation of a ConditionRepository
 *
 * @author Paul-Christian Volkmer
 * @since 0.1.0
 * @see ConditionRepository
 */
class InMemoryConditionRepository : ConditionRepository {

    private val conditions = mutableMapOf<ConditionId, Condition>()

    override fun saveIfNewerVersion(condition: Condition): Boolean {
        if ((this.conditions[condition.id]?.version ?: 0) < condition.version) {
            return this.save(condition)
        }
        return false
    }

    override fun save(condition: Condition): Boolean {
        this.conditions[condition.id] = condition
        return true
    }

    override fun findAll(): List<Condition> {
        return conditions.values.toList()
    }

}