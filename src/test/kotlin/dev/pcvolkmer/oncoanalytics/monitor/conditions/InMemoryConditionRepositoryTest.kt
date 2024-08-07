package dev.pcvolkmer.oncoanalytics.monitor.conditions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InMemoryConditionRepositoryTest {

    private lateinit var conditionsRepository: InMemoryConditionRepository

    @BeforeEach
    fun setup() {
        this.conditionsRepository = InMemoryConditionRepository()

        // Setup initial data
        this.conditionsRepository.saveIfNewerVersion(Condition(ConditionId("TEST"), "F79.9", 2))
        assertThat(this.conditionsRepository.findAll()).hasSize(1)
    }

    @Test
    fun shouldSaveNewerCondition() {
        val success = this.conditionsRepository.saveIfNewerVersion(Condition(ConditionId("TEST"), "F79.9", 3))

        assertThat(success).isTrue
        assertThat(this.conditionsRepository.findAll()).hasSize(1)
        assertThat(this.conditionsRepository.findAll()[0].version).isEqualTo(3)
    }

    @Test
    fun shouldNotSaveSameConditionTwice() {
        val success = this.conditionsRepository.saveIfNewerVersion(Condition(ConditionId("TEST"), "F79.9", 2))

        assertThat(success).isFalse
        assertThat(this.conditionsRepository.findAll()).hasSize(1)
        assertThat(this.conditionsRepository.findAll()[0].version).isEqualTo(2)
    }

    @Test
    fun shouldNotSaveOlderCondition() {
        val success = this.conditionsRepository.saveIfNewerVersion(Condition(ConditionId("TEST"), "F79.9", 1))

        assertThat(success).isFalse
        assertThat(this.conditionsRepository.findAll()).hasSize(1)
        assertThat(this.conditionsRepository.findAll()[0].version).isEqualTo(2)
    }

}