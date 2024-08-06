package dev.pcvolkmer.oncoanalytics.monitor.conditions

import org.apache.commons.codec.digest.DigestUtils

@JvmInline
value class ConditionId(val value: String) {
    companion object {
        fun generateConditionId(patientId: String, tumorId: String): ConditionId {
            return ConditionId(DigestUtils.sha256Hex("$patientId-$tumorId"))
        }
    }
}

data class Condition(val id: ConditionId, val icd10: String, val version: Int = 0)

class ConditionInMemoryRepository {

    private val conditions = mutableMapOf<ConditionId, Condition>()

    fun saveIfNewerVersion(condition: Condition): Boolean {
        if ((this.conditions[condition.id]?.version ?: 0) < condition.version) {
            this.conditions[condition.id] = condition
            return true
        }
        return false
    }

    fun save(condition: Condition): Boolean {
        this.conditions[condition.id] = condition
        return true
    }

    fun findAll(): List<Condition> {
        return conditions.values.toList()
    }

}