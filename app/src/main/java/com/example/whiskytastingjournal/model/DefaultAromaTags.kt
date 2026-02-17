package com.example.whiskytastingjournal.model

object DefaultAromaTags {

    fun all(): List<AromaTag> = buildList {
        // Fruit
        cat("Fruit", "Green Apple", "Citrus", "Lemon", "Orange Peel",
            "Dried Fruit", "Tropical Fruit", "Berry", "Stone Fruit", "Banana", "Raisin")
        // Malt / Grain
        cat("Malt/Grain", "Malt", "Barley", "Cereal", "Biscuit", "Bread", "Toast")
        // Sweet (Honey / Caramel)
        cat("Sweet", "Honey", "Caramel", "Toffee", "Butterscotch", "Brown Sugar", "Maple Syrup")
        // Wood / Vanilla
        cat("Wood/Vanilla", "Vanilla", "Oak", "Cedar", "Sandalwood", "Coconut")
        // Sherry / Wine
        cat("Sherry/Wine", "Sherry", "Red Wine", "Port", "Muscat", "Brandy")
        // Smoke / Peat
        cat("Smoke/Peat", "Peat", "Smoke", "Campfire", "Ash", "Iodine", "Maritime", "Tar")
        // Spice / Herbal
        cat("Spice/Herbal", "Cinnamon", "Black Pepper", "Ginger", "Clove",
            "Nutmeg", "Mint", "Eucalyptus", "Liquorice")
        // Nut / Chocolate
        cat("Nut/Chocolate", "Almond", "Walnut", "Hazelnut", "Dark Chocolate", "Cocoa", "Coffee")
        // Off-notes
        cat("Off-notes", "Sulphur", "Rubber", "Cardboard", "Soapy", "Metallic")
    }

    private fun MutableList<AromaTag>.cat(category: String, vararg names: String) {
        names.forEach { name ->
            add(AromaTag(name = name, category = category))
        }
    }
}
