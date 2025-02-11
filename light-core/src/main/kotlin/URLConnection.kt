package org.sayandev

import java.net.HttpURLConnection
import java.net.URL

data class URLConnection(
    val url: URL
) {
    constructor(url: String): this(URL(url))
    private var connection: HttpURLConnection? = null

    fun connection(): HttpURLConnection {
        return connection ?: open()
    }

    fun open(): HttpURLConnection {
        return (url.openConnection() as HttpURLConnection).also { connection = it }
    }

    fun connect() {
        connection().requestMethod = "GET"
        connection().connect()
    }

    fun openAndConnect(): HttpURLConnection {
        return open().also { connect() }
    }

    fun isValid(): Boolean {
        return connection().responseCode == 200
    }
}