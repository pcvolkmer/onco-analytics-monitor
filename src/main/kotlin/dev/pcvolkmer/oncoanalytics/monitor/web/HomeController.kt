package dev.pcvolkmer.oncoanalytics.monitor.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping
    fun index(): String {
        return "index"
    }

}