package com.kutirakushala.app.ui.navigation

object Routes {
    const val LOGIN        = "login"
    const val REGISTER     = "register"
    const val HOME         = "home"
    const val SEARCH       = "search"
    const val BUSINESS     = "business/{businessId}"
    const val ADD_BUSINESS = "add_business"
    const val ADD_PRODUCT  = "add_product/{businessId}"

    fun business(id: String)       = "business/$id"
    fun addProduct(businessId: String) = "add_product/$businessId"
}