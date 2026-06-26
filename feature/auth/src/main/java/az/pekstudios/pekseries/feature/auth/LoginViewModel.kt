package az.pekstudios.pekseries.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import az.pekstudios.pekseries.core.network.repository.SeriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: SeriesRepository
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

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
                viewModelScope.launch { repository.syncSubscriptionsWithFcm() }
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