package dev.ajkneisl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Int,
    val name: String,
    @SerialName("account_id") val accountId: Int,
    val uuid: String,
    @SerialName("start_at") val startAt: String? = null,
    @SerialName("grading_standard_id") val gradingStandardId: Int? = null,
    @SerialName("is_public") val isPublic: Boolean?,
    @SerialName("created_at") val createdAt: String,
    @SerialName("course_code") val courseCode: String,
    @SerialName("default_view") val defaultView: String,
    @SerialName("root_account_id") val rootAccountId: Int,
    @SerialName("enrollment_term_id") val enrollmentTermId: Int,
    val license: String? = null,
    @SerialName("grade_passback_setting") val gradePassbackSetting: String? = null,
    @SerialName("end_at") val endAt: String? = null,
    @SerialName("public_syllabus") val publicSyllabus: Boolean,
    @SerialName("public_syllabus_to_auth") val publicSyllabusToAuth: Boolean,
    @SerialName("storage_quota_mb") val storageQuotaMb: Int,
    @SerialName("is_public_to_auth_users") val isPublicToAuthUsers: Boolean,
    @SerialName("homeroom_course") val homeroomCourse: Boolean,
    @SerialName("course_color") var courseColor: String? = null,
    @SerialName("friendly_name") val friendlyName: String? = null,
    @SerialName("apply_assignment_group_weights") val applyAssignmentGroupWeights: Boolean,
    val calendar: Calendar,
    @SerialName("time_zone") val timeZone: String,
    val blueprint: Boolean,
    val template: Boolean,
    val enrollments: List<Enrollment>,
    @SerialName("hide_final_grades") val hideFinalGrades: Boolean,
    @SerialName("workflow_state") val workflowState: String,
    @SerialName("restrict_enrollments_to_course_dates") val restrictEnrollmentsToCourseDates: Boolean
)

@Serializable
data class Calendar(
    val ics: String
)

@Serializable
data class Enrollment(
    val type: String,
    val role: String,
    @SerialName("role_id") val roleId: Int,
    @SerialName("user_id") val userId: Int,
    @SerialName("enrollment_state") val enrollmentState: String,
    @SerialName("limit_privileges_to_course_section") val limitPrivilegesToCourseSection: Boolean
)