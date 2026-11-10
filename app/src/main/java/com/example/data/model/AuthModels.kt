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
    val ACCOUNTS = mapOf(
        "student" to Triple("vit_student", UserRole.STUDENT, "Aarav Sharma (Student)"),
        "cook" to Triple("vit_cook", UserRole.CHEF, "Chef Shinde (Canteen Kitchen)"),
        "admin" to Triple("vit_admin", UserRole.ADMIN, "VIT Canteen Manager (Admin)")
    )

    fun authenticate(username: String, password: String): AuthUser? {
        val normalizedUser = username.trim().lowercase()
        val entry = ACCOUNTS[normalizedUser] ?: return null
        if (entry.first == password.trim()) {
            val role = entry.second
            return AuthUser(
                username = normalizedUser,
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
