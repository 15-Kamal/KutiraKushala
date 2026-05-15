package com.kutirakushala.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kutirakushala.app.data.model.Business
import com.kutirakushala.app.data.model.Categories
import com.kutirakushala.app.ui.theme.CapacityRed
import com.kutirakushala.app.ui.theme.Clay
import com.kutirakushala.app.ui.theme.Cream
import com.kutirakushala.app.ui.theme.Leaf
import com.kutirakushala.app.ui.theme.OnSaffron
import com.kutirakushala.app.ui.theme.Saffron
import com.kutirakushala.app.ui.theme.SaffronLight
import com.kutirakushala.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel:        AppViewModel,
    onBusinessClick:  (String) -> Unit,
    onAddBusiness:    () -> Unit,
    onSearch:         () -> Unit,
    onLogout:         () -> Unit
) {
    val businesses       by viewModel.businesses.collectAsState()
    val myBusiness       by viewModel.myBusiness.collectAsState()
    val isLoading        by viewModel.isLoading.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var currentTab       by remember { mutableStateOf(0) }   // 0=Marketplace  1=My Business
    var showMenu         by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val isOwner = myBusiness != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kutira-Kushala", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            if (currentTab == 0) "Micro-Factory Marketplace"
                            else "My Business Dashboard",
                            fontSize = 11.sp,
                            color = OnSaffron.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    if (currentTab == 0) {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Default.Search, "Search", tint = OnSaffron)
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "Menu", tint = OnSaffron)
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Sign Out", color = CapacityRed) },
                                onClick = { showMenu = false; showLogoutDialog = true },
                                leadingIcon = { Icon(Icons.Default.Logout, null, tint = CapacityRed) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Saffron,
                    titleContentColor = OnSaffron
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected  = currentTab == 0,
                    onClick   = { currentTab = 0 },
                    icon      = { Icon(Icons.Default.Store, null) },
                    label     = { Text("Marketplace", fontSize = 11.sp) },
                    colors    = NavigationBarItemDefaults.colors(
                        selectedIconColor       = Saffron,
                        selectedTextColor       = Saffron,
                        indicatorColor          = SaffronLight.copy(alpha = 0.4f)
                    )
                )
                NavigationBarItem(
                    selected  = currentTab == 1,
                    onClick   = { currentTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (!isOwner) Badge(containerColor = CapacityRed) { Text("!", fontSize = 8.sp) }
                            }
                        ) {
                            Icon(Icons.Default.Business, null)
                        }
                    },
                    label     = { Text("My Business", fontSize = 11.sp) },
                    colors    = NavigationBarItemDefaults.colors(
                        selectedIconColor       = Saffron,
                        selectedTextColor       = Saffron,
                        indicatorColor          = SaffronLight.copy(alpha = 0.4f)
                    )
                )
            }
        }
    ) { padding ->

        when (currentTab) {
            0 -> MarketplaceTab(
                businesses       = businesses,
                isLoading        = isLoading,
                selectedCategory = selectedCategory,
                onCategorySelect = { viewModel.selectCategory(it) },
                onBusinessClick  = onBusinessClick,
                padding          = padding
            )
            1 -> MyBusinessTab(
                myBusiness      = myBusiness,
                isOwner         = isOwner,
                onOpenBusiness  = { myBusiness?.let { onBusinessClick(it.id) } },
                onRegister      = onAddBusiness,
                padding         = padding
            )
        }
    }

    // Logout dialog
    if (showLogoutDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon    = { Icon(Icons.Default.Logout, null, tint = CapacityRed) },
            title   = { Text("Sign Out?") },
            text    = { Text("You will return to the login screen.") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors  = ButtonDefaults.buttonColors(containerColor = CapacityRed)
                ) { Text("Sign Out") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Tab 0: Marketplace ──────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceTab(
    businesses: List<Business>,
    isLoading: Boolean,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit,
    onBusinessClick: (String) -> Unit,
    padding: PaddingValues
) {
    Column(Modifier.fillMaxSize().padding(padding)) {
        // Category filter
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Categories.all.forEach { cat ->
                FilterChip(
                    selected  = selectedCategory == cat,
                    onClick   = { onCategorySelect(cat) },
                    label     = { Text(cat, fontSize = 12.sp) },
                    colors    = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Saffron,
                        selectedLabelColor     = OnSaffron
                    )
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(color = Saffron)
            }
        } else if (businesses.isEmpty()) {
            BuyerEmptyState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(businesses) { biz ->
                    BusinessCard(biz, onClick = { onBusinessClick(biz.id) })
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

// ── Tab 1: My Business ──────────────────────────────────────────────────────────
@Composable
private fun MyBusinessTab(
    myBusiness: Business?,
    isOwner: Boolean,
    onOpenBusiness: () -> Unit,
    onRegister: () -> Unit,
    padding: PaddingValues
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if (!isOwner) {
            // ── Not yet registered ─────────────────────────────────────────────
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(Icons.Default.Store, null, Modifier.size(80.dp), tint = SaffronLight)
                    Text("No Business Registered", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(
                        "Register your cottage industry to start\nreceiving bulk orders from buyers.",
                        textAlign = TextAlign.Center,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Button(
                        onClick = onRegister,
                        modifier  = Modifier.fillMaxWidth().height(52.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Saffron),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(Icons.Default.AddBox, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Register My Business", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

        } else {
            val biz = myBusiness!!

            // ── Owner Dashboard ────────────────────────────────────────────────
            Text("Your Business", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Saffron)

            // Business summary card
            Card(
                modifier   = Modifier.fillMaxWidth().clickable { onOpenBusiness() },
                shape      = RoundedCornerShape(18.dp),
                elevation  = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors     = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // Cover image
                    Box(Modifier.fillMaxWidth().height(140.dp)) {
                        if (biz.profileImageUrl.isNotEmpty()) {
                            AsyncImage(model = biz.profileImageUrl, contentDescription = null,
                                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        } else {
                            Box(
                                Modifier.fillMaxSize()
                                    .background(Brush.verticalGradient(listOf(Saffron, Clay))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Store, null, Modifier.size(52.dp), tint = OnSaffron)
                            }
                        }
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (biz.isAcceptingOrders) Leaf else CapacityRed)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                if (biz.isAcceptingOrders) "✓ Accepting Orders" else "✗ Closed",
                                color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(biz.businessName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SuggestionChip(onClick = {}, label = { Text(biz.category, fontSize = 11.sp) })
                            SuggestionChip(onClick = {}, label = { Text(biz.skillArea, fontSize = 11.sp) })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), tint = Saffron)
                            Text(biz.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (biz.capacityText.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Speed, null, Modifier.size(14.dp), tint = Clay)
                                Text(biz.capacityText, fontSize = 13.sp, color = Clay, fontWeight = FontWeight.Medium)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Text(
                            "Tap to open your profile, edit capacity, manage products",
                            fontSize = 12.sp, color = Saffron, fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Quick action cards
            Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A1A))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Update Capacity
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Default.Speed,
                    title    = "Update Capacity",
                    subtitle = "Change weekly units\n& order status",
                    color    = Leaf,
                    onClick  = onOpenBusiness
                )
                // Manage Products
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon     = Icons.Default.AddBox,
                    title    = "Add Products",
                    subtitle = "Add items to your\nproduct catalog",
                    color    = Saffron,
                    onClick  = onOpenBusiness
                )
            }

            // Info tip
            Card(
                Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SaffronLight.copy(alpha = 0.4f))
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("💡", fontSize = 20.sp)
                    Column {
                        Text("How to update your business", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Clay)
                        Spacer(Modifier.height(4.dp))
                        Text("1. Tap your business card above or any Quick Action", fontSize = 12.sp, color = Clay, lineHeight = 18.sp)
                        Text("2. On your profile — tap the ✎ pencil icon to update the Capacity Meter", fontSize = 12.sp, color = Clay, lineHeight = 18.sp)
                        Text("3. Tap the ⊞ icon in the top bar to add new products", fontSize = 12.sp, color = Clay, lineHeight = 18.sp)
                        Text("4. Only you (the registered owner) see edit controls — buyers cannot modify anything", fontSize = 12.sp, color = Clay, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

// ── Quick Action Card ────────────────────────────────────────────────────────────
@Composable
private fun QuickActionCard(
    modifier: Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier  = modifier.clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(26.dp), tint = color)
            }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center, lineHeight = 15.sp)
        }
    }
}

// ── Business Card (Marketplace) ─────────────────────────────────────────────────
@Composable
fun BusinessCard(business: Business, onClick: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            Box(Modifier.fillMaxWidth().height(140.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
                if (business.profileImageUrl.isNotEmpty()) {
                    AsyncImage(model = business.profileImageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SaffronLight, Cream))),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Store, null, Modifier.size(48.dp), tint = Saffron)
                    }
                }
                Box(
                    Modifier.align(Alignment.TopEnd).padding(8.dp).clip(RoundedCornerShape(50))
                        .background(if (business.isAcceptingOrders) Leaf else CapacityRed)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        if (business.isAcceptingOrders) "✓ Accepting Orders" else "✗ Closed",
                        color = OnSaffron, fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(business.businessName, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    SuggestionChip(onClick = {}, label = { Text(business.category, fontSize = 11.sp) })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(14.dp), tint = Saffron)
                    Text(business.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Person, null, Modifier.size(14.dp), tint = Saffron)
                    Text(business.ownerName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (business.capacityText.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Speed, null, Modifier.size(14.dp), tint = Clay)
                        Text(business.capacityText, fontSize = 13.sp, color = Clay, fontWeight = FontWeight.Medium)
                    }
                }
                if (business.bulkPrice.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CurrencyRupee, null, Modifier.size(14.dp), tint = Leaf)
                        Text(business.bulkPrice, fontSize = 13.sp, color = Leaf, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// ── Empty State ─────────────────────────────────────────────────────────────────
@Composable
fun BuyerEmptyState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(32.dp)) {
            Icon(Icons.Default.Store, null, Modifier.size(72.dp), tint = SaffronLight)
            Text("No businesses listed yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Cottage industry businesses will appear here.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        }
    }
}

// Keep for backward compat
@Composable
fun EmptyState(onAddBusiness: () -> Unit) = BuyerEmptyState()