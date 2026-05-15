package com.example.kutirakushala.app.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kutirakushala.app.R
import com.example.kutirakushala.app.ui.theme.CapacityRed
import com.example.kutirakushala.app.ui.theme.Leaf
import com.example.kutirakushala.app.ui.theme.OnSaffron
import com.example.kutirakushala.app.ui.theme.Saffron
import com.example.kutirakushala.app.ui.theme.SaffronDark
import com.example.kutirakushala.app.ui.theme.SaffronLight
import com.example.kutirakushala.app.viewmodel.AuthState
import com.example.kutirakushala.app.viewmodel.AuthViewModel

private val MUTED = Color(0xFF9E8C7D)

@Composable
fun LoginScreen(
    authViewModel:   AuthViewModel,
    onLoginSuccess:  () -> Unit,
    onRegisterClick: () -> Unit
) {
    val authState    by authViewModel.authState.collectAsState()
    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current

    var mode            by remember { mutableStateOf(0) } // 0=welcome 1=email 2=phone
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegister      by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var phone           by remember { mutableStateOf("") }
    var otpDigits       by remember { mutableStateOf(List(6) { "" }) }
    var errorMsg        by remember { mutableStateOf<String?>(null) }

    val otpComplete = otpDigits.all { it.isNotEmpty() }
    LaunchedEffect(otpComplete) {
        if (otpComplete && authState is AuthState.OtpSent)
            authViewModel.verifyOtp(otpDigits.joinToString(""))
    }

    LaunchedEffect(authState) {
        when (val s = authState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error   -> { errorMsg = s.message; authViewModel.resetState() }
            else -> {}
        }
    }

    Box(Modifier.fillMaxSize()) {
        // ── Full Screen Background Image ──────────────────────────────────────────
        androidx.compose.foundation.Image(
            painter            = painterResource(id = R.drawable.login_banner),
            contentDescription = "Background",
            contentScale       = ContentScale.Crop,
            modifier           = Modifier.fillMaxSize()
        )
        
        // Gradient overlay for readability
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Black.copy(alpha = 0.5f),
                            0.4f to Color.Black.copy(alpha = 0.2f),
                            1.0f to Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            // ── Brand Text ───────────────────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Kutira-Kushala",
                    fontSize      = 36.sp,
                    fontWeight    = FontWeight.ExtraBold,
                    color         = Color.White,
                    letterSpacing = 0.5.sp
                )
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Leaf))
                    Text(
                        "Micro-Factory Showcase",
                        fontSize   = 14.sp,
                        color      = Color.White.copy(alpha = 0.95f),
                        fontWeight = FontWeight.Medium
                    )
                    Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Leaf))
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("🧺 Craft", "🪔 Agarbatti", "🥘 Food", "🧵 Textile").forEach { tag ->
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Saffron.copy(alpha = 0.9f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(tag, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── Auth Card ─────────────────────────────────────────────────────────
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape     = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f))
            ) {
                Column(Modifier.padding(24.dp)) {
                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = {
                            if (targetState > initialState)
                                slideInHorizontally { it }  + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                            else
                                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it }  + fadeOut()
                        },
                        label = "mode"
                    ) { m ->
                        when (m) {
                            0 -> WelcomePanel(
                                onEmail    = { mode = 1; isRegister = false; errorMsg = null },
                                onPhone    = { mode = 2; errorMsg = null },
                                onRegister = onRegisterClick,
                                isLoading  = authState is AuthState.Loading
                            )
                            1 -> EmailPanel(
                                email = email,                onEmailChange    = { email = it },
                                password = password,          onPasswordChange = { password = it },
                                confirmPassword = confirmPassword, onConfirmChange  = { confirmPassword = it },
                                passwordVisible = passwordVisible, onTogglePassword = { passwordVisible = !passwordVisible },
                                isRegister   = isRegister,
                                onToggleMode = { isRegister = !isRegister; errorMsg = null },
                                isLoading    = authState is AuthState.Loading,
                                onSubmit = {
                                    focusManager.clearFocus(); errorMsg = null
                                    if (isRegister) authViewModel.registerWithEmail(email, password, confirmPassword)
                                    else authViewModel.loginWithEmail(email, password)
                                },
                                onBack = { mode = 0; errorMsg = null; authViewModel.resetState() }
                            )
                            2 -> PhonePanel(
                                phone = phone, onPhoneChange = { if (it.length <= 10) phone = it.filter(Char::isDigit) },
                                otpDigits = otpDigits, onOtpChange = { otpDigits = it },
                                otpSent  = authState is AuthState.OtpSent,
                                isLoading = authState is AuthState.Loading,
                                onSendOtp = { focusManager.clearFocus(); errorMsg = null; authViewModel.sendOtp(phone, context as Activity) },
                                onVerify  = { errorMsg = null; authViewModel.verifyOtp(otpDigits.joinToString("")) },
                                onResend  = { otpDigits = List(6) { "" }; authViewModel.resetState() },
                                onBack    = { mode = 0; errorMsg = null; authViewModel.resetState() }
                            )
                        }
                    }

                    AnimatedVisibility(visible = errorMsg != null,
                        enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                                    .background(CapacityRed.copy(alpha = 0.08f)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, null, Modifier.size(18.dp), tint = CapacityRed)
                                Text(errorMsg ?: "", fontSize = 12.5.sp, color = CapacityRed,
                                    modifier = Modifier.weight(1f), lineHeight = 17.sp)
                                IconButton(onClick = { errorMsg = null }, Modifier.size(22.dp)) {
                                    Icon(Icons.Default.Close, null, Modifier.size(14.dp), tint = CapacityRed)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
            Text(
                "Connecting cottage industries directly with bulk buyers",
                fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f), textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp), lineHeight = 18.sp
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun WelcomePanel(
    onEmail: () -> Unit, onPhone: () -> Unit,
    onRegister: () -> Unit, isLoading: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Sign In to Continue", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
        Text("Browse businesses or manage your cottage industry", fontSize = 12.sp, color = MUTED, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onEmail, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp), enabled = !isLoading
        ) {
            Icon(Icons.Default.Email, null, Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Continue with Email", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onPhone, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, Saffron),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Saffron), enabled = !isLoading
        ) {
            Icon(Icons.Default.Phone, null, Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("Continue with Phone / OTP", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = Color(0xFFEEEEEE))
        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Are you an entrepreneur?", fontSize = 13.sp, color = MUTED)
            TextButton(onClick = onRegister, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text("Register your business →", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Saffron)
            }
        }
    }
}

@Composable
private fun EmailPanel(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmChange: (String) -> Unit,
    passwordVisible: Boolean, onTogglePassword: () -> Unit,
    isRegister: Boolean, onToggleMode: () -> Unit,
    isLoading: Boolean, onSubmit: () -> Unit, onBack: () -> Unit
) {
    val focusManager  = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus  = remember { FocusRequester() }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Saffron)
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(if (isRegister) "Create Account" else "Sign In",
                    fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A1A))
                Text(if (isRegister) "Register with email" else "Welcome back!",
                    fontSize = 12.sp, color = MUTED)
            }
        }
        Spacer(Modifier.height(20.dp))

        LoginField(value = email, onValueChange = onEmailChange, label = "Email Address",
            icon = Icons.Default.AlternateEmail, keyboardType = KeyboardType.Email, imeAction = ImeAction.Next,
            onNext = { passwordFocus.requestFocus() })
        Spacer(Modifier.height(12.dp))
        LoginField(value = password, onValueChange = onPasswordChange, label = "Password",
            icon = Icons.Default.Lock, keyboardType = KeyboardType.Password,
            imeAction = if (isRegister) ImeAction.Next else ImeAction.Done,
            isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = onTogglePassword,
            modifier = Modifier.focusRequester(passwordFocus),
            onNext = { if (isRegister) confirmFocus.requestFocus() else onSubmit() }, onDone = { onSubmit() })

        AnimatedVisibility(isRegister, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column {
                Spacer(Modifier.height(12.dp))
                LoginField(value = confirmPassword, onValueChange = onConfirmChange, label = "Confirm Password",
                    icon = Icons.Default.LockReset, keyboardType = KeyboardType.Password, imeAction = ImeAction.Done,
                    isPassword = true, passwordVisible = passwordVisible, onPasswordToggle = onTogglePassword,
                    modifier = Modifier.focusRequester(confirmFocus),
                    onDone = { focusManager.clearFocus(); onSubmit() })
            }
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onSubmit, modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp), enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = OnSaffron, strokeWidth = 2.dp)
            else {
                Icon(if (isRegister) Icons.Default.PersonAdd else Icons.Default.Login, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isRegister) "Create Account" else "Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(if (isRegister) "Already have an account?" else "Don't have an account?", fontSize = 13.sp, color = MUTED)
            TextButton(onClick = onToggleMode, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Text(if (isRegister) "Sign In" else "Register", fontWeight = FontWeight.Bold, color = Saffron, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun PhonePanel(
    phone: String, onPhoneChange: (String) -> Unit,
    otpDigits: List<String>, onOtpChange: (List<String>) -> Unit,
    otpSent: Boolean, isLoading: Boolean,
    onSendOtp: () -> Unit, onVerify: () -> Unit,
    onResend: () -> Unit, onBack: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Saffron)
            }
            Spacer(Modifier.width(4.dp))
            Column {
                Text(if (otpSent) "Verify OTP" else "Phone Sign In",
                    fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF1A1A1A))
                Text(if (otpSent) "OTP sent to +91 $phone" else "We'll send an OTP to your mobile",
                    fontSize = 12.sp, color = MUTED)
            }
        }
        Spacer(Modifier.height(20.dp))

        AnimatedVisibility(!otpSent) {
            Column {
                OutlinedTextField(
                    value = phone, onValueChange = onPhoneChange,
                    label = { Text("Mobile Number") },
                    leadingIcon = {
                        Row(Modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("+91", fontWeight = FontWeight.Bold, color = Saffron, fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Box(Modifier.width(1.dp).height(20.dp).background(Color(0xFFE0E0E0)))
                            Spacer(Modifier.width(4.dp))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSendOtp() }),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Saffron,
                        focusedLabelColor = Saffron, cursorColor = Saffron)
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        AnimatedVisibility(otpSent, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
            Column {
                Spacer(Modifier.height(4.dp))
                AutoAdvanceOtpInput(digits = otpDigits, onDigitsChange = onOtpChange)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = onResend) {
                        Icon(Icons.Default.Refresh, null, Modifier.size(14.dp), tint = Saffron)
                        Spacer(Modifier.width(4.dp))
                        Text("Change number / Resend OTP", color = Saffron, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Button(
            onClick = if (otpSent) onVerify else onSendOtp,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            enabled = !isLoading && (if (!otpSent) phone.length == 10 else otpDigits.all { it.isNotEmpty() }),
            colors = ButtonDefaults.buttonColors(containerColor = Saffron),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = OnSaffron, strokeWidth = 2.dp)
            else {
                Icon(if (otpSent) Icons.Default.VerifiedUser else Icons.Default.Send, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (otpSent) "Verify & Sign In" else "Send OTP", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun AutoAdvanceOtpInput(digits: List<String>, onDigitsChange: (List<String>) -> Unit) {
    val focusRequesters = remember { List(6) { FocusRequester() } }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        digits.forEachIndexed { index, digit ->
            OutlinedTextField(
                value = digit,
                onValueChange = { input ->
                    val f = input.filter(Char::isDigit)
                    if (f.isEmpty()) {
                        onDigitsChange(digits.toMutableList().also { it[index] = "" })
                        if (index > 0) focusRequesters[index - 1].requestFocus()
                    } else {
                        onDigitsChange(digits.toMutableList().also { it[index] = f.last().toString() })
                        if (index < 5) focusRequesters[index + 1].requestFocus()
                    }
                },
                modifier = Modifier.weight(1f).aspectRatio(0.85f)
                    .focusRequester(focusRequesters[index])
                    .onKeyEvent { e ->
                        if (e.type == KeyEventType.KeyDown && e.key == Key.Backspace && digit.isEmpty() && index > 0) {
                            onDigitsChange(digits.toMutableList().also { it[index - 1] = "" })
                            focusRequesters[index - 1].requestFocus(); true
                        } else false
                    },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Saffron),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Saffron,
                    unfocusedBorderColor = if (digit.isNotEmpty()) SaffronDark else Color(0xFFE0E0E0),
                    focusedContainerColor = SaffronLight.copy(alpha = 0.25f),
                    unfocusedContainerColor = if (digit.isNotEmpty()) SaffronLight.copy(alpha = 0.2f) else Color.White,
                    cursorColor = Saffron)
            )
        }
    }
    LaunchedEffect(Unit) { try { focusRequesters[0].requestFocus() } catch (_: Exception) {} }
}

@Composable
fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onNext: (() -> Unit)? = null,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, null, tint = Saffron, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onPasswordToggle?.invoke() }) {
                    Icon(
                        if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        null,
                        tint = MUTED,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onNext?.invoke() }, onDone = { onDone?.invoke() }),
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Saffron,
            focusedLabelColor = Saffron,
            focusedLeadingIconColor = Saffron,
            cursorColor = Saffron
        )
    )
}
