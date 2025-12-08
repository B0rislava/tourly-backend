package com.tourly.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TourlyApiApplication

fun main(args: Array<String>) {
	runApplication<TourlyApiApplication>(*args)
}
