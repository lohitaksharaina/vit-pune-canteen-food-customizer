package com.example.data

import com.example.R
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem

object MenuCatalog {
    val items: List<MenuItem> = listOf(
        // Nashta & Snacks
        MenuItem(
            id = "vit_snack_01",
            name = "Puneri Special Misal Pav",
            category = MenuCategory.PUNERI_NASHTA,
            description = "Spicy sprouted moth bean curry (kat/rassa) topped with crunchy Kolhapuri farsan, diced onions, fresh coriander, lemon, and 2 buttered pav.",
            price = 50.0,
            isVeg = true,
            drawableResId = R.drawable.vit_misal_pav_1794233551945,
            imageUrl = "https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80",
            spiciness = 3,
            badge = "VIT Student Favorite",
            dislikableIngredients = listOf(
                "Spicy Rassa / Tarri",
                "Onions (Kanda)",
                "Fresh Coriander (Kothimbir)",
                "Farsan / Sev",
                "Garlic Paste",
                "Lemon Juice"
            )
        ),
        MenuItem(
            id = "vit_snack_02",
            name = "Batata Vada Pav (2 pcs)",
            category = MenuCategory.PUNERI_NASHTA,
            description = "Hot crispy golden potato fritters served in fresh pav with spicy dry red garlic chutney, green mint thecha, and fried salted green chillies.",
            price = 25.0,
            isVeg = true,
            drawableResId = R.drawable.vit_vada_pav_1794233566141,
            imageUrl = "https://images.unsplash.com/photo-1606491956689-2ea866880c84?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            badge = "College Daily Classic",
            dislikableIngredients = listOf(
                "Dry Garlic Chutney",
                "Green Chillies (Mirchi)",
                "Mustard Seeds (Mohari)",
                "Curry Leaves (Kadi Patta)",
                "Green Thecha"
            )
        ),
        MenuItem(
            id = "vit_snack_03",
            name = "Kanda Poha (Bibwewadi Special)",
            category = MenuCategory.PUNERI_NASHTA,
            description = "Traditional Maharashtrian flattened rice cooked with fried onions, mustard seeds, turmeric, roasted crunchy peanuts, fresh coriander and nylon sev.",
            price = 30.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1630409346824-4f0e7b080087?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Morning Nashta",
            dislikableIngredients = listOf(
                "Onions (Kanda)",
                "Roasted Peanuts (Shengdana)",
                "Green Chillies",
                "Fresh Coriander",
                "Mustard Seeds",
                "Nylon Sev"
            )
        ),
        MenuItem(
            id = "vit_snack_04",
            name = "Sabudana Khichdi (Upvas Special)",
            category = MenuCategory.PUNERI_NASHTA,
            description = "Fluffy sago pearls sautéed with roasted crushed peanuts, cumin seeds, green chillies, and potatoes, served with fresh sweetened curd.",
            price = 45.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1601050690187-573574972e0a?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Fast & Upvas",
            dislikableIngredients = listOf(
                "Roasted Peanuts",
                "Green Chillies",
                "Cumin Seeds (Jeera)",
                "Fresh Curd (Dahi)"
            )
        ),
        MenuItem(
            id = "vit_snack_05",
            name = "Crispy Samosa Pav (2 pcs)",
            category = MenuCategory.PUNERI_NASHTA,
            description = "Golden flaky pastry filled with cumin-spiced mashed potatoes and green peas, served with sweet tamarind and spicy mint coriander chutney.",
            price = 35.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            badge = "Evening Snack",
            dislikableIngredients = listOf(
                "Green Chillies",
                "Coriander Seeds",
                "Whole Spices",
                "Sweet Tamarind Chutney",
                "Mint Chutney"
            )
        ),

        // Sandwich & Maggi
        MenuItem(
            id = "vit_ff_01",
            name = "Veg Cheese Grilled Sandwich",
            category = MenuCategory.SANDWICH_MAGGI,
            description = "Triple layered bread stuffed with cucumber, juicy tomatoes, capsicum, mint butter spread, topped with melted Amul cheese, grilled till crisp.",
            price = 70.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Campus Bestseller",
            dislikableIngredients = listOf(
                "Amul Cheese",
                "Capsicum / Bell Peppers",
                "Onions",
                "Chaat Masala",
                "Mint Chutney Butter"
            )
        ),
        MenuItem(
            id = "vit_ff_02",
            name = "Double Masala Maggi",
            category = MenuCategory.SANDWICH_MAGGI,
            description = "Steaming hot canteen special 2-minute noodles tossed with butter, diced onions, green chillies, and double tastemaker seasoning.",
            price = 40.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1612927601601-6638404737ce?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            badge = "Hostel & Canteen Hero",
            dislikableIngredients = listOf(
                "Onions",
                "Green Chillies",
                "Extra Tastemaker",
                "Dairy Butter",
                "Coriander"
            )
        ),
        MenuItem(
            id = "vit_ff_03",
            name = "Cheese Masala Maggi",
            category = MenuCategory.SANDWICH_MAGGI,
            description = "Double masala Maggi smothered in generous grated Amul cheese with hot butter glaze.",
            price = 55.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1612927601601-6638404737ce?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Cheesy Delight",
            dislikableIngredients = listOf(
                "Grated Cheese",
                "Onions",
                "Green Chillies",
                "Dairy Butter"
            )
        ),
        MenuItem(
            id = "vit_ff_04",
            name = "Pav Bhaji (Amul Butter Special)",
            category = MenuCategory.SANDWICH_MAGGI,
            description = "Slow-cooked mashed vegetable curry blended with authentic Pune spices, served with 2 warm butter pavs and chopped onion-lemon salad.",
            price = 90.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1606491956689-2ea866880c84?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            badge = "Special Treat",
            dislikableIngredients = listOf(
                "Excess Butter",
                "Raw Onions",
                "Capsicum",
                "Garlic Paste",
                "Fresh Coriander"
            )
        ),
        MenuItem(
            id = "vit_ff_05",
            name = "Crispy Bread Pattice (2 pcs)",
            category = MenuCategory.SANDWICH_MAGGI,
            description = "Crispy triangular gram flour coated bread pockets stuffed with spiced potato masala, served with green chilli thecha.",
            price = 35.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            dislikableIngredients = listOf(
                "Green Thecha",
                "Mustard Seeds",
                "Onions",
                "Curry Leaves"
            )
        ),

        // Student Meals & Thali
        MenuItem(
            id = "vit_meal_01",
            name = "VIT Student Special Thali",
            category = MenuCategory.MEALS_THALI,
            description = "Complete daily student lunch: 3 soft hot chapatis, Paneer butter masala, dry seasonal sabzi, Dal tadka, fragrant Jeera rice, roasted papad, pickle & sweet gulab jamun.",
            price = 95.0,
            isVeg = true,
            drawableResId = R.drawable.vit_student_thali_1794233595020,
            imageUrl = "https://images.unsplash.com/photo-1610057099443-fde8c4d50f91?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Full Lunch Meal",
            dislikableIngredients = listOf(
                "Garlic (Lasun)",
                "Onions (Kanda)",
                "Dairy Butter / Cream",
                "Mustard Seeds",
                "Coriander",
                "Spicy Pickle"
            )
        ),
        MenuItem(
            id = "vit_meal_02",
            name = "Mini Lunch Plate (Daily Special)",
            category = MenuCategory.MEALS_THALI,
            description = "2 Chapatis, homestyle Dal fry, daily vegetable bhaji, steamed basmati rice, and lemon pickle.",
            price = 65.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Quick Lunch",
            dislikableIngredients = listOf(
                "Garlic",
                "Onions",
                "Spicy Tadka",
                "Pickle"
            )
        ),
        MenuItem(
            id = "vit_meal_03",
            name = "Dal Khichdi Tadka with Ghee",
            category = MenuCategory.MEALS_THALI,
            description = "Hearty comfort dish of yellow lentils and rice tempered with desi ghee, cumin, burnt garlic, and dry red chillies.",
            price = 75.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1546833999-b9f581a1996d?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Comfort Food",
            dislikableIngredients = listOf(
                "Burnt Garlic",
                "Desi Ghee",
                "Dry Red Chillies",
                "Cumin (Jeera)"
            )
        ),
        MenuItem(
            id = "vit_meal_04",
            name = "Paneer Masala with 3 Chapati",
            category = MenuCategory.MEALS_THALI,
            description = "Tender cottage cheese cubes simmered in spiced onion-tomato gravy, served with 3 warm chapatis.",
            price = 85.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1567188040759-fb8a883dc6d8?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            dislikableIngredients = listOf(
                "Dairy Cream",
                "Onion Chunks",
                "Garlic Paste",
                "Fresh Coriander"
            )
        ),
        MenuItem(
            id = "vit_meal_05",
            name = "Veg Pulao with Boondi Raita",
            category = MenuCategory.MEALS_THALI,
            description = "Long grain basmati rice tossed with fresh carrots, green beans, peas, and whole spices, served with seasoned boondi curd.",
            price = 70.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            dislikableIngredients = listOf(
                "Whole Spices (Cloves/Cardamom)",
                "Fried Onions",
                "Boondi Raita",
                "Green Peas"
            )
        ),

        // South Indian
        MenuItem(
            id = "vit_si_01",
            name = "Crispy Butter Masala Dosa",
            category = MenuCategory.SOUTH_INDIAN,
            description = "Thin fermented rice crepe roasted on cast iron with butter, stuffed with spiced mustard potato filling, served with piping hot sambar & coconut chutney.",
            price = 60.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1668236543090-82eba5ee5976?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Fresh Off The Tawa",
            dislikableIngredients = listOf(
                "Mustard Seeds",
                "Curry Leaves",
                "Onions in Potato Bhaji",
                "Excess Butter",
                "Coconut Chutney"
            )
        ),
        MenuItem(
            id = "vit_si_02",
            name = "Steamed Idli Sambar (2 pcs)",
            category = MenuCategory.SOUTH_INDIAN,
            description = "Soft steamed rice and urad dal cakes served with spiced lentil vegetable sambar and freshly grated coconut chutney.",
            price = 40.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1589301760014-d929f3979dbc?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            badge = "Healthy Nashta",
            dislikableIngredients = listOf(
                "Curry Leaves",
                "Mustard Seeds",
                "Coconut Chutney",
                "Drumsticks in Sambar"
            )
        ),
        MenuItem(
            id = "vit_si_03",
            name = "Crispy Medu Vada (2 pcs)",
            category = MenuCategory.SOUTH_INDIAN,
            description = "Golden crispy savoury lentil doughnuts served with hot drumstick sambar and fresh coconut dip.",
            price = 45.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            dislikableIngredients = listOf(
                "Black Pepper in Vada",
                "Curry Leaves",
                "Coconut Chutney",
                "Sambar"
            )
        ),

        // Rice & Noodles
        MenuItem(
            id = "vit_ch_01",
            name = "Veg Hakka Noodles",
            category = MenuCategory.RICE_NOODLES,
            description = "Wok-tossed noodles with shredded cabbage, bell peppers, carrots, spring onions in soy and garlic sauce.",
            price = 80.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1585032226651-759b368d7246?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            badge = "Student Fast Food",
            dislikableIngredients = listOf(
                "Garlic",
                "Capsicum / Bell Peppers",
                "Spring Onions",
                "Vinegar",
                "Soy Sauce"
            )
        ),
        MenuItem(
            id = "vit_ch_02",
            name = "Schezwan Fried Rice",
            category = MenuCategory.RICE_NOODLES,
            description = "Fiery basmati rice tossed with crunchy vegetables in hot red Schezwan pepper sauce and aromatic garlic.",
            price = 85.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1603133872878-684f208fb84b?auto=format&fit=crop&w=600&q=80",
            spiciness = 3,
            badge = "Spicy Wok",
            dislikableIngredients = listOf(
                "Spicy Schezwan Sauce",
                "Garlic",
                "Spring Onions",
                "Capsicum"
            )
        ),
        MenuItem(
            id = "vit_ch_03",
            name = "Veg Manchurian Combo",
            category = MenuCategory.RICE_NOODLES,
            description = "Crisp vegetable dumplings in dark savoury soy-garlic gravy served with hot veg fried rice.",
            price = 110.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1569058242252-623df46b5025?auto=format&fit=crop&w=600&q=80",
            spiciness = 2,
            badge = "Canteen Combo",
            dislikableIngredients = listOf(
                "Garlic",
                "Spring Onions",
                "Thick Cornstarch Gravy",
                "Soy Sauce"
            )
        ),

        // Chai & Cold Coffee
        MenuItem(
            id = "vit_bev_01",
            name = "Pune Special Thick Cold Coffee",
            category = MenuCategory.BEVERAGES,
            description = "The legendary Pune college student beverage: chilled thick blended cold coffee topped with rich coffee foam and Hershey's chocolate drizzle.",
            price = 40.0,
            isVeg = true,
            drawableResId = R.drawable.vit_cold_coffee_1794233579575,
            imageUrl = "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            badge = "Pune College Legend",
            dislikableIngredients = listOf(
                "Chocolate Syrup Drizzle",
                "Excess Sugar",
                "Extra Ice"
            )
        ),
        MenuItem(
            id = "vit_bev_02",
            name = "Kadak Special Cutting Chai",
            category = MenuCategory.BEVERAGES,
            description = "Piping hot aromatic milk tea brewed with fresh crushed ginger, green cardamom, and high-grown CTC tea.",
            price = 15.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            badge = "Chai Tapri Special",
            dislikableIngredients = listOf(
                "Fresh Ginger (Aale)",
                "Cardamom (Elaichi)",
                "Excess Sugar"
            )
        ),
        MenuItem(
            id = "vit_bev_03",
            name = "South Indian Filter Coffee",
            category = MenuCategory.BEVERAGES,
            description = "Freshly brewed chicory coffee decoction frothed with boiling whole milk in a stainless steel cup.",
            price = 20.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            dislikableIngredients = listOf(
                "Excess Sugar",
                "Thick Cream"
            )
        ),
        MenuItem(
            id = "vit_bev_04",
            name = "Masala Taak (Chilled Buttermilk)",
            category = MenuCategory.BEVERAGES,
            description = "Traditional Maharashtrian churned curd spiced with roasted cumin, rock salt, ginger, and fresh coriander.",
            price = 20.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=600&q=80",
            spiciness = 1,
            badge = "Cooling Digestif",
            dislikableIngredients = listOf(
                "Roasted Cumin (Jeera)",
                "Black Rock Salt",
                "Fresh Coriander",
                "Ginger"
            )
        ),
        MenuItem(
            id = "vit_bev_05",
            name = "Fresh Lime Soda (Sweet & Salt)",
            category = MenuCategory.BEVERAGES,
            description = "Chilled sparkling soda water infused with freshly squeezed lemon juice, black salt, and sugar syrup.",
            price = 30.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            dislikableIngredients = listOf(
                "Black Salt",
                "Excess Sugar",
                "Extra Ice"
            )
        ),
        MenuItem(
            id = "vit_bev_06",
            name = "Kokum Sharbat (Maharashtrian Cooler)",
            category = MenuCategory.BEVERAGES,
            description = "Refreshing tangy-sweet kokum extract cooler with roasted cumin and crushed ice.",
            price = 25.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1536935338788-846bb9981813?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            badge = "Konkan Special",
            dislikableIngredients = listOf(
                "Cumin Powder",
                "Extra Ice"
            )
        ),

        // Desserts
        MenuItem(
            id = "vit_sw_01",
            name = "Hot Gulab Jamun (2 pcs)",
            category = MenuCategory.SWEETS,
            description = "Warm fried mawa dumplings soaked in saffron and cardamom sugar syrup.",
            price = 35.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1589119908995-c6837fa14848?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            badge = "Sweet Tooth",
            dislikableIngredients = listOf(
                "Cardamom Powder",
                "Excess Sugar Syrup"
            )
        ),
        MenuItem(
            id = "vit_sw_02",
            name = "Vanilla & Chocolate Ice Cream Cup",
            category = MenuCategory.SWEETS,
            description = "Chilled dessert cup to wrap up canteen lunch or snacks.",
            price = 30.0,
            isVeg = true,
            imageUrl = "https://images.unsplash.com/photo-1497034825429-c343d7c6a68f?auto=format&fit=crop&w=600&q=80",
            spiciness = 0,
            dislikableIngredients = listOf(
                "Chocolate Sauce"
            )
        )
    )
}
