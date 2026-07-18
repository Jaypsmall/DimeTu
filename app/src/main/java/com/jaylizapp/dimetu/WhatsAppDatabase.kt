package com.jaylizapp.dimetu

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import android.util.Log

data class MessageInfo(val id: Long, val status: Int, val text: String?)
data class ChatInfo(val jid: String, val name: String)

object WhatsAppDatabase {
    private const val DB_NAME = "msgstore.db"

    fun debugLastMessages(context: Context) {

        val dbFile = File(context.filesDir, DB_NAME)
        if (!dbFile.exists()) return

        var db: SQLiteDatabase? = null

        try {

            db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )

            val cursor = db.rawQuery(
                """
            SELECT
                _id,
                status,
                message_type,
                text_data
            FROM message
            WHERE from_me = 1
            ORDER BY _id DESC
            LIMIT 20
            """.trimIndent(),
                null
            )

            while (cursor.moveToNext()) {

                Log.d(
                    "WA_DEBUG",
                    "id=${cursor.getLong(0)} status=${cursor.getInt(1)} type=${cursor.getInt(2)} text=${cursor.getString(3)}"
                )

            }

            cursor.close()

        } catch (e: Exception) {

            Log.e("WA_DEBUG", "Error", e)

        } finally {

            db?.close()

        }
    }
    

    fun getChats(context: Context): List<ChatInfo> {

        val dbFile = File(context.filesDir, DB_NAME)

        if (!dbFile.exists())
            return emptyList()

        val chats = mutableListOf<ChatInfo>()

        var db: SQLiteDatabase? = null

        try {

            db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )

            val cursor = db.rawQuery(
                """
            SELECT 
                chat._id,
                jid.raw_string,
                chat.subject
            FROM chat
            JOIN jid ON chat.jid_row_id = jid._id
            WHERE jid.type = 0
            """.trimIndent(),
                null
            )

            while (cursor.moveToNext()) {
                val jid = cursor.getString(1)
                val subject = cursor.getString(2)
                
                chats.add(
                    ChatInfo(
                        jid = jid,
                        name = subject ?: jid.substringBefore("@")
                    )
                )
            }
            cursor.close()

        } catch (e: Exception) {

            android.util.Log.e(
                "WhatsAppDatabase",
                "Error leyendo chats",
                e
            )

        } finally {

            db?.close()

        }

        return chats
    }

    fun getLatestSentMessageForChat(
        context: Context,
        jid: String
    ): MessageInfo? {

        val dbFile = File(context.filesDir, DB_NAME)

        if (!dbFile.exists()) {
            Log.e("WhatsAppDatabase", "No existe la BD: ${dbFile.absolutePath}")
            return null
        }

        var db: SQLiteDatabase? = null

        return try {

            db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )

            val query = """
            SELECT _id, status, text_data
            FROM message
            WHERE from_me = 1
            AND chat_row_id = (
                SELECT chat._id
                FROM chat
                JOIN jid ON chat.jid_row_id = jid._id
                WHERE jid.raw_string = ?
            )
            ORDER BY _id DESC
            LIMIT 1
        """.trimIndent()

            val cursor = db.rawQuery(query, arrayOf(jid))

            if (cursor.moveToFirst()) {

                val message = MessageInfo(
                    id = cursor.getLong(0),
                    status = cursor.getInt(1),
                    text = cursor.getString(2)
                )

                cursor.close()

                message

            } else {

                cursor.close()
                null

            }

        } catch (e: Exception) {

            Log.e("WhatsAppDatabase", "Error leyendo chat $jid", e)
            null

        } finally {

            db?.close()

        }
    }

    fun getLatestSentMessageGlobal(context: Context): MessageInfo? {
        val dbFile = File(context.filesDir, DB_NAME)
        if (!dbFile.exists()) return null

        var db: SQLiteDatabase? = null

        return try {

            db = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY or SQLiteDatabase.NO_LOCALIZED_COLLATORS
            )

            val cursor = db.rawQuery(
                "SELECT _id, status, text_data FROM message WHERE from_me = 1 ORDER BY _id DESC LIMIT 1",
                null
            )

            if (cursor.moveToFirst()) {

                val id = cursor.getLong(0)
                val status = cursor.getInt(1)
                val text = cursor.getString(2)

                cursor.close()

                MessageInfo(id, status, text)

            } else {

                cursor.close()
                null

            }

        } catch (e: Exception) {

            Log.e("WhatsAppDatabase", "Error leyendo la BD", e)
            null

        } finally {

            db?.close()

        }
    }
}

