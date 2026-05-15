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
import com.example.kutirakushala.app.data.model.Business
import com.example.kutirakushala.app.data.model.Categories
import com.example.kutirakushala.app.ui.theme.*
import com.example.kutirakushala.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBusinessScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val aiText by viewModel.aiGeneratedText.collectAsState()
    val uploadedImageUrl by viewModel.uploadedImageUrl.collectAsState()

    var businessName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var skillArea by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("") }
    var bulkPrice by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Craft") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Apply AI suggestion to description field
    LaunchedEffect(aiText) {
        aiText?.let {
            description = it
            viewModel.clearAiText()
        }
    }

    // Apply uploaded image URL
    LaunchedEffect(uploadedImageUrl) {
        // url will be stored and used at submit time
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            viewModel.uploadImage(it, "profiles")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Business") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = OnSaffron)
                    }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile image picker
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val imageToShow = uploadedImageUrl ?: selectedImageUri?.toString()
                if (imageToShow != null) {
                    AsyncImage(
                        model = imageToShow,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, Modifier.size(40.dp), tint = Saffron)
                        Text("Tap to add business photo", color = Saffron)
                    }
                }
            }

            SectionHeader("Business Details")

            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business / Workshop Name *") },
                leadingIcon = { Icon(Icons.Default.Store, null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = ownerName,
                onValueChange = { ownerName = it },
                label = { Text("Owner / Family Name *") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = skillArea,
                onValueChange = { skillArea = it },
                label = { Text("Skill / Product (e.g. Basket Weaving) *") },
                leadingIcon = { Icon(Icons.Default.Build, null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Category dropdown
            ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
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
                value = location,
                onValueChange = { location = it },
                label = { Text("Village / City, State *") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Contact Phone *") },
                leadingIcon = { Icon(Icons.Default.Phone, null) },
                modifier = Modifier.fillMaxWidth()
            )

            SectionHeader("Production Capacity")

            OutlinedTextField(
                value = capacityText,
                onValueChange = { capacityText = it },
                label = { Text("Daily Capacity (e.g. 500 baskets/day)") },
                leadingIcon = { Icon(Icons.Default.Speed, null) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = bulkPrice,
                onValueChange = { bulkPrice = it },
                label = { Text("Bulk Price (e.g. ₹50/unit min 100 units)") },
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                modifier = Modifier.fillMaxWidth()
            )

            SectionHeader("Business Description")

            // AI generate button
            OutlinedButton(
                onClick = {
                    viewModel.generateDescription(
                        Business(
                            businessName = businessName,
                            skillArea = skillArea,
                            location = location,
                            capacityText = capacityText
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = businessName.isNotBlank() && skillArea.isNotBlank()
            ) {
                Icon(Icons.Default.AutoAwesome, null, tint = Saffron)
                Spacer(Modifier.width(8.dp))
                Text("Generate with Gemini AI", color = Saffron)
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            if (isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = Saffron)
            }

            // Submit
            Button(
                onClick = {
                    val biz = Business(
                        businessName = businessName.trim(),
                        ownerName = ownerName.trim(),
                        skillArea = skillArea.trim(),
                        category = selectedCategory,
                        location = location.trim(),
                        phone = phone.trim(),
                        description = description.trim(),
                        profileImageUrl = uploadedImageUrl ?: "",
                        capacityText = capacityText.trim(),
                        bulkPrice = bulkPrice.trim(),
                        isAcceptingOrders = true
                    )
                    viewModel.saveBusiness(biz) { id -> onSuccess(id) }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = businessName.isNotBlank() && ownerName.isNotBlank() && phone.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Saffron)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text("Register Business", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Saffron)
    HorizontalDivider(color = SaffronLight)
}