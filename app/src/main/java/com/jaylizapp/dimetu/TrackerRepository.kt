package com.jaylizapp.dimetu

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TrackerRepository {
    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _selectedChatJid = MutableStateFlow<String?>(null)
    val selectedChatJid: StateFlow<String?> = _selectedChatJid.asStateFlow()

    private val _availableChats = MutableStateFlow<List<ChatInfo>>(emptyList())
    val availableChats: StateFlow<List<ChatInfo>> = _availableChats.asStateFlow()

    fun addLog(message: String) {
        val currentLogs = _logs.value.toMutableList()
        // Filtrar mensajes de éxito repetitivos para no ensuciar el pergamino
        val isRepetitive = (message.contains("Confirmado") || message.contains("OK")) && 
                          currentLogs.any { it.contains("Confirmado") || it.contains("OK") }
        
        if (isRepetitive) return
        
        currentLogs.add(0, "[${System.currentTimeMillis()}] $message")
        if (currentLogs.size > 50) currentLogs.removeAt(currentLogs.size - 1)
        _logs.value = currentLogs
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    fun setSelectedChat(jid: String?) {
        _selectedChatJid.value = jid
    }

    fun setAvailableChats(chats: List<ChatInfo>) {
        _availableChats.value = chats
    }
}
