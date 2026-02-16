package com.example.whiskytastingjournal.navigation

sealed class NavRoutes(val route: String) {
    data object TastingList : NavRoutes("tasting_list")
    data object AddTasting : NavRoutes("add_tasting")
    data object TastingDetail : NavRoutes("tasting_detail/{tastingId}") {
        fun withId(id: String) = "tasting_detail/$id"
    }
    data object EditTasting : NavRoutes("edit_tasting/{tastingId}") {
        fun withId(id: String) = "edit_tasting/$id"
    }
}
