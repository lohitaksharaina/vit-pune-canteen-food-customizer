package com.example.data.model

enum class MenuCategory(val title: String, val iconName: String) {
    ALL("All Items", "Restaurant"),
    PUNERI_NASHTA("Nashta & Snacks", "BakeryDining"),
    SANDWICH_MAGGI("Sandwich & Maggi", "Fastfood"),
    MEALS_THALI("Student Meals & Thali", "DinnerDining"),
    SOUTH_INDIAN("South Indian", "BreakfastDining"),
    RICE_NOODLES("Rice & Noodles", "RamenDining"),
    BEVERAGES("Chai & Cold Coffee", "LocalCafe"),
    SWEETS("Desserts", "Icecream")
}

data class MenuItem(
    val id: String,
    val name: String,
    val category: MenuCategory,
    val description: String,
    val price: Double,
    val isVeg: Boolean,
    val drawableResId: Int? = null,
    val imageUrl: String? = null,
    val spiciness: Int = 1, // 0 = mild, 1 = medium, 2 = spicy, 3 = extra fiery
    val badge: String? = null,
    val dislikableIngredients: List<String> = defaultDislikableIngredients()
) {
    companion object {
        fun defaultDislikableIngredients(): List<String> = listOf(
            "Onions (Kanda)",
            "Garlic (Lasun)",
            "Green Chillies (Mirchi)",
            "Fresh Coriander (Kothimbir)",
            "Mustard Seeds (Mohari)",
            "Roasted Peanuts (Shengdana)",
            "Curry Leaves (Kadi Patta)",
            "Spicy Rassa / Tarri",
            "Dairy Butter / Cheese",
            "Ginger (Aale)"
        )
    }
}
