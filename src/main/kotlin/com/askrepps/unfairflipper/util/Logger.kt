package com.askrepps.unfairflipper.util

interface Logger {

    fun log(message: String)
}

class NullLogger : Logger {

    override fun log(message: String) {
        // do nothing
    }
}

class ConsoleLogger : Logger {

    override fun log(message: String) {
        println(message)
    }
}
