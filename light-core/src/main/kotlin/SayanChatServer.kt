package org.sayandev

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class SayanChatServer(
) {
    init {
        embeddedServer(
            Netty,
            port = 8080,
            host = "0.0.0.0",
            module = Application::module,
        ).start(wait = true)
    }
}

fun Application.module() {
    configureRouting()
}

fun Application.configureRouting() {
    routing {
        route("/api/v1") {
            get("/chatnodepackage") {
                call.respondText(
                    """
                        {
                            "foo": "bar"
                        }
                    """.trimIndent(),
                    ContentType.Application.Json
                )
            }
        }
    }
}