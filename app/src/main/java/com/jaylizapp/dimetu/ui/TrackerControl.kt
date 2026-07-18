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
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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

// Colores personalizados Modo Luz
val ShinySilver = Color(0xFFD1D5D8)
val AshGrey = Color(0xFF9E9E9E)
val AbyssBlack = Color(0xFF0D0D0D)

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun TrackerControl() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logs by TrackerRepository.logs.collectAsState()
    val isRunning by TrackerRepository.isServiceRunning.collectAsState()
    val chats by TrackerRepository.availableChats.collectAsState()
    val selectedChatId by TrackerRepository.selectedChatId.collectAsState()
    val trackingMessage by TrackerRepository.trackingMessage.collectAsState()
    val trackingStatus by TrackerRepository.trackingStatus.collectAsState()
    val isDarkMode by TrackerRepository.isDarkMode.collectAsState()
    val isEnglish by TrackerRepository.isEnglish.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val bgColor = if (isDarkMode) Color(0xFF101418) else ShinySilver
    val cardColor = if (isDarkMode) Color(0xFF151B23) else Color.White
    val textColor = if (isDarkMode) Color.White else AbyssBlack
    val subTextColor = if (isDarkMode) Color(0xFFB8C2CC) else AshGrey
    val dividerColor = if (isDarkMode) Color(0xFF30363D) else Color(0xFFB0B4B8)

    val titleShadow = Shadow(
        color = Color.Black.copy(alpha = 0.8f),
        offset = Offset(6f, 6f),
        blurRadius = 12f
    )

    val demoniTitle = buildAnnotatedString {
        val capsStyle = SpanStyle(
            color = Color(0xFF3E6BDB),
            fontWeight = FontWeight.ExtraBold,
            shadow = titleShadow
        )
        val lowerStyle = SpanStyle(
            color = if (isDarkMode) Color.White else AbyssBlack,
            fontWeight = FontWeight.ExtraBold,
            shadow = titleShadow
        )

        withStyle(style = capsStyle) { append("D") }
        withStyle(style = lowerStyle) { append("i") }
        withStyle(style = capsStyle) { append("M") }
        withStyle(style = lowerStyle) { append("e") }
        withStyle(style = capsStyle) { append("T") }
        withStyle(style = lowerStyle) { append("u") }
        withStyle(style = lowerStyle) { append(" 😈") }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = if (isDarkMode) Color(0xFF101418) else ShinySilver,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 1. EL TÍTULO
                    Text(
                        text = demoniTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )

                    // 2. LA LÍNEA SEPARADORA SUTIL
                    HorizontalDivider(
                        modifier = Modifier.padding(bottom = 32.dp),
                        color = Color(0xFF3E6BDB).copy(alpha = 0.2f)
                    )

                    // 3. BLOQUE DE BOTONES
                    DrawerButton(
                        text = if (isEnglish) "Settings" else "Ajustes",
                        icon = Icons.Default.Settings,
                        isDarkMode = isDarkMode
                    ) {
                        scope.launch { drawerState.close() }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerButton(
                        text = if (isEnglish) "Languages" else "Idiomas",
                        icon = Icons.Default.Language,
                        isDarkMode = isDarkMode
                    ) {
                        TrackerRepository.toggleLanguage()
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerButton(
                        text = if (isEnglish) "Import" else "Importar",
                        icon = Icons.Default.Upload,
                        isDarkMode = isDarkMode
                    ) {
                        scope.launch { drawerState.close() }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    DrawerButton(
                        text = if (isEnglish) "Export" else "Exportar",
                        icon = Icons.Default.Download,
                        isDarkMode = isDarkMode
                    ) {
                        scope.launch { drawerState.close() }
                    }

                    // 4. ESPACIADOR FLEXIBLE
                    Spacer(modifier = Modifier.weight(1f))

                    // 5. SECCIÓN DE CRÉDITOS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Dimetu v1.0.1",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.Gray else Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Created by JAYLIZ with ❤️",
                            fontSize = 9.sp,
                            color = (if (isDarkMode) Color.Gray else Color.DarkGray).copy(0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu, 
                                contentDescription = "Menu",
                                tint = if (isDarkMode) Color.White else AbyssBlack
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { TrackerRepository.toggleDarkMode() },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .background(
                                    color = (if (isDarkMode) Color.White else Color.Black).copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bedtime,
                                contentDescription = "Toggle Theme",
                                tint = if (isDarkMode) Color(0xFFFFD700) else Color(0xFF3E6BDB) // Dorado en dark, azul en light
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = bgColor,
                        titleContentColor = textColor,
                        navigationIconContentColor = textColor,
                        actionIconContentColor = textColor
                    )
                )
            },
            containerColor = bgColor
        ) { paddingValues ->
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
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Header(isDarkMode, subTextColor, isEnglish)

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
                                        TrackerRepository.addLog(if (isEnglish) "Root confirmed" else "Root confirmado")
                                    } else {
                                        Toast.makeText(context, if (isEnglish) "Root unavailable" else "Root no disponible", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) Color(0xFF1B2632) else Color(0xFF4A5568)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("ROOT", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    val chatList = withContext(Dispatchers.IO) {
                                        if (!RootReader.requestRoot()) {
                                            TrackerRepository.addLog(if (isEnglish) "Could not get root" else "No se pudo obtener root")
                                            emptyList()
                                        } else {
                                            WhatsAppDatabase.getChats(context)
                                        }
                                    }

                                    TrackerRepository.setAvailableChats(chatList)
                                    if (chatList.isEmpty()) {
                                        TrackerRepository.addLog(if (isEnglish) "No chats found in msgstore.db" else "No se encontraron chats en msgstore.db")
                                    } else {
                                        TrackerRepository.addLog(if (isEnglish) "${chatList.size} chats loaded" else "${chatList.size} chats cargados")
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF235347)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isEnglish) "LOAD" else "CARGAR", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val lines = withContext(Dispatchers.IO) {
                                    if (!RootReader.requestRoot()) {
                                        listOf(if (isEnglish) "Could not get root" else "No se pudo obtener root")
                                    } else {
                                        WhatsAppDatabase.getRecentMessageDebugLines(context, 10)
                                    }
                                }

                                TrackerRepository.addLog(
                                    "${if (isEnglish) "DB Diagnosis" else "Diagnostico BD"}:\n${lines.joinToString("\n")}"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, dividerColor)
                    ) {
                        Text(if (isEnglish) "VIEW LATEST MESSAGES" else "VER ULTIMOS MENSAJES", color = textColor)
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
                                isRunning -> if (isEnglish) "STOP TRACKING" else "DETENER RASTREO"
                                selectedChatId != null -> (if (isEnglish) "TRACK CHAT #" else "RASTREAR CHAT #") + selectedChatId
                                else -> if (isEnglish) "GLOBAL TRACKING" else "RASTREO GLOBAL"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color.White
                        )
                    }

                    if (isRunning) {
                        Spacer(modifier = Modifier.height(16.dp))
                        TrackingBox(trackingMessage, trackingStatus, isDarkMode, isEnglish)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    SectionTitle(if (isEnglish) "Chats" else "Chats", subTextColor)
                }

                if (chats.isEmpty()) {
                    item {
                        EmptyState(if (isEnglish) "Press LOAD to read WhatsApp chats" else "Pulsa CARGAR para leer los chats de WhatsApp", cardColor, subTextColor)
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                } else {
                    item {
                        GlobalChatRow(
                            selected = selectedChatId == null,
                            isDarkMode = isDarkMode,
                            isEnglish = isEnglish,
                            textColor = textColor,
                            subTextColor = subTextColor,
                            dividerColor = dividerColor,
                            onClick = { TrackerRepository.setSelectedChat(null) }
                        )
                    }

                    items(chats, key = { it.chatId }) { chat ->
                        ChatRow(
                            chat = chat,
                            selected = selectedChatId == chat.chatId,
                            isDarkMode = isDarkMode,
                            textColor = textColor,
                            subTextColor = subTextColor,
                            dividerColor = dividerColor,
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
                    SectionTitle(if (isEnglish) "Activity" else "Actividad", subTextColor)
                }

                if (logs.isEmpty()) {
                    item {
                        EmptyState(if (isEnglish) "No activity yet" else "Sin actividad todavia", cardColor, subTextColor)
                    }
                } else {
                    items(logs) { log ->
                        LogRow(log, isDarkMode)
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerButton(text: String, icon: ImageVector, isDarkMode: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = if (isDarkMode) Color(0xFF151B23) else Color.White
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF3E6BDB) // Azul para los iconos
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDarkMode) Color.White else AbyssBlack
            )
        }
    }
}

@Composable
private fun DrawerHeader(isDarkMode: Boolean, isEnglish: Boolean) {
    val gradientColors = if (isDarkMode) {
        listOf(Color(0xFF8B0000), Color(0xFF4B0082))
    } else {
        listOf(Color(0xFFD1D5D8), Color(0xFF9E9E9E))
    }

    val titleShadow = Shadow(
        color = Color.Black.copy(alpha = 0.8f),
        offset = Offset(6f, 6f),
        blurRadius = 12f
    )

    val drawerTitle = buildAnnotatedString {
        val capsStyle = SpanStyle(
            color = Color(0xFF3E6BDB),
            fontWeight = FontWeight.ExtraBold,
            shadow = titleShadow
        )
        val lowerStyle = SpanStyle(
            color = if (isDarkMode) Color.White else AbyssBlack,
            fontWeight = FontWeight.ExtraBold,
            shadow = titleShadow
        )

        withStyle(style = capsStyle) { append("D") }
        withStyle(style = lowerStyle) { append("i") }
        withStyle(style = capsStyle) { append("M") }
        withStyle(style = lowerStyle) { append("e") }
        withStyle(style = capsStyle) { append("T") }
        withStyle(style = lowerStyle) { append("u") }
        withStyle(style = lowerStyle) { append(" 😈") }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(gradientColors)
            )
            .padding(24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.icono_dimetu),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = drawerTitle,
            style = MaterialTheme.typography.headlineMedium
        )
        
        Text(
            text = if (isEnglish) "Advanced WA Tracker" else "Rastreador Avanzado WA",
            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else AbyssBlack.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun Header(isDarkMode: Boolean, subTextColor: Color, isEnglish: Boolean) {
    val titleShadow = Shadow(
        color = Color.Black.copy(alpha = 0.6f),
        offset = Offset(4f, 4f),
        blurRadius = 10f
    )

    val mainTitle = buildAnnotatedString {
        val capsStyle = SpanStyle(
            color = Color(0xFF3E6BDB),
            fontWeight = FontWeight.Black,
            shadow = titleShadow,
            fontSize = 44.sp
        )
        val lowerStyle = SpanStyle(
            color = if (isDarkMode) Color.White else AbyssBlack,
            fontWeight = FontWeight.Black,
            shadow = titleShadow,
            fontSize = 44.sp
        )

        withStyle(style = capsStyle) { append("D") }
        withStyle(style = lowerStyle) { append("i") }
        withStyle(style = capsStyle) { append("M") }
        withStyle(style = lowerStyle) { append("e") }
        withStyle(style = capsStyle) { append("T") }
        withStyle(style = lowerStyle) { append("u") }
        withStyle(style = lowerStyle) { append(" 😈") }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = mainTitle,
            style = androidx.compose.ui.text.TextStyle(
                lineHeight = 50.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
            // Efecto de resplandor sutil detras del logo
            Surface(
                modifier = Modifier.size(94.dp),
                color = Color(0xFF3E6BDB).copy(alpha = if (isDarkMode) 0.12f else 0.08f),
                shape = RoundedCornerShape(22.dp)
            ) {}
            
            Image(
                painter = painterResource(id = R.drawable.icono_dimetu),
                contentDescription = "Logo DiMeTu",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        width = 2.dp, 
                        color = Color(0xFF3E6BDB).copy(alpha = 0.3f), 
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            color = if (isDarkMode) Color(0xFF1B2632) else Color.White,
            shape = RoundedCornerShape(50.dp),
            shadowElevation = 2.dp,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = if (isEnglish) "ADVANCED TRACKING" else "RASTREO AVANZADO",
                color = Color(0xFF3E6BDB),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                letterSpacing = 2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun SectionTitle(text: String, subTextColor: Color) {
    Text(
        text = text.uppercase(),
        color = subTextColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun EmptyState(text: String, cardColor: Color, subTextColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = cardColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = subTextColor,
            modifier = Modifier.padding(14.dp),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GlobalChatRow(
    selected: Boolean,
    isDarkMode: Boolean,
    isEnglish: Boolean,
    textColor: Color,
    subTextColor: Color,
    dividerColor: Color,
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
                color = if (selected) Color(0xFF3E6BDB) else dividerColor,
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (selected) (if (isDarkMode) Color(0xFF1C315C) else Color(0xFFE3EAFA)) else (if (isDarkMode) Color(0xFF151B23) else Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isEnglish) "Global tracking" else "Rastreo global",
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnglish) "Latest sent message in any chat" else "Ultimo mensaje enviado en cualquier chat",
                    color = subTextColor,
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
    isDarkMode: Boolean,
    textColor: Color,
    subTextColor: Color,
    dividerColor: Color,
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
                color = if (selected) Color(0xFF3E6BDB) else dividerColor,
                shape = RoundedCornerShape(8.dp)
            ),
        color = if (selected) (if (isDarkMode) Color(0xFF1C315C) else Color(0xFFE3EAFA)) else (if (isDarkMode) Color(0xFF151B23) else Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.name,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "chat_id=${chat.chatId}  jid=${chat.jidUser}@${chat.jidServer}",
                    color = subTextColor,
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
private fun LogRow(log: String, isDarkMode: Boolean) {
    val color = when {
        log.contains("ENTREGADO") || log.contains("DELIVERED") || log.contains("LEIDO") || log.contains("READ") -> Color(0xFF5AD18A)
        log.contains("No se pudo") || log.contains("Could not") || log.contains("Error", ignoreCase = true) -> Color(0xFFFF6B7A)
        log.contains("Cambio:") || log.contains("Change:") -> Color(0xFFFFC857)
        log.contains("NUEVO MENSAJE") || log.contains("NEW MESSAGE") -> Color(0xFF8BD5FF)
        else -> if (isDarkMode) Color(0xFFD6DEE6) else AbyssBlack
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDarkMode) Color(0xFF0D1117) else Color(0x11000000))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = log,
            color = color,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        HorizontalDivider(
            color = if (isDarkMode) Color(0xFF21262D) else Color(0xFFD1D5D8),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun TrackingBox(message: String?, status: String?, isDarkMode: Boolean, isEnglish: Boolean) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF3E6BDB), RoundedCornerShape(12.dp)),
        color = if (isDarkMode) Color(0xFF1B2632) else Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEnglish) "CURRENTLY TRACKING" else "RASTREANDO ACTUALMENTE",
                color = Color(0xFF3E6BDB),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message ?: (if (isEnglish) "Searching for message..." else "Buscando mensaje..."),
                color = if (isDarkMode) Color.White else AbyssBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                color = when {
                    status?.contains("Leido") == true || status?.contains("Read") == true -> Color(0xFF235347)
                    status?.contains("Entregado") == true || status?.contains("Delivered") == true -> Color(0xFF2E4D2E)
                    else -> if (isDarkMode) Color(0xFF30363D) else Color(0xFFE1E4E8)
                },
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = translateStatus(status, isEnglish),
                    color = if (isDarkMode || status?.contains("Entregado") == true || status?.contains("Leido") == true) Color.White else AbyssBlack,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

private fun translateStatus(status: String?, isEnglish: Boolean): String {
    if (status == null) return if (isEnglish) "Starting..." else "Iniciando..."
    if (!isEnglish) return status
    
    return when {
        status.contains("Pendiente") -> "Pending"
        status.contains("Enviado") -> "Sent"
        status.contains("Entregado") -> "Delivered"
        status.contains("Leido") -> "Read"
        else -> status
    }
}
