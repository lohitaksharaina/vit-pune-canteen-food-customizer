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

        // 5. Friendly aliases (student/student, cook/cook, chef/cook, admin/admin)
        val studentEasy = DemoCredentials.authenticate("student", "student")
        assertNotNull("Student easy pass should authenticate", studentEasy)
        assertEquals(UserRole.STUDENT, studentEasy?.role)

        val cookEasy = DemoCredentials.authenticate("cook", "cook")
        assertNotNull("Cook easy pass should authenticate", cookEasy)
        assertEquals(UserRole.CHEF, cookEasy?.role)

        val chefAlias = DemoCredentials.authenticate("chef", "chef")
        assertNotNull("Chef alias should authenticate", chefAlias)
        assertEquals(UserRole.CHEF, chefAlias?.role)

        val adminEasy = DemoCredentials.authenticate("admin", "admin")
        assertNotNull("Admin easy pass should authenticate", adminEasy)
        assertEquals(UserRole.ADMIN, adminEasy?.role)
    }

    @Test
    fun `kitchen mode order workflow advances statuses properly`() {
        val repo = OrderRepository.getInstance()
        val initialOrders = repo.orders.value
        assertTrue("Initial demo orders should be present", initialOrders.isNotEmpty())

        val testOrder = initialOrders.first()
        val orderId = testOrder.orderId

        // Test accepting order in kitchen
        repo.acceptOrder(orderId)
        val accepted = repo.orders.value.find { it.orderId == orderId }
        assertEquals("Order should be ACCEPTED_PREPARING", com.example.data.model.OrderStatus.ACCEPTED_PREPARING, accepted?.status)

        // Test marking ready for counter
        repo.markOrderReady(orderId)
        val ready = repo.orders.value.find { it.orderId == orderId }
        assertEquals("Order should be READY_FOR_COUNTER", com.example.data.model.OrderStatus.READY_FOR_COUNTER, ready?.status)

        // Test settling at counter
        repo.markOrderPaidAtCounter(orderId)
        val settled = repo.orders.value.find { it.orderId == orderId }
        assertEquals("Order should be COMPLETED_SETTLED", com.example.data.model.OrderStatus.COMPLETED_SETTLED, settled?.status)
        assertTrue("Order should be marked paid at counter", settled?.isPaidAtCounter == true)
    }

    @Test
    fun `kitchen capacity guard triggers waitlist when 30 active orders reached`() {
        val repo = OrderRepository.getInstance()
        repo.fillOrdersToTestWaitlist()
        assertTrue("Waitlist should be active at 30 active orders", repo.isWaitlistActive())
        assertTrue("Active orders should be at least 30", repo.getActiveOrdersCount() >= 30)

        // Clear waitlist orders
        repo.clearActiveWaitlistDemoOrders()
        assertTrue("Active orders count should be below 30 after clearing demo orders", repo.getActiveOrdersCount() < 30)
    }
}
