package com.kutirakushala.app.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    val isLoggedIn get() = auth.currentUser != null

    fun loginWithEmail(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) { _authState.value = AuthState.Error("Please fill in all fields."); return }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
                _currentUser.value = auth.currentUser
                _authState.value = AuthState.Success
            } catch (e: Exception) { _authState.value = AuthState.Error(friendlyError(e.message)) }
        }
    }

    fun registerWithEmail(email: String, password: String, confirmPassword: String) {
        if (email.isBlank() || password.isBlank()) { _authState.value = AuthState.Error("Please fill in all fields."); return }
        if (password != confirmPassword) { _authState.value = AuthState.Error("Passwords do not match."); return }
        if (password.length < 6) { _authState.value = AuthState.Error("Password must be at least 6 characters."); return }
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.createUserWithEmailAndPassword(email.trim(), password).await()
                _currentUser.value = auth.currentUser
                _authState.value = AuthState.Success
            } catch (e: Exception) { _authState.value = AuthState.Error(friendlyError(e.message)) }
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity) {
        if (phoneNumber.length < 10) { _authState.value = AuthState.Error("Enter a valid 10-digit mobile number."); return }
        _authState.value = AuthState.Loading
        val fullNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) { signInWithCredential(credential) }
            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) { _authState.value = AuthState.Error(friendlyError(e.message)) }
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = verificationId; resendToken = token; _authState.value = AuthState.OtpSent
            }
        }
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(auth).setPhoneNumber(fullNumber)
                .setTimeout(60L, TimeUnit.SECONDS).setActivity(activity).setCallbacks(callbacks).build()
        )
    }

    fun verifyOtp(otpCode: String) {
        val vid = storedVerificationId
        if (vid == null || otpCode.length != 6) { _authState.value = AuthState.Error("Enter the complete 6-digit OTP."); return }
        signInWithCredential(PhoneAuthProvider.getCredential(vid, otpCode))
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                auth.signInWithCredential(credential).await()
                _currentUser.value = auth.currentUser
                _authState.value = AuthState.Success
            } catch (e: Exception) { _authState.value = AuthState.Error(friendlyError(e.message)) }
        }
    }

    fun logout() { auth.signOut(); _currentUser.value = null; _authState.value = AuthState.Idle }
    fun resetState() { _authState.value = AuthState.Idle }

    private fun friendlyError(msg: String?): String = when {
        msg == null                              -> "Something went wrong. Try again."
        msg.contains("no user record")           -> "No account found with this email."
        msg.contains("password is invalid")      -> "Incorrect password. Try again."
        msg.contains("email address is already") -> "Email already registered. Please sign in."
        msg.contains("badly formatted")          -> "Please enter a valid email address."
        msg.contains("TOO_SHORT")                -> "Phone number too short."
        msg.contains("too-many-requests")        -> "Too many attempts. Please wait."
        msg.contains("network")                  -> "No internet connection."
        else                                     -> msg
    }
}