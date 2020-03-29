package com.example

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.features.*
import com.fasterxml.jackson.databind.*
import io.ktor.jackson.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.util.pipeline.PipelineContext
import io.ktor.features.ContentNegotiation

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

const val REST_ENDPOINT = "/person"

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CORS) { // installing middleware
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val client = HttpClient(Apache) {
    }

    routing {
        // get request with dynamic variable id
        get("$REST_ENDPOINT/{id}") {
            errorAware {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Parameter id not found")
                call.respond(PersonObject.get(id))
            }
        }

        // get request to get the list of all persons
        get(REST_ENDPOINT) {
            errorAware {
                call.respond(PersonObject.getAll())
            }
        }

        // delete request by the person's id
        delete("$REST_ENDPOINT/{id}") {
            errorAware {
                val id = call.parameters["id"] ?: throw IllegalArgumentException("Parameter id not found")
                call.respondSuccessJson(PersonObject.remove(id))
            }
        }

        // delete request to clear all the fake database
        delete(REST_ENDPOINT) {
            errorAware {
                PersonObject.clear()
                call.respondSuccessJson()
            }
        }

        post(REST_ENDPOINT) {
            errorAware {
                val receive = call.receive<Person>()
                println("Received Post Request: $receive")
                call.respond(PersonObject.add(receive))
            }
        }

        get("/") {
            call.respondHtml {
                head {
                    title("Kotlin API")
                }
                body {
                    h1 { +"Welcome to the Interview Test Dev API" }
                    p { +"First of all run all the request inside the PostRequest.http" }
                    p { +"Great ! We now have data in our fake database. Now, please run the request inside GetList.http, you can see the result by adding /person in your browser" }
                    p { +"Pretty cool yeah, now we have a list of all persons stored in our fake database." }
                    p { +"If you want to delete someone's data by his id, please run DeleteRequest to get rid of it." }
                    p { +"If you want to clear the fake database you can run the request inside DeleteAll.http" }
                    p { +"Now you've seen pretty much everything, feel free to modify the .http files to play with your new API" }
                    p { +"You will find further information inside the LIBRARIES.MD if you want" }
                    p { +"Thank you !" }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }

        get("/json/jackson") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}

private suspend fun <R> PipelineContext<*, ApplicationCall>.errorAware(block: suspend () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        call.respondText(
            """{"error":"$e"}"""
            , ContentType.parse("application/json")
            , HttpStatusCode.InternalServerError
        )
        null
    }
}

private suspend fun ApplicationCall.respondSuccessJson(value: Boolean = true) =
    respond("""{"success": "$value"}""")
