package com.example.pekseries.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    // Состояние: залогинен ли юзер прямо сейчас?
    private val _isUserLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _isUserLoggedIn.value = true
                _error.value = null
            }
            .addOnFailureListener { e ->
                _error.value = "Google Auth Error: ${e.localizedMessage}"
            }
    }
    fun login(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _isUserLoggedIn.value = true
                _error.value = null
            }
            .addOnFailureListener { e ->
                _error.value = "Ошибка входа: ${e.localizedMessage}"
            }
    }

    fun register(email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _isUserLoggedIn.value = true
                _error.value = null
            }
            .addOnFailureListener { e ->
                _error.value = "Ошибка регистрации: ${e.localizedMessage}"
            }
    }

    fun logout() {
        auth.signOut()
        _isUserLoggedIn.value = false
    }
}