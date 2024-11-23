package com.ai.faq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FaqApplication

fun main(args: Array<String>) {
	runApplication<FaqApplication>(*args)
}
