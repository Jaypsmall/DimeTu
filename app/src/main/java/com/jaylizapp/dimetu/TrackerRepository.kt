package com.jaylizapp.dimetu

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TrackerRepository {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isEnglish = MutableStateFlow(false)
    val isEnglish: StateFlow<Boolean> = _isEnglish.asStateFlow()

    private val _selectedChatId = MutableStateFlow<Long?>(null)
    val selectedChatId: StateFlow<Long?> = _selectedChatId.asStateFlow()

    private val _availableChats = MutableStateFlow<List<ChatInfo>>(emptyList())
    val availableChats: StateFlow<List<ChatInfo>> = _availableChats.asStateFlow()

    private val _trackingMessage = MutableStateFlow<String?>(null)
    val trackingMessage: StateFlow<String?> = _trackingMessage.asStateFlow()

    private val _trackingStatus = MutableStateFlow<String?>(null)
    val trackingStatus: StateFlow<String?> = _trackingStatus.asStateFlow()

    fun addLog(message: String) {
        val currentLogs = _logs.value.toMutableList()
        if (currentLogs.firstOrNull()?.endsWith(message) == true) return

        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        currentLogs.add(0, "[$time] $message")

        if (currentLogs.size > 50) {
            currentLogs.removeAt(currentLogs.lastIndex)
        }

        _logs.value = currentLogs
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun setSelectedChat(chatId: Long?) {
        _selectedChatId.value = chatId
    }

    fun setAvailableChats(chats: List<ChatInfo>) {
        _availableChats.value = chats
    }

    fun updateTrackingInfo(message: String?, status: String?) {
        _trackingMessage.value = message
        _trackingStatus.value = status
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun toggleLanguage() {
        _isEnglish.value = !_isEnglish.value
    }
}
