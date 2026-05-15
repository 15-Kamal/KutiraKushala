package com.example.kutirakushala.app.data.model

data class Business(
    val id: String = "",
    val ownerUid: String = "",          // Firebase Auth UID of the registering user
    val ownerName: String = "",
    val businessName: String = "",
    val skillArea: String = "",
    val category: String = "",
    val location: String = "",
    val phone: String = "",
    val description: String = "",
    val profileImageUrl: String = "",
    val capacityText: String = "",
    val capacityAvailable: Int = 0,
    val isAcceptingOrders: Boolean = true,
    val bulkPrice: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class Product(
    val id: String = "",
    val businessId: String = "",
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val wholesalePrice: String = "",
    val minOrderQty: Int = 10,
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

object Categories {
    val all = listOf("All", "Food", "Craft", "Textile", "Agarbatti", "Paper", "Other")
}