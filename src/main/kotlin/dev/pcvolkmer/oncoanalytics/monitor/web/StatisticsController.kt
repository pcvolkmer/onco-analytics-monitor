package dev.pcvolkmer.oncoanalytics.monitor.web

import dev.pcvolkmer.oncoanalytics.monitor.conditions.ConditionInMemoryRepository
import dev.pcvolkmer.oncoanalytics.monitor.conditions.Statistics
import dev.pcvolkmer.oncoanalytics.monitor.fetchStatistics
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/statistics"])
class StatisticsController(
    @Qualifier("obdsXmlConditionRepository")
    private val obdsXmlConditionRepository: ConditionInMemoryRepository,
    @Qualifier("fhirObdsConditionRepository")
    private val fhirObdsConditionRepository: ConditionInMemoryRepository,
    @Qualifier("fhirPseudonymizedConditionRepository")
    private val fhirPseudonymizedConditionRepository: ConditionInMemoryRepository,
) {

    @GetMapping(path = ["obdsxml"])
    fun obdsxmlStatistics(): Statistics {
        return fetchStatistics("obdsxml", obdsXmlConditionRepository)
    }

    @GetMapping(path = ["fhirobds"])
    fun obdsfhirStatistics(): Statistics {
        return fetchStatistics("fhirobds", fhirObdsConditionRepository)
    }

    @GetMapping(path = ["fhirpseudonymized"])
    fun fhirpseudonymizedStatistics(): Statistics {
        return fetchStatistics("fhirpseudonymized", fhirPseudonymizedConditionRepository)
    }

}