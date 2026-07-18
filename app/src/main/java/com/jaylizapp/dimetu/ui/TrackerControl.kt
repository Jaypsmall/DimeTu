package com.jaylizapp.dimetu.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jaylizapp.dimetu.ChatInfo
import com.jaylizapp.dimetu.R
import com.jaylizapp.dimetu.RootReader
import com.jaylizapp.dimetu.TrackerRepository
import com.jaylizapp.dimetu.TrackerService
import com.jaylizapp.dimetu.WhatsAppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TrackerControl() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by TrackerRepository.logs.collectAsState()
    val isRunning by TrackerRepository.isServiceRunning.collectAsState()
    val chats by TrackerRepository.availableChats.collectAsState()
    val selectedChatId by TrackerRepository.selectedChatId.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasNotificationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101418))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Header()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val success = withContext(Dispatchers.IO) {
                                RootReader.requestRoot()
                            }

                            if (success) {
                                TrackerRepository.addLog("Root confirmado")
                            } else {
                                Toast.makeText(context, "Root no disponible", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B2632)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ROOT", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        scope.launch {
                            val chatList = withContext(Dispatchers.IO) {
                                if (!RootReader.requestRoot()) {
                                    TrackerRepository.addLog("No se pudo obtener root")
                                    emptyList()
                                } else {
                                    WhatsAppDatabase.getChats(context)
                                }
                            }

                            TrackerRepository.setAvailableChats(chatList)
                            if (chatList.isEmpty()) {
                                TrackerRepository.addLog("No se encontraron chats en msgstore.db")
                            } else {
                                TrackerRepository.addLog("${chatList.size} chats cargados")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF235347)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CARGAR", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val lines = withContext(Dispatchers.IO) {
                            if (!RootReader.requestRoot()) {
                                listOf("No se pudo obtener root")
                            } else {
                                WhatsAppDatabase.getRecentMessageDebugLines(context, 10)
                            }
                        }

                        TrackerRepository.addLog(
                            "Diagnostico BD:\n${lines.joinToString("\n")}"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("VER ULTIMOS MENSAJES")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val intent = Intent(context, TrackerService::class.java)
                    if (isRunning) {
                        context.stopService(intent)
                    } else {
                        ContextCompat.startForegroundService(context, intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFF7D2735) else Color(0xFF3E6BDB)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when {
                        isRunning -> "DETENER RASTREO"
                        selectedChatId != null -> "RASTREAR CHAT #$selectedChatId"
                        else -> "RASTREO GLOBAL"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            SectionTitle("Chats")
        }

        if (chats.isEmpty()) {
            item {
                EmptyState("Pulsa CARGAR para leer los chats de WhatsApp")
                Spacer(modifier = Modifier.height(18.dp))
            }
        } else {
            item {
                GlobalChatRow(
                    selected = selectedChatId == null,
                    onClick = { TrackerRepository.setSelectedChat(null) }
                )
            }

            items(chats, key = { it.chatId }) { chat ->
                ChatRow(
                    chat = chat,
                    selected = selectedChatId == chat.chatId,
                    onClick = {
                        TrackerRepository.setSelectedChat(
                            if (selectedChatId == chat.chatId) null else chat.chatId
                        )
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionTitle("Actividad")
        }

        if (logs.isEmpty()) {
            item {
                EmptyState("Sin actividad todavia")
            }
        } else {
            items(logs) { log ->
                LogRow(log)
            }
        }
    }
}

@Composable
private fun Header() {
    Spacer(modifier = Modifier.height(12.dp))

    Image(
        painter = painterResource(id = R.drawable.icono_dimetu),
        contentDescription = "Logo Dimetu",
        modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Dimetu",
        color = Color.White,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "Rastro WhatsApp",
        color = Color(0xFFB8C2CC),
        fontSize = 13.sp,
        modifier = Modifier.padding(bottom = 18.dp)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text.uppercase(),
        color = Color(0xFFB8C2CC),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun EmptyState(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF151B23),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF8B949E),
            modifier = Modifier.padding(14.dp),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GlobalChatRow(
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF3E6BDB) else Color(0xFF30363D),
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (selected) Color(0xFF1C315C) else Color(0xFF151B23),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rastreo global",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ultimo mensaje enviado en cualquier chat",
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF5AD18A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: ChatInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF3E6BDB) else Color(0xFF30363D),
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (selected) Color(0xFF1C315C) else Color(0xFF151B23),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "chat_id=${chat.chatId}  jid=${chat.jidUser}@${chat.jidServer}",
                    color = Color(0xFF8B949E),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF5AD18A),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LogRow(log: String) {
    val color = when {
        log.contains("ENTREGADO") || log.contains("LEIDO") -> Color(0xFF5AD18A)
        log.contains("No se pudo") || log.contains("Error", ignoreCase = true) -> Color(0xFFFF6B7A)
        log.contains("Cambio:") -> Color(0xFFFFC857)
        log.contains("NUEVO MENSAJE") -> Color(0xFF8BD5FF)
        else -> Color(0xFFD6DEE6)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = log,
            color = color,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        HorizontalDivider(
            color = Color(0xFF21262D),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
