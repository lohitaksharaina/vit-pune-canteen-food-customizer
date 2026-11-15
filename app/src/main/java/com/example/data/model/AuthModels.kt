package com.example.data.model

enum class UserRole(val label: String, val badge: String) {
    STUDENT("VIT Student", "Student"),
    CHEF("Canteen Cook", "Cook"),
    ADMIN("Canteen Administrator", "Admin")
}

data class AuthUser(
    val username: String,
    val role: UserRole,
    val displayName: String,
    val email: String
)

object DemoCredentials {
    private val VALID_CREDENTIALS = mapOf(
        "student" to Triple(setOf("vit_student", "student"), UserRole.STUDENT, "Aarav Sharma (Student)"),
        "cook" to Triple(setOf("vit_cook", "cook", "chef", "vit_chef"), UserRole.CHEF, "Chef Shinde (Canteen Kitchen)"),
        "chef" to Triple(setOf("vit_cook", "cook", "chef", "vit_chef"), UserRole.CHEF, "Chef Shinde (Canteen Kitchen)"),
        "admin" to Triple(setOf("vit_admin", "admin"), UserRole.ADMIN, "VIT Canteen Manager (Admin)")
    )

    fun authenticate(username: String, password: String): AuthUser? {
        val normalizedUser = username.trim().lowercase()
        val entry = VALID_CREDENTIALS[normalizedUser] ?: return null
        val trimmedPass = password.trim()
        if (entry.first.contains(trimmedPass)) {
            val role = entry.second
            return AuthUser(
                username = if (normalizedUser == "chef") "cook" else normalizedUser,
                role = role,
                displayName = entry.third,
                email = when (role) {
                    UserRole.STUDENT -> "student.vit@vit.edu"
                    UserRole.CHEF -> "canteen.cook@vit.edu"
                    UserRole.ADMIN -> "canteen.admin@vit.edu"
                }
            )
        }
        return null
    }
}
