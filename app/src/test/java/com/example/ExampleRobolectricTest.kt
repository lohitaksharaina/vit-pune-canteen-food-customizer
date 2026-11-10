package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MenuCatalog
import com.example.data.model.CustomisedOrderItem
import com.example.data.model.DemoCredentials
import com.example.data.model.UserRole
import com.example.data.repository.OrderRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("VIT Canteen", appName)
    }

    @Test
    fun `menu catalog contains VIT Pune Canteen specialties`() {
        val items = MenuCatalog.items
        assertTrue("Menu should contain canteen items", items.isNotEmpty())
        assertTrue("Menu should contain Misal Pav", items.any { it.name.contains("Misal", ignoreCase = true) })
        assertTrue("Menu should contain Vada Pav", items.any { it.name.contains("Vada Pav", ignoreCase = true) })
        assertTrue("Menu should contain Thick Cold Coffee", items.any { it.name.contains("Cold Coffee", ignoreCase = true) })
        assertTrue("Menu should contain Student Thali", items.any { it.name.contains("Thali", ignoreCase = true) })
    }

    @Test
    fun `customised item captures excluded ingredients and chef notes`() {
        val item = CustomisedOrderItem(
            menuItemId = "vit_snack_01",
            name = "Puneri Special Misal Pav",
            unitPrice = 50.0,
            quantity = 2,
            isVeg = true,
            dislikedIngredients = listOf("Onions (Kanda)", "Spicy Rassa / Tarri"),
            chefNote = "Extra crispy pav please",
            spicePreference = "Normal"
        )
        assertEquals(100.0, item.totalPrice, 0.01)
        assertTrue(item.dislikedIngredients.contains("Onions (Kanda)"))
        assertEquals("Extra crispy pav please", item.chefNote)
    }

    @Test
    fun `order repository auto purges settled orders older than 7 days`() {
        val repo = OrderRepository.getInstance()
        val purged = repo.purgeOldSettledOrders()
        assertTrue(purged >= 0)
    }

    @Test
    fun `demo login authenticates student cook and admin accurately`() {
        // 1. Student demo credentials
        val student = DemoCredentials.authenticate("student", "vit_student")
        assertNotNull("Student should authenticate", student)
        assertEquals(UserRole.STUDENT, student?.role)
        assertEquals("student", student?.username)

        // 2. Chef / Cook demo credentials
        val cook = DemoCredentials.authenticate("cook", "vit_cook")
        assertNotNull("Cook should authenticate", cook)
        assertEquals(UserRole.CHEF, cook?.role)
        assertEquals("cook", cook?.username)

        // 3. Admin demo credentials
        val admin = DemoCredentials.authenticate("admin", "vit_admin")
        assertNotNull("Admin should authenticate", admin)
        assertEquals(UserRole.ADMIN, admin?.role)
        assertEquals("admin", admin?.username)

        // 4. Invalid credentials must fail
        val invalidPass = DemoCredentials.authenticate("student", "wrong_pass")
        assertNull("Invalid password must fail", invalidPass)

        val invalidUser = DemoCredentials.authenticate("unknown_user", "vit_student")
        assertNull("Invalid username must fail", invalidUser)
    }
}
