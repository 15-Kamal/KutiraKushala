package com.example.kutirakushala.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kutirakushala.app.data.model.Categories
import com.example.kutirakushala.app.data.model.Product
import com.example.kutirakushala.app.ui.theme.*
import com.example.kutirakushala.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    businessId: String,
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val aiText by viewModel.aiGeneratedText.collectAsState()
    val uploadedImageUrl by viewModel.uploadedImageUrl.collectAsState()
    val business by viewModel.selectedBusiness.collectAsState()

    var productName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var wholesalePrice by remember { mutableStateOf("") }
    var minOrderQty by remember { mutableStateOf("10") }
    var selectedCategory by remember { mutableStateOf(business?.category ?: "Craft") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var aiSuggestion by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(aiText) {
        aiText?.let {
            aiSuggestion = it
            viewModel.clearAiText()
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadImage(it, "products")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back", tint = OnSaffron) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Saffron, titleContentColor = OnSaffron)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Product image
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageToShow = uploadedImageUrl ?: selectedImageUri?.toString()
                if (imageToShow != null) {
                    AsyncImage(model = imageToShow, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(40.dp), tint = Saffron)
                        Text("Tap to add product photo", color = Saffron)
                    }
                }
            }

            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name *") },
                leadingIcon = { Icon(Icons.Default.Inventory2, null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Category
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    Categories.all.drop(1).forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = { selectedCategory = cat; categoryExpanded = false })
                    }
                }
            }

            OutlinedTextField(
                value = minOrderQty,
                onValueChange = { minOrderQty = it.filter { c -> c.isDigit() } },
                label = { Text("Minimum Order Quantity") },
                leadingIcon = { Icon(Icons.Default.ShoppingCart, null) },
                modifier = Modifier.fillMaxWidth()
            )

            // AI price suggestion
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        viewModel.suggestProductPricing(productName, selectedCategory, minOrderQty.toIntOrNull() ?: 10)
                    },
                    enabled = productName.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Saffron)
                    Spacer(Modifier.width(4.dp))
                    Text("AI Price", color = Saffron)
                }
                OutlinedButton(
                    onClick = {
                        viewModel.generateProductDescription(productName, selectedCategory)
                    },
                    enabled = productName.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Saffron)
                    Spacer(Modifier.width(4.dp))
                    Text("AI Desc", color = Saffron)
                }
            }

            // Show AI suggestion
            if (aiSuggestion.isNotEmpty()) {
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SaffronLight),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = Saffron, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Gemini Suggestion", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Saffron)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(aiSuggestion, fontSize = 13.sp)
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = {
                                // If it's a price suggestion, apply to price field; otherwise description
                                if (aiSuggestion.contains("₹")) {
                                    val priceMatch = Regex("₹[\\d,–-]+(?:/unit)?").find(aiSuggestion)
                                    wholesalePrice = priceMatch?.value ?: aiSuggestion
                                } else {
                                    description = aiSuggestion
                                }
                                aiSuggestion = ""
                            }) { Text("Apply", color = Saffron) }
                            TextButton(onClick = { aiSuggestion = "" }) { Text("Dismiss") }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = wholesalePrice,
                onValueChange = { wholesalePrice = it },
                label = { Text("Wholesale Price (e.g. ₹45/unit)") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Product Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Saffron)

            Button(
                onClick = {
                    val product = Product(
                        businessId = businessId,
                        name = productName.trim(),
                        description = description.trim(),
                        imageUrl = uploadedImageUrl ?: "",
                        wholesalePrice = wholesalePrice.trim(),
                        minOrderQty = minOrderQty.toIntOrNull() ?: 10,
                        category = selectedCategory
                    )
                    viewModel.saveProduct(product) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = productName.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Add to Catalog", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}