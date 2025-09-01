package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.*
import com.example.underbigtreeapplication.repository.UserRepository
import kotlinx.coroutines.launch

class SignupViewModel(private val repository: UserRepository = UserRepository()) : ViewModel() {

    private val _signupStatus = MutableLiveData<Result<Boolean>>()
    val signupStatus: LiveData<Result<Boolean>> = _signupStatus

    fun signup(email: String, password: String, name: String, phone: String) {
        viewModelScope.launch {
            try {
                val result = repository.registerUser(email, password, name, phone)
                _signupStatus.value = Result.success(result)
            } catch (e: Exception) {
                _signupStatus.value = Result.failure(e)
            }
        }
    }
}