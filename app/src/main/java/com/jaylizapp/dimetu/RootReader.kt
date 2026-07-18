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
        "/data/data/com.whatsapp.w4b/databases"
    )

    fun requestRoot(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)

            os.writeBytes("exit\n")
            os.flush()

            process.waitFor() == 0

        } catch (e: Exception) {
            false
        }
    }

    fun copyWhatsAppDb(context: Context): Boolean {

        val dir = context.filesDir.absolutePath
        val uid = android.os.Process.myUid()

        for (base in whatsappPaths) {

            val db = "$base/$DB_NAME"

            val cmd = """
                if [ -f "$db" ]; then
                    cp "$base/$DB_NAME" "$dir/$DB_NAME.tmp"
                    cp "$base/$WAL_NAME" "$dir/$WAL_NAME.tmp" 2>/dev/null
                    cp "$base/$SHM_NAME" "$dir/$SHM_NAME.tmp" 2>/dev/null
                    
                    chown $uid:$uid "$dir/$DB_NAME.tmp"
                    chown $uid:$uid "$dir/$WAL_NAME.tmp" 2>/dev/null
                    chown $uid:$uid "$dir/$SHM_NAME.tmp" 2>/dev/null
                    
                    chmod 666 "$dir/$DB_NAME.tmp"
                    chmod 666 "$dir/$WAL_NAME.tmp" 2>/dev/null
                    chmod 666 "$dir/$SHM_NAME.tmp" 2>/dev/null
                    
                    mv "$dir/$DB_NAME.tmp" "$dir/$DB_NAME"
                    mv "$dir/$WAL_NAME.tmp" "$dir/$WAL_NAME" 2>/dev/null
                    mv "$dir/$SHM_NAME.tmp" "$dir/$SHM_NAME" 2>/dev/null
                    
                    restorecon -R "$dir"
                    
                    exit 0
                fi
            """.trimIndent()

            val process = Runtime.getRuntime().exec("su")

            val os = DataOutputStream(process.outputStream)

            os.writeBytes(cmd)
            os.writeBytes("\nexit\n")
            os.flush()

            if (process.waitFor() == 0) {

                if (isDbReady(context))
                    return true
            }
        }

        return false
    }

    fun startRootDaemon(context: Context) {

        if (daemonProcess != null)
            return

        val dir = context.filesDir.absolutePath
        val uid = android.os.Process.myUid()

        val script = buildString {

            append("while true; do ")

            whatsappPaths.forEach { base ->

                append(
                    """
                    if [ -f "$base/$DB_NAME" ]; then
                        cp "$base/$DB_NAME" "$dir/$DB_NAME.tmp";
                        cp "$base/$WAL_NAME" "$dir/$WAL_NAME.tmp" 2>/dev/null;
                        cp "$base/$SHM_NAME" "$dir/$SHM_NAME.tmp" 2>/dev/null;

                        chown $uid:$uid "$dir/$DB_NAME.tmp";
                        chown $uid:$uid "$dir/$WAL_NAME.tmp" 2>/dev/null;
                        chown $uid:$uid "$dir/$SHM_NAME.tmp" 2>/dev/null;

                        chmod 666 "$dir/$DB_NAME.tmp";
                        chmod 666 "$dir/$WAL_NAME.tmp" 2>/dev/null;
                        chmod 666 "$dir/$SHM_NAME.tmp" 2>/dev/null;

                        mv "$dir/$DB_NAME.tmp" "$dir/$DB_NAME";
                        mv "$dir/$WAL_NAME.tmp" "$dir/$WAL_NAME" 2>/dev/null;
                        mv "$dir/$SHM_NAME.tmp" "$dir/$SHM_NAME" 2>/dev/null;
                        
                        restorecon -R "$dir";

                        break;
                    fi;
                    """.trimIndent()
                )

            }

            append("sleep 3; done")
        }

        Thread {

            try {

                daemonProcess =
                    Runtime.getRuntime().exec(arrayOf("su"))

                val os =
                    DataOutputStream(daemonProcess!!.outputStream)

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

        val dir = context.filesDir

        val db = File(dir, DB_NAME)

        return db.exists() &&
                db.length() > 0

    }

    fun getLocalDatabase(context: Context): File {

        return File(
            context.filesDir,
            DB_NAME
        )

    }
}