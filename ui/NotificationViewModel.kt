package com.example.basicpushnotification.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.basicpushnotification.data.AppDatabase
import com.example.basicpushnotification.data.NotificationItem
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).notificationDao()

    val notificationHistory: StateFlow<List<NotificationItem>> = dao.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _fcmToken = MutableStateFlow("Fetching token...")
    val fcmToken: StateFlow<String> = _fcmToken.asStateFlow()

    init {
        retrieveFcmToken()
    }

    fun retrieveFcmToken() {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _fcmToken.value = task.result ?: "Token is null"
                    Log.d("FCM_TOKEN", "Current Token: ${task.result}")
                } else {
                    _fcmToken.value = "Failed to fetch token: ${task.exception?.localizedMessage}"
                }
            }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearAllNotifications()
        }
    }
}
