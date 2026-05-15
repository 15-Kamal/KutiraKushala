package com.example.kutirakushala.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kutirakushala.app.data.model.Business
import com.example.kutirakushala.app.data.model.Categories
import com.example.kutirakushala.app.ui.theme.*
import com.example.kutirakushala.app.viewmodel.AppViewModel
import com.example.kutirakushala.app.viewmodel.AuthState
import com.example.kutirakushala.app.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    appViewModel: AppViewModel,
    onSuccess: (String) -> Unit,
    onBack: () -> Unit
) {

    val authState        by authViewModel.authState.collectAsState()
    val isLoading        by appViewModel.isLoading.collectAsState()
    val uploadedImageUrl by appViewModel.uploadedImageUrl.collectAsState()
    val aiText           by appViewModel.aiGeneratedText.collectAsState()

    var currentStep by remember { mutableStateOf(0) }

    // Step 0 — Account
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    // Step 1 — Business
    var businessName     by remember { mutableStateOf("") }
    var ownerName        by remember { mutableStateOf("") }
    var phone            by remember { mutableStateOf("") }
    var location         by remember { mutableStateOf("") }
    var skillArea        by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Craft") }
    var capacityText     by remember { mutableStateOf("") }
    var bulkPrice        by remember { mutableStateOf("") }
    var description      by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMsg         by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(aiText) { aiText?.let { description = it; appViewModel.clearAiText() } }

    LaunchedEffect(authState) {
        when (val s = authState) {
            is AuthState.Success -> { currentStep = 1; authViewModel.resetState() }
            is AuthState.Error   -> { errorMsg = s.message; authViewModel.resetState() }
            else -> {}
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedImageUri = it; appViewModel.uploadImage(it, "profiles") }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF8F0))
    ) {
        Column(Modifier.fillMaxSize()) {

            // ── Top bar ──────────────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Saffron, SaffronDark)))
                    .padding(top = 40.dp, bottom = 16.dp, start = 8.dp, end = 16.dp)
            ) {
                IconButton(
                    onClick  = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = OnSaffron)
                }
                Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Register Business",
                        fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnSaffron
                    )
                    Text(
                        "Join the Kutira-Kushala network",
                        fontSize = 12.sp, color = OnSaffron.copy(alpha = 0.85f)
                    )
                }
            }

            // ── Step indicator ────────────────────────────────────────────────────
            RegisterStepIndicator(currentStep = currentStep)

            // ── Form ──────────────────────────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState)
                            slideInHorizontally { it }  + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        else
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                    },
                    label = "step_content"
                ) { step ->

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                        if (step == 0) {
                            // ── STEP 0 ─────────────────────────────────────────
                            RegisterStepCard(
                                title = "Create Your Account",
                                icon  = Icons.Default.AccountCircle
                            ) {
                                LoginField(
                                    value         = email,
                                    onValueChange = { email = it },
                                    label         = "Email Address",
                                    icon          = Icons.Default.AccountCircle,
                                    keyboardType  = KeyboardType.Email
                                )
                                LoginField(
                                    value           = password,
                                    onValueChange   = { password = it },
                                    label           = "Password (min 6 chars)",
                                    icon            = Icons.Default.Lock,
                                    keyboardType    = KeyboardType.Password,
                                    isPassword      = true,
                                    passwordVisible = passwordVisible,
                                    onPasswordToggle = { passwordVisible = !passwordVisible }
                                )
                                LoginField(
                                    value           = confirmPassword,
                                    onValueChange   = { confirmPassword = it },
                                    label           = "Confirm Password",
                                    icon            = Icons.Default.LockReset,
                                    keyboardType    = KeyboardType.Password,
                                    isPassword      = true,
                                    passwordVisible = confirmVisible,
                                    onPasswordToggle = { confirmVisible = !confirmVisible }
                                )
                            }

                        } else {
                            // ── STEP 1 ─────────────────────────────────────────

                            // Photo picker
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                val img = uploadedImageUrl ?: selectedImageUri?.toString()
                                if (img != null) {
                                    AsyncImage(
                                        model            = img,
                                        contentDescription = null,
                                        contentScale     = ContentScale.Crop,
                                        modifier         = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.Store, null,
                                            Modifier.size(40.dp), tint = Saffron
                                        )
                                        Text(
                                            "Tap to add business photo",
                                            color = Saffron, fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            RegisterStepCard(
                                title = "Business Identity",
                                icon  = Icons.Default.Store
                            ) {
                                LoginField(
                                    value         = businessName,
                                    onValueChange = { businessName = it },
                                    label         = "Business / Workshop Name *",
                                    icon          = Icons.Default.Store
                                )
                                LoginField(
                                    value         = ownerName,
                                    onValueChange = { ownerName = it },
                                    label         = "Owner / Family Name *",
                                    icon          = Icons.Default.Person
                                )
                                LoginField(
                                    value         = phone,
                                    onValueChange = { phone = it },
                                    label         = "Contact Phone *",
                                    icon          = Icons.Default.Phone,
                                    keyboardType  = KeyboardType.Phone
                                )
                                LoginField(
                                    value         = location,
                                    onValueChange = { location = it },
                                    label         = "Village / City, State *",
                                    icon          = Icons.Default.LocationOn
                                )
                            }

                            RegisterStepCard(
                                title = "Production Details",
                                icon  = Icons.Default.Build
                            ) {
                                LoginField(
                                    value         = skillArea,
                                    onValueChange = { skillArea = it },
                                    label         = "Skill / Product (e.g. Basket Weaving) *",
                                    icon          = Icons.Default.Build
                                )

                                // Category dropdown
                                ExposedDropdownMenuBox(
                                    expanded          = categoryExpanded,
                                    onExpandedChange  = { categoryExpanded = it }
                                ) {
                                    OutlinedTextField(
                                        value       = selectedCategory,
                                        onValueChange = {},
                                        readOnly    = true,
                                        label       = { Text("Category *") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Category, null, tint = Saffron)
                                        },
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape  = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Saffron,
                                            focusedLabelColor  = Saffron
                                        )
                                    )
                                    ExposedDropdownMenu(
                                        expanded          = categoryExpanded,
                                        onDismissRequest  = { categoryExpanded = false }
                                    ) {
                                        Categories.all.drop(1).forEach { cat ->
                                            DropdownMenuItem(
                                                text    = { Text(cat) },
                                                onClick = {
                                                    selectedCategory = cat
                                                    categoryExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                LoginField(
                                    value         = capacityText,
                                    onValueChange = { capacityText = it },
                                    label         = "Daily Capacity (e.g. 500 baskets/day)",
                                    icon          = Icons.Default.Speed
                                )
                                LoginField(
                                    value         = bulkPrice,
                                    onValueChange = { bulkPrice = it },
                                    label         = "Bulk Price (e.g. ₹50/unit min 100)",
                                    icon          = Icons.Default.CurrencyRupee
                                )
                            }

                            RegisterStepCard(
                                title = "Business Description",
                                icon  = Icons.Default.Description
                            ) {
                                OutlinedButton(
                                    onClick  = {
                                        appViewModel.generateDescription(
                                            Business(
                                                businessName = businessName,
                                                skillArea    = skillArea,
                                                location     = location,
                                                capacityText = capacityText
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled  = businessName.isNotBlank() && skillArea.isNotBlank(),
                                    shape    = RoundedCornerShape(12.dp),
                                    border   = BorderStroke(1.dp, Saffron)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, null, tint = Saffron)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Generate with Gemini AI", color = Saffron)
                                }
                                OutlinedTextField(
                                    value         = description,
                                    onValueChange = { description = it },
                                    label         = { Text("Description") },
                                    modifier      = Modifier.fillMaxWidth(),
                                    minLines      = 3,
                                    shape         = RoundedCornerShape(12.dp),
                                    colors        = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Saffron,
                                        focusedLabelColor  = Saffron
                                    )
                                )
                            }
                        }
                    }
                }

                // ── Error ──────────────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = errorMsg != null,
                    enter   = expandVertically() + fadeIn(),
                    exit    = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CapacityRed.copy(alpha = 0.08f))
                            .padding(12.dp),
                        verticalAlignment      = Alignment.CenterVertically,
                        horizontalArrangement  = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, null, Modifier.size(16.dp), tint = CapacityRed)
                        Text(
                            text     = errorMsg ?: "",
                            fontSize = 12.sp,
                            color    = CapacityRed,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick  = { errorMsg = null },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = CapacityRed)
                        }
                    }
                }

                // Loading bar
                if (isLoading || authState is AuthState.Loading) {
                    LinearProgressIndicator(
                        modifier   = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color      = Saffron,
                        trackColor = SaffronLight
                    )
                }

                // ── Action button ──────────────────────────────────────────────────
                Button(
                    onClick = {
                        errorMsg = null
                        if (currentStep == 0) {
                            authViewModel.registerWithEmail(email, password, confirmPassword)
                        } else {
                            if (businessName.isBlank() || ownerName.isBlank() || phone.isBlank()) {
                                errorMsg = "Please fill in all required fields (*)"
                                return@Button
                            }
                            val biz = Business(
                                businessName    = businessName.trim(),
                                ownerName       = ownerName.trim(),
                                skillArea       = skillArea.trim(),
                                category        = selectedCategory,
                                location        = location.trim(),
                                phone           = phone.trim(),
                                description     = description.trim(),
                                profileImageUrl = uploadedImageUrl ?: "",
                                capacityText    = capacityText.trim(),
                                bulkPrice       = bulkPrice.trim(),
                                isAcceptingOrders = true
                            )
                            appViewModel.saveBusiness(biz) { id -> onSuccess(id) }
                        }
                    },
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled   = !isLoading && authState !is AuthState.Loading,
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Saffron),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        if (currentStep == 0) Icons.Default.NavigateNext
                        else Icons.Default.Check,
                        null,
                        Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (currentStep == 0) "Next: Business Details" else "Register Business",
                        fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                }

                if (currentStep == 1) {
                    TextButton(
                        onClick  = { currentStep = 0 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.NavigateBefore, null, Modifier.size(16.dp))
                        Text("← Back to Account Setup", color = Saffron)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Step Indicator ──────────────────────────────────────────────────────────────
@Composable
fun RegisterStepIndicator(currentStep: Int) {
    val steps = listOf("Account Setup", "Business Details")
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isDone   = index < currentStep
            val isActive = index == currentStep

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isDone   -> Leaf
                                isActive -> Saffron
                                else     -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, null, Modifier.size(14.dp), tint = OnSaffron)
                    } else {
                        Text(
                            "${index + 1}",
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color      = if (isActive) OnSaffron
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    fontSize   = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color      = if (isActive) Saffron
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (index < steps.lastIndex) {
                HorizontalDivider(
                    modifier  = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    color     = if (isDone) Leaf
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.5.dp
                )
            }
        }
    }
}

// ── Section Card ────────────────────────────────────────────────────────────────
@Composable
fun RegisterStepCard(
    title:   String,
    icon:    ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, null, Modifier.size(18.dp), tint = Saffron)
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Saffron)
            }
            HorizontalDivider(color = SaffronLight)
            content()
        }
    }
}