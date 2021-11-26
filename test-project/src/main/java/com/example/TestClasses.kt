package com.example

import com.example.annotation.Scrub

@Scrub
data class DataClass(val address: String, val city: String, val unit: Int)

class Human(
    private var firstName: String,
    @Scrub
    var lastName: String,
    @Scrub
    var socialSecurityNumber: String
)