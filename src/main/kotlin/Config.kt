package org.example

data class Config(
    var model: String = "sonar-pro",
    var temperature: Double = 0.7,
    var systemPrompt: String = "",
)