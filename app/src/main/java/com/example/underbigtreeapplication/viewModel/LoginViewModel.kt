package com.example.underbigtreeapplication.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.example.underbigtreeapplication.model.User
import com.example.underbigtreeapplication.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    var email by mutableStateOf("")
    var password by mutableStateOf("")

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun login() {
        viewModelScope.launch {
            try {
                val user = repository.loginUser(email, password)
                _loginResult.value = Result.success(user)
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }
}