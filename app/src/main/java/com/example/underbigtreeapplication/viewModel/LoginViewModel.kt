package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.*
import com.example.underbigtreeapplication.model.User
import com.example.underbigtreeapplication.repository.UserRepository
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _loginResult = MutableLiveData<Result<User>>()
    val loginResult: LiveData<Result<User>> = _loginResult

    fun login(email: String, password: String) {
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