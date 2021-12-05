package com.example

import dev.meloche.Scrub


@Scrub
data class DataClass(val address: String, val city: String, val unit: Int)

class Human(
    private var firstName: String,
    @Scrub
    var job: Job,
    @Scrub
    var lastName: String,
    @Scrub
    var socialSecurityNumber: String
)

class Job(
    private var jobTitle: String,
    @Scrub
    private var company: String
)
