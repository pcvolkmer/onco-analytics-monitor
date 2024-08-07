package dev.pcvolkmer.oncoanalytics.monitor

import dev.pcvolkmer.oncoanalytics.monitor.conditions.Condition
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionId
import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class HelpersTest {

    private lateinit var conditionsRepository: ConditionRepository

    @BeforeEach
    fun setup(
        @Mock conditionRepository: ConditionRepository
    ) {
        this.conditionsRepository = conditionRepository
    }

    @Test
    fun statisticsContainAllRequiredKeys() {
        val actual = fetchStatistics("test", this.conditionsRepository).entries.map { it.name }

        assertThat(actual).isEqualTo(allKeys)
    }

    @Test
    fun correctStatistics() {
        whenever(this.conditionsRepository.findAll()).thenReturn(conditionForEachKey)

        val actual = fetchStatistics("test", this.conditionsRepository)

        actual.entries.forEach { entry ->
            assertThat(entry.count).isEqualTo(1)
        }
    }

    companion object {
        val conditionForEachKey = allKeys
            .map { key -> key.split("[,\\-]".toRegex()).first().toString() }
            .map { key ->
                if (key == "Other" || key.contains('.', true)) {
                    key
                } else {
                    "$key.9"
                }
            }
            .map { icd10 ->
                Condition(ConditionId("TEST$icd10"), icd10, 1)
            }
    }

}