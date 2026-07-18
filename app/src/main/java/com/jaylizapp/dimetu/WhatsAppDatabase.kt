package com.jaylizapp.dimetu

import android.content.Context
import android.util.Log

data class MessageInfo(
    val id: Long,
    val chatId: Long,
    val status: Int,
    val text: String?
)

data class ChatInfo(
    val chatId: Long,
    val jidUser: String,
    val jidServer: String,
    val rawJid: String?,
    val name: String,
    val lastTimestamp: Long
)

object WhatsAppDatabase {

    fun debugLastMessages(context: Context) {
        getRecentMessageDebugLines(context, 20).forEach {
            Log.d("WA_DEBUG", it)
        }
    }

    fun getRecentMessageDebugLines(
        context: Context,
        limit: Int = 20
    ): List<String> {
        val safeLimit = limit.coerceIn(1, 100)
        val output = RootSqlite.query(
            """
            SELECT
                message._id,
                hex(COALESCE(jid.user, '')),
                hex(COALESCE(jid.server, '')),
                message.from_me,
                message.status,
                hex(COALESCE(message.text_data, ''))
            FROM message
            JOIN chat ON message.chat_row_id = chat._id
            JOIN jid ON chat.jid_row_id = jid._id
            WHERE message.text_data IS NOT NULL
            ORDER BY message.timestamp DESC, message._id DESC
            LIMIT $safeLimit;
            """.trimIndent()
        ) ?: return listOf("No se pudo leer msgstore.db con root/sqlite3")

        return output.rows().mapNotNull { row ->
            if (row.size < 6) return@mapNotNull null

            "id=${row[0]} jid=${row[1].fromHex()}@${row[2].fromHex()} " +
                "from_me=${row[3]} status=${row[4]} text=${row[5].fromHex()}"
        }.ifEmpty {
            listOf("La consulta no devolvio mensajes con texto")
        }
    }

    fun getChats(context: Context): List<ChatInfo> {
        val hasJidMap = tableExists("jid_map")
        val output = queryChats(hasJidMap) ?: queryChats(false)

        if (output.isNullOrBlank()) return emptyList()

        return output.rows().mapNotNull { row ->
            if (row.size < 6) return@mapNotNull null

            val chatId = row[0].toLongOrNull() ?: return@mapNotNull null
            val jidUser = row[1].fromHex()
            val jidServer = row[2].fromHex()
            val rawJid = row[3].fromHex().ifBlank { null }
            val subject = row[4].fromHex().ifBlank { null }
            val lastTimestamp = row[5].toLongOrNull() ?: 0L

            ChatInfo(
                chatId = chatId,
                jidUser = jidUser,
                jidServer = jidServer,
                rawJid = rawJid,
                name = displayName(subject, jidUser, jidServer, rawJid, chatId),
                lastTimestamp = lastTimestamp
            )
        }
    }

    fun getLatestSentMessageForChat(
        context: Context,
        chatId: Long
    ): MessageInfo? {
        val output = RootSqlite.query(
            """
            SELECT
                _id,
                chat_row_id,
                status,
                hex(COALESCE(text_data, ''))
            FROM message
            WHERE from_me = 1
            AND chat_row_id = $chatId
            ORDER BY timestamp DESC, _id DESC
            LIMIT 1;
            """.trimIndent()
        ) ?: return null

        return output.firstMessageOrNull()
    }

    fun getLatestSentMessageGlobal(context: Context): MessageInfo? {
        val output = RootSqlite.query(
            """
            SELECT
                message._id,
                message.chat_row_id,
                message.status,
                hex(COALESCE(message.text_data, ''))
            FROM message
            JOIN chat ON message.chat_row_id = chat._id
            JOIN jid ON chat.jid_row_id = jid._id
            WHERE message.from_me = 1
            ORDER BY message.timestamp DESC, message._id DESC
            LIMIT 1;
            """.trimIndent()
        ) ?: return null

        return output.firstMessageOrNull()
    }

    private fun queryChats(useJidMap: Boolean): String? {
        val sql = if (useJidMap) {
            """
            SELECT
                chat._id,
                hex(COALESCE(realjid.user, jid.user, '')),
                hex(COALESCE(realjid.server, jid.server, '')),
                hex(COALESCE(realjid.raw_string, jid.raw_string, '')),
                hex(COALESCE(chat.subject, '')),
                COALESCE(chat.sort_timestamp, 0)
            FROM chat
            JOIN jid ON chat.jid_row_id = jid._id
            LEFT JOIN jid_map ON jid_map.lid_row_id = jid._id
            LEFT JOIN jid realjid ON realjid._id = jid_map.jid_row_id
            WHERE EXISTS (
                SELECT 1 FROM message WHERE message.chat_row_id = chat._id
            )
            ORDER BY chat.sort_timestamp DESC;
            """.trimIndent()
        } else {
            """
            SELECT
                chat._id,
                hex(COALESCE(jid.user, '')),
                hex(COALESCE(jid.server, '')),
                hex(COALESCE(jid.raw_string, '')),
                hex(COALESCE(chat.subject, '')),
                COALESCE(chat.sort_timestamp, 0)
            FROM chat
            JOIN jid ON chat.jid_row_id = jid._id
            WHERE EXISTS (
                SELECT 1 FROM message WHERE message.chat_row_id = chat._id
            )
            ORDER BY chat.sort_timestamp DESC;
            """.trimIndent()
        }

        return RootSqlite.query(sql)
    }

    private fun tableExists(tableName: String): Boolean {
        return RootSqlite.query(
            "SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = '$tableName' LIMIT 1;"
        )?.trim() == "1"
    }

    private fun String.firstMessageOrNull(): MessageInfo? {
        val row = rows().firstOrNull() ?: return null
        if (row.size < 4) return null

        return MessageInfo(
            id = row[0].toLongOrNull() ?: return null,
            chatId = row[1].toLongOrNull() ?: return null,
            status = row[2].toIntOrNull() ?: -1,
            text = row[3].fromHex().ifBlank { null }
        )
    }

    private fun String.rows(): List<List<String>> {
        return lineSequence()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .map { it.split("|") }
            .toList()
    }

    private fun displayName(
        subject: String?,
        jidUser: String,
        jidServer: String,
        rawJid: String?,
        chatId: Long
    ): String {
        subject.cleanOrNull()?.let { return it }
        rawJid.cleanOrNull()?.let { return it }

        return when {
            jidUser.isNotBlank() && jidServer.isNotBlank() -> "$jidUser@$jidServer"
            jidUser.isNotBlank() -> jidUser
            else -> "Chat $chatId"
        }
    }

    private fun String.fromHex(): String {
        if (isBlank()) return ""

        return try {
            val bytes = chunked(2)
                .map { it.toInt(16).toByte() }
                .toByteArray()

            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            this
        }
    }

    private fun String?.cleanOrNull(): String? {
        val value = this?.trim()
        return value?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
    }
}
