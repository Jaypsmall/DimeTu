package com.jaylizapp.dimetu

import android.content.Context
import java.io.DataOutputStream
import java.io.File

object RootReader {

    private var daemonProcess: Process? = null

    private const val DB_NAME = "msgstore.db"
    private const val WAL_NAME = "msgstore.db-wal"
    private const val SHM_NAME = "msgstore.db-shm"

    private val whatsappPaths = listOf(
        "/data/data/com.whatsapp/databases",
        "/data/user/0/com.whatsapp/databases",
        "/data/data/com.whatsapp.w4b/databases",
        "/data/user/0/com.whatsapp.w4b/databases"
    )

    fun requestRoot(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            DataOutputStream(process.outputStream).use { os ->
                os.writeBytes("id\n")
                os.writeBytes("exit\n")
                os.flush()
            }

            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    fun copyWhatsAppDb(context: Context): Boolean {
        val command = buildCopyCommand(context, exitWhenCopied = true, loop = false)
        return runRootCommand(command) && isDbReady(context)
    }

    fun startRootDaemon(context: Context) {
        if (daemonProcess != null) return

        val script = buildCopyCommand(context, exitWhenCopied = false, loop = true)

        Thread {
            try {
                daemonProcess = Runtime.getRuntime().exec(arrayOf("su"))

                val os = DataOutputStream(daemonProcess!!.outputStream)
                os.writeBytes(script)
                os.writeBytes("\n")
                os.flush()

                daemonProcess!!.waitFor()
            } catch (e: Exception) {
                daemonProcess = null
            }
        }.start()
    }

    fun stopRootDaemon() {
        daemonProcess?.destroy()
        daemonProcess = null
    }

    fun isDbReady(context: Context): Boolean {
        val db = File(context.filesDir, DB_NAME)
        return db.exists() && db.length() > 0
    }

    fun getLocalDatabase(context: Context): File {
        return File(context.filesDir, DB_NAME)
    }

    private fun buildCopyCommand(
        context: Context,
        exitWhenCopied: Boolean,
        loop: Boolean
    ): String {
        val dir = context.filesDir.absolutePath
        val uid = android.os.Process.myUid()

        val body = buildString {
            appendLine("copied=0")

            whatsappPaths.forEach { base ->
                appendLine("""if [ "${'$'}copied" = "0" ] && [ -f "$base/$DB_NAME" ]; then""")
                appendLine("""  cp "$base/$DB_NAME" "$dir/$DB_NAME.tmp";""")
                appendLine("""  if [ -f "$base/$WAL_NAME" ]; then cp "$base/$WAL_NAME" "$dir/$WAL_NAME.tmp"; else rm -f "$dir/$WAL_NAME.tmp" "$dir/$WAL_NAME"; fi;""")
                appendLine("""  if [ -f "$base/$SHM_NAME" ]; then cp "$base/$SHM_NAME" "$dir/$SHM_NAME.tmp"; else rm -f "$dir/$SHM_NAME.tmp" "$dir/$SHM_NAME"; fi;""")
                appendLine("""  chown $uid:$uid "$dir/$DB_NAME.tmp" 2>/dev/null;""")
                appendLine("""  chown $uid:$uid "$dir/$WAL_NAME.tmp" 2>/dev/null;""")
                appendLine("""  chown $uid:$uid "$dir/$SHM_NAME.tmp" 2>/dev/null;""")
                appendLine("""  chmod 660 "$dir/$DB_NAME.tmp" 2>/dev/null;""")
                appendLine("""  chmod 660 "$dir/$WAL_NAME.tmp" 2>/dev/null;""")
                appendLine("""  chmod 660 "$dir/$SHM_NAME.tmp" 2>/dev/null;""")
                appendLine("""  mv "$dir/$DB_NAME.tmp" "$dir/$DB_NAME";""")
                appendLine("""  if [ -f "$dir/$WAL_NAME.tmp" ]; then mv "$dir/$WAL_NAME.tmp" "$dir/$WAL_NAME"; fi;""")
                appendLine("""  if [ -f "$dir/$SHM_NAME.tmp" ]; then mv "$dir/$SHM_NAME.tmp" "$dir/$SHM_NAME"; fi;""")
                appendLine("""  restorecon -R "$dir" 2>/dev/null;""")
                appendLine("  copied=1;")
                if (exitWhenCopied) {
                    appendLine("  exit 0;")
                }
                appendLine("fi")
            }
        }

        return if (loop) {
            """
            while true; do
              $body
              sleep 1
            done
            """.trimIndent()
        } else {
            """
            $body
            exit 1
            """.trimIndent()
        }
    }

    private fun runRootCommand(command: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")

            DataOutputStream(process.outputStream).use { os ->
                os.writeBytes(command)
                os.writeBytes("\n")
                os.flush()
            }

            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
}
