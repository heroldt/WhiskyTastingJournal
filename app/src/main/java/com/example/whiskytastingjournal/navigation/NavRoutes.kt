package com.example.whiskytastingjournal.navigation

sealed class NavRoutes(val route: String) {
    data object WhiskyList : NavRoutes("whisky_list")
    data object Statistics : NavRoutes("statistics")
    data object AddWhisky : NavRoutes("add_whisky")
    data object WhiskyDetail : NavRoutes("whisky_detail/{whiskyId}") {
        fun withId(id: String) = "whisky_detail/$id"
    }
    data object EditWhisky : NavRoutes("edit_whisky/{whiskyId}") {
        fun withId(id: String) = "edit_whisky/$id"
    }
    data object AddTasting : NavRoutes("add_tasting/{whiskyId}") {
        fun withWhiskyId(id: String) = "add_tasting/$id"
    }
    data object EditTasting : NavRoutes("edit_tasting/{tastingId}") {
        fun withId(id: String) = "edit_tasting/$id"
    }
}
