package dev.ajkneisl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject
import java.awt.Color
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


val httpClient =
    HttpClient(CIO) { install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) } }

val config by lazy {
    val configFile = File("config.json")
    Json.decodeFromString<Config>(configFile.readText())
}

suspend fun main() {
    val courses = getCanvasClasses()
    val projects = getTodoistProjects()

    for (course in courses) {
        val courseColor = Color.decode(course.courseColor ?: "#ffffff")

        println("${rgbbg(courseColor.red, courseColor.green, courseColor.blue)} ${course.name} $RC")

        val todoistCourseName = course.name.split(" ").take(2).joinToString(" ")
        val todoistColor = nearestColor(courseColor)
        val todoistProject =
            projects
                .map { it as JSONObject }
                .firstOrNull { it.getString("name") == todoistCourseName }

        var projectId: String

        val tasks: JSONArray =
            if (todoistProject == null) {
                print("Creating Todoist project... ")

                val response = createTodoistProject(todoistCourseName, todoistColor)
                projectId = response.getString("id")

                // some classes have similar name w diff sections
                projects.put(response)

                print("Created Todoist project for $U$todoistCourseName$RC! ")

                JSONArray()
            } else {
                projectId = todoistProject.getString("id")
                val tasks = getTasks(projectId)

                print("Found Todoist project with $U${tasks.length()}$RC tasks. ")

                tasks
            }

        val assignments = getCanvasAssignments(course.id)

        print("Found $U${assignments.length()}$RC assignments. ")

        var noSubmissionDate = 0
        var nonSubmittable = 0
        var createdTask = 0
        var alreadyExists = 0
        var updatedTask = 0

        assignment@ for (assignment in assignments.map { it as JSONObject }) {
            val url = assignment.getString("html_url")

            val isLocked = assignment.getBoolean("locked_for_user")
            val submitted =
                assignment.getJSONObject("submission").getString("workflow_state") != "unsubmitted"
            val name = assignment.getString("name")
            val dueAt =
                try {
                    assignment.getString("due_at")
                } catch (ex: Exception) {
                    noSubmissionDate++
                    continue
                }

            val assignmentName = "[$name]($url)"
            var existingTask: JSONObject? = null

            task@ for (task in tasks.map { it as JSONObject }) {
                val taskName = task.getString("content")

                if (taskName == assignmentName) {
                    alreadyExists++
                    existingTask = task
                    break@task
                }
            }

            try {
                val submissionTypes = listOf("not_graded", "none", "on_paper")

                if (
                    submissionTypes.contains(
                        assignment.getJSONArray("submission_types").getString(0)
                    )
                ) {
                    nonSubmittable++
                    continue
                }
            } catch (_: Exception) {}

            if (existingTask == null && !isLocked && !submitted) {
                createTask(projectId, assignmentName, dueAt, detectPriority(name))
                createdTask++
            } else if (existingTask != null) {
                val date = existingTask.getJSONObject("deadline").getString("date")

                val existingDate = LocalDate.parse(dueAt, DateTimeFormatter.ISO_DATE_TIME)
                val assignmentDate =
                    LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                println("$existingDate $assignmentDate")

                // when an assignment's date changes
                if (existingDate != assignmentDate) {
                    updateTask(existingTask.getString("id"), "deadline_date" to dueAt)
                    updatedTask++
                }
            }
        }

        print(
            "Created $U$createdTask$RC tasks and updated $U$updatedTask$RC tasks.\n"
        )
    }
}

suspend fun updateTask(taskID: String, attr: Pair<String, String>) {
    httpClient.post("https://api.todoist.com/rest/v2/tasks/$taskID") {
        header("Authorization", "Bearer ${config.todoistToken}")

        contentType(ContentType.Application.Json)
        setBody(hashMapOf(attr))
    }
}

fun detectPriority(name: String): Int {
    return when {
        name.contains("exam") || name.contains("report") || name.contains("quiz") -> 1
        name.contains("lab") -> 2
        name.contains("homework") -> 3
        else -> 4
    }
}

suspend fun createTask(
    projectId: String,
    name: String,
    deadline: String,
    priority: Int,
): JSONObject {
    return httpClient
        .post("https://api.todoist.com/rest/v2/tasks") {
            header("Authorization", "Bearer ${config.todoistToken}")

            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "content" to name,
                    "project_id" to projectId,
                    "deadline_date" to deadline,
                    "priority" to priority.toString(),
                )
            )
        }
        .let { JSONObject() }
}

suspend fun getTasks(projectId: String): JSONArray {
    return httpClient
        .get("https://api.todoist.com/rest/v2/tasks") {
            header("Authorization", "Bearer ${config.todoistToken}")

            parameter("project_id", projectId)
        }
        .bodyAsText()
        .let { JSONArray(it) }
}

suspend fun getCanvasAssignments(course: Int): JSONArray {
    return httpClient
        .get("${config.canvasUrl}/api/v1/courses/$course/assignments") {
            header("Authorization", "Bearer ${config.canvasToken}")

            parameter("per_page", "100")
            parameter("include", "submission")
            parameter("enrollment_state", "activate")
        }
        .bodyAsText()
        .let { JSONArray(it) }
}

suspend fun createTodoistProject(name: String, color: String): JSONObject {
    return httpClient
        .post("https://api.todoist.com/rest/v2/projects") {
            header("Authorization", "Bearer ${config.todoistToken}")
            contentType(ContentType.Application.Json)
            setBody(hashMapOf("name" to name, "color" to color))
        }
        .let { JSONObject(it.bodyAsText()) }
}

suspend fun getTodoistProjects(): JSONArray {
    return httpClient
        .get("https://api.todoist.com/rest/v2/projects") {
            header("Authorization", "Bearer ${config.todoistToken}")
        }
        .bodyAsText()
        .let { JSONArray(it) }
}

suspend fun getCanvasClasses(): List<Course> {
    val courses: List<Course> =
        httpClient
            .get("${config.canvasUrl}/api/v1/courses") {
                header("Authorization", "Bearer ${config.canvasToken}")

                parameter("per_page", "100")
                parameter("include", "submission")
                parameter("enrollment_state", "activate")
            }
            .body()

    val courseColors =
        httpClient
            .get("${config.canvasUrl}/api/v1/users/self/colors") {
                header("Authorization", "Bearer ${config.canvasToken}")
            }
            .bodyAsText()
            .let { JSONObject(it) }
            .getJSONObject("custom_colors")

    for (course in courses) {
        val courseColor =
            try {
                courseColors.getString("course_${course.id}")
            } catch (ex: Exception) {
                null
            }

        course.courseColor = courseColor
    }

    return courses
}
