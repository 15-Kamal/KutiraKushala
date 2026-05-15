package com.example.kutirakushala.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.example.kutirakushala.app.data.model.Business
import com.example.kutirakushala.app.data.model.Product
import kotlinx.coroutines.tasks.await
import java.util.UUID

class BusinessRepository {
    private val db         = FirebaseFirestore.getInstance()
    private val storage    = FirebaseStorage.getInstance()
    private val businesses = db.collection("businesses")
    private val products   = db.collection("products")

    // ── Public browsing ─────────────────────────────────────────────────────────

    suspend fun getAllBusinesses(): List<Business> =
        businesses.orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().toObjects(Business::class.java)

    suspend fun getBusinessesByCategory(category: String): List<Business> {
        if (category == "All") return getAllBusinesses()
        return businesses.whereEqualTo("category", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get().await().toObjects(Business::class.java)
    }

    suspend fun searchBusinesses(query: String): List<Business> =
        getAllBusinesses().filter { biz ->
            biz.businessName.contains(query, ignoreCase = true) ||
                    biz.skillArea.contains(query, ignoreCase = true)    ||
                    biz.location.contains(query, ignoreCase = true)     ||
                    biz.category.contains(query, ignoreCase = true)
        }

    suspend fun getBusinessById(id: String): Business? =
        businesses.document(id).get().await().toObject(Business::class.java)

    // ── Owner lookup — fetch the business belonging to this uid ─────────────────
    suspend fun getMyBusiness(ownerUid: String): Business? {
        val results = businesses
            .whereEqualTo("ownerUid", ownerUid)
            .limit(1)
            .get().await()
            .toObjects(Business::class.java)
        return results.firstOrNull()
    }

    // ── Save / update ───────────────────────────────────────────────────────────

    suspend fun saveBusiness(business: Business): String {
        val id = business.id.ifEmpty { UUID.randomUUID().toString() }
        businesses.document(id).set(business.copy(id = id)).await()
        return id
    }

    suspend fun updateCapacity(businessId: String, available: Int, accepting: Boolean) {
        businesses.document(businessId)
            .update(mapOf(
                "capacityAvailable"  to available,
                "isAcceptingOrders"  to accepting
            )).await()
    }

    // ── Products ────────────────────────────────────────────────────────────────

    suspend fun getProductsForBusiness(businessId: String): List<Product> =
        products.whereEqualTo("businessId", businessId)
            .get().await().toObjects(Product::class.java)

    suspend fun saveProduct(product: Product): String {
        val id = product.id.ifEmpty { UUID.randomUUID().toString() }
        products.document(id).set(product.copy(id = id)).await()
        return id
    }

    suspend fun deleteProduct(productId: String) {
        products.document(productId).delete().await()
    }

    // ── Image upload ────────────────────────────────────────────────────────────

    suspend fun uploadImage(uri: Uri, folder: String = "images"): String {
        val ref = storage.reference.child("$folder/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}