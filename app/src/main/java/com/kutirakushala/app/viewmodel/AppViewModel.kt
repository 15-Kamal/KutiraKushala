package com.kutirakushala.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.kutirakushala.app.data.ai.GeminiHelper
import com.kutirakushala.app.data.model.Business
import com.kutirakushala.app.data.model.Product
import com.kutirakushala.app.data.repository.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel : ViewModel() {

    private val repo = BusinessRepository()
    private val auth = FirebaseAuth.getInstance()

    // ── Current user UID (used for owner checks and my-business lookup) ─────────
    val currentUserUid: String? get() = auth.currentUser?.uid

    // ── Public business list ─────────────────────────────────────────────────────
    private val _businesses = MutableStateFlow<List<Business>>(emptyList())
    val businesses: StateFlow<List<Business>> = _businesses.asStateFlow()

    // ── The owner's own business (null = not registered yet) ─────────────────────
    private val _myBusiness = MutableStateFlow<Business?>(null)
    val myBusiness: StateFlow<Business?> = _myBusiness.asStateFlow()

    // ── Currently opened business profile ────────────────────────────────────────
    private val _selectedBusiness = MutableStateFlow<Business?>(null)
    val selectedBusiness: StateFlow<Business?> = _selectedBusiness.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    // ── UI state ─────────────────────────────────────────────────────────────────
    private val _isLoading        = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage     = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _aiGeneratedText  = MutableStateFlow<String?>(null)
    val aiGeneratedText: StateFlow<String?> = _aiGeneratedText.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery      = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _uploadedImageUrl = MutableStateFlow<String?>(null)
    val uploadedImageUrl: StateFlow<String?> = _uploadedImageUrl.asStateFlow()

    init {
        loadBusinesses()
        // Load owner's business if they are logged in
        currentUserUid?.let { loadMyBusiness(it) }
    }

    // ── Owner check helper ────────────────────────────────────────────────────────
    fun isOwnerOf(business: Business): Boolean =
        currentUserUid != null && currentUserUid == business.ownerUid

    // ── Fetch the business registered by the current logged-in owner ─────────────
    fun loadMyBusiness(uid: String = currentUserUid ?: "") {
        if (uid.isEmpty()) return
        viewModelScope.launch {
            try {
                _myBusiness.value = repo.getMyBusiness(uid)
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    // ── Public browse ─────────────────────────────────────────────────────────────
    fun loadBusinesses(category: String = "All") {
        viewModelScope.launch {
            _isLoading.value = true
            try { _businesses.value = repo.getBusinessesByCategory(category) }
            catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _businesses.value = if (query.isBlank())
                    repo.getBusinessesByCategory(_selectedCategory.value)
                else repo.searchBusinesses(query)
            } catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
        loadBusinesses(category)
    }

    // ── Load a single business profile ────────────────────────────────────────────
    fun loadBusiness(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _selectedBusiness.value = repo.getBusinessById(id)
                _selectedBusiness.value?.let { _products.value = repo.getProductsForBusiness(it.id) }
            } catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    // ── Save business (attaches ownerUid automatically) ───────────────────────────
    fun saveBusiness(business: Business, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val withOwner = business.copy(ownerUid = currentUserUid ?: "")
                val id = repo.saveBusiness(withOwner)
                _myBusiness.value = withOwner.copy(id = id)   // update local myBusiness
                loadBusiness(id)
                onSuccess(id)
            } catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    // ── Capacity update (owner only in UI — enforced by Firestore rules too) ──────
    fun updateCapacity(businessId: String, available: Int, accepting: Boolean) {
        viewModelScope.launch {
            try {
                repo.updateCapacity(businessId, available, accepting)
                val updated = _selectedBusiness.value?.copy(
                    capacityAvailable = available, isAcceptingOrders = accepting)
                _selectedBusiness.value = updated
                // Also update myBusiness if it's the same
                if (_myBusiness.value?.id == businessId) _myBusiness.value = updated
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    // ── Products ──────────────────────────────────────────────────────────────────
    fun saveProduct(product: Product, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repo.saveProduct(product)
                _selectedBusiness.value?.let { _products.value = repo.getProductsForBusiness(it.id) }
                onSuccess()
            } catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                repo.deleteProduct(productId)
                _products.value = _products.value.filter { it.id != productId }
            } catch (e: Exception) { _errorMessage.value = e.message }
        }
    }

    // ── Image upload ──────────────────────────────────────────────────────────────
    fun uploadImage(uri: Uri, folder: String = "images") {
        viewModelScope.launch {
            _isLoading.value = true
            try { _uploadedImageUrl.value = repo.uploadImage(uri, folder) }
            catch (e: Exception) { _errorMessage.value = e.message }
            finally { _isLoading.value = false }
        }
    }

    // ── Gemini AI ─────────────────────────────────────────────────────────────────
    fun generateDescription(business: Business) {
        viewModelScope.launch {
            _isLoading.value = true
            _aiGeneratedText.value = GeminiHelper.generateBusinessDescription(
                business.businessName, business.skillArea, business.location, business.capacityText)
            _isLoading.value = false
        }
    }

    fun suggestProductPricing(productName: String, category: String, minQty: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _aiGeneratedText.value = GeminiHelper.suggestPricing(productName, category, minQty)
            _isLoading.value = false
        }
    }

    fun generateProductDescription(productName: String, category: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _aiGeneratedText.value = GeminiHelper.generateProductDescription(productName, category)
            _isLoading.value = false
        }
    }

    fun clearUploadedImage() { _uploadedImageUrl.value = null }
    fun clearAiText()        { _aiGeneratedText.value = null }
    fun clearError()         { _errorMessage.value = null }
}