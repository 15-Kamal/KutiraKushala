package com.example.kutirakushala.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.kutirakushala.app.data.model.Product
import com.example.kutirakushala.app.ui.theme.CapacityRed
import com.example.kutirakushala.app.ui.theme.Clay
import com.example.kutirakushala.app.ui.theme.Cream
import com.example.kutirakushala.app.ui.theme.Leaf
import com.example.kutirakushala.app.ui.theme.OnSaffron
import com.example.kutirakushala.app.ui.theme.Saffron
import com.example.kutirakushala.app.ui.theme.SaffronDark
import com.example.kutirakushala.app.ui.theme.SaffronLight
import com.example.kutirakushala.app.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    businessId:   String,
    viewModel:    AppViewModel,
    onBack:       () -> Unit,
    onAddProduct: () -> Unit
) {
    val business    by viewModel.selectedBusiness.collectAsState()
    val products    by viewModel.products.collectAsState()
    val isLoading   by viewModel.isLoading.collectAsState()
    val context     = LocalContext.current

    // Owner check — only the user who registered this business sees edit controls
    val isOwner = business?.let { viewModel.isOwnerOf(it) } ?: false

    var showCapacityDialog by remember { mutableStateOf(false) }
    var capacityInput      by remember { mutableStateOf("") }
    var acceptingOrders    by remember { mutableStateOf(true) }

    LaunchedEffect(businessId) { viewModel.loadBusiness(businessId) }
    LaunchedEffect(business) {
        business?.let {
            capacityInput   = it.capacityAvailable.toString()
            acceptingOrders = it.isAcceptingOrders
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(business?.businessName ?: "Business Profile", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = OnSaffron)
                    }
                },
                actions = {
                    // Owner-only actions in toolbar
                    if (isOwner) {
                        IconButton(onClick = { showCapacityDialog = true }) {
                            Icon(Icons.Default.Edit, "Edit Capacity", tint = OnSaffron)
                        }
                        IconButton(onClick = onAddProduct) {
                            Icon(Icons.Default.AddBox, "Add Product", tint = OnSaffron)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor   = Saffron,
                    titleContentColor = OnSaffron
                )
            )
        }
    ) { padding ->

        if (isLoading || business == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Saffron)
            }
            return@Scaffold
        }

        val biz = business!!

        LazyColumn(
            Modifier.fillMaxSize().padding(padding)
        ) {

            // ── Hero image ─────────────────────────────────────────────────────
            item {
                Box(Modifier.fillMaxWidth().height(220.dp)) {
                    if (biz.profileImageUrl.isNotEmpty()) {
                        AsyncImage(model = biz.profileImageUrl, contentDescription = null,
                            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Saffron, SaffronDark))),
                            contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Store, null, Modifier.size(64.dp), tint = OnSaffron)
                        }
                    }
                    // Owner badge overlay
                    if (isOwner) {
                        Box(
                            Modifier.align(Alignment.TopStart).padding(10.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF1A237E).copy(alpha = 0.85f))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text("✎ Your Business", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Business info ──────────────────────────────────────────────────
            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(biz.businessName, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(onClick = {}, label = { Text(biz.category) })
                        SuggestionChip(onClick = {}, label = { Text(biz.skillArea) })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.Person, null, Modifier.size(15.dp), tint = Saffron)
                        Text(biz.ownerName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.LocationOn, null, Modifier.size(15.dp), tint = Saffron)
                        Text(biz.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (biz.description.isNotEmpty()) {
                        Text(biz.description, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp, fontSize = 13.sp)
                    }
                }
            }

            // ── Capacity Meter ─────────────────────────────────────────────────
            item {
                CapacityMeterCard(
                    isAccepting  = biz.isAcceptingOrders,
                    capacityText = biz.capacityText,
                    available    = biz.capacityAvailable,
                    isOwner      = isOwner,
                    onEditClick  = { showCapacityDialog = true }
                )
            }

            // ── Action buttons for ALL users ───────────────────────────────────
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

                    if (biz.phone.isNotEmpty()) {
                        // Direct Connect Call
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${biz.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Leaf),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) {
                            Icon(Icons.Default.Phone, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Direct Connect — Call for Orders", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(biz.phone, fontSize = 11.sp, color = Color.White.copy(alpha = 0.85f))
                            }
                        }

                        // SMS for queries
                        OutlinedButton(
                            onClick = {
                                val smsBody = "Hi ${biz.ownerName}, I found your business \"${biz.businessName}\" on Kutira-Kushala. " +
                                        "I'm interested in placing a bulk order. Could you please share your availability and pricing details?"
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${biz.phone}")
                                    putExtra("sms_body", smsBody)
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape    = RoundedCornerShape(14.dp),
                            border   = androidx.compose.foundation.BorderStroke(1.5.dp, Saffron)
                        ) {
                            Icon(Icons.Default.Message, null, Modifier.size(18.dp), tint = Saffron)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Message for Queries — SMS", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Saffron)
                                Text("Send a pre-filled enquiry message", fontSize = 11.sp, color = Clay)
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }

            // ── Product catalog section header ─────────────────────────────────
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text("Product Catalog", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    // Only owner sees Add button here
                    if (isOwner) {
                        TextButton(onClick = onAddProduct) {
                            Icon(Icons.Default.AddBox, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add Product", color = Saffron)
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = SaffronLight)
            }

            // ── Products list ──────────────────────────────────────────────────
            if (products.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No products listed yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            } else {
                items(products) { product ->
                    ProductItem(
                        product  = product,
                        canEdit  = isOwner,           // only owner can delete
                        onDelete = { viewModel.deleteProduct(product.id) }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        // ── Capacity edit dialog (owner only) ─────────────────────────────────
        if (showCapacityDialog && isOwner) {
            AlertDialog(
                onDismissRequest = { showCapacityDialog = false },
                title = { Text("Update Capacity Meter") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        androidx.compose.material3.OutlinedTextField(
                            value         = capacityInput,
                            onValueChange = { capacityInput = it.filter(Char::isDigit) },
                            label         = { Text("Available Units This Week") },
                            leadingIcon   = { Icon(Icons.Default.Inventory, null) },
                            singleLine    = true
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = acceptingOrders,
                                onCheckedChange = { acceptingOrders = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Leaf, checkedTrackColor = Leaf.copy(alpha = 0.4f))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(if (acceptingOrders) "✓ Accepting Orders" else "✗ Not Accepting")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateCapacity(biz.id, capacityInput.toIntOrNull() ?: 0, acceptingOrders)
                            showCapacityDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Saffron)
                    ) { Text("Update") }
                },
                dismissButton = {
                    TextButton(onClick = { showCapacityDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

// ── Capacity Meter Card ─────────────────────────────────────────────────────────
@Composable
fun CapacityMeterCard(
    isAccepting: Boolean,  capacityText: String,
    available: Int,        isOwner: Boolean,
    onEditClick: () -> Unit
) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAccepting) Leaf.copy(alpha = 0.08f) else CapacityRed.copy(alpha = 0.08f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Speed, null, Modifier.size(40.dp),
                tint = if (isAccepting) Leaf else CapacityRed)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Capacity Meter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (capacityText.isNotEmpty())
                    Text(capacityText, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (isAccepting) "✓ Ready for $available units this week"
                    else "✗ Not accepting orders currently",
                    fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = if (isAccepting) Leaf else CapacityRed
                )
            }
            // Edit button only for owner
            if (isOwner) {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, "Edit", tint = Saffron)
                }
            }
        }
    }
}

// ── Product List Item ───────────────────────────────────────────────────────────
@Composable
fun ProductItem(product: Product, canEdit: Boolean, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Product image
            Box(
                Modifier.size(72.dp).clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(model = product.imageUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Icon(Icons.Default.Inventory2, null, tint = Saffron)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (product.description.isNotEmpty())
                    Text(product.description, fontSize = 12.sp, maxLines = 2,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (product.wholesalePrice.isNotEmpty())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CurrencyRupee, null, Modifier.size(12.dp), tint = Leaf)
                            Text(product.wholesalePrice, color = Leaf, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                    Text("Min: ${product.minOrderQty} units", fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Delete button only for owner
            if (canEdit) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete", tint = CapacityRed)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Product?") },
            text  = { Text("\"${product.name}\" will be removed from your catalog.") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteDialog = false },
                    colors  = ButtonDefaults.buttonColors(containerColor = CapacityRed)
                ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}