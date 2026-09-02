package com.editor.es.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.editor.es.MainActivity
import com.editor.es.R
import com.termux.terminal.TerminalSession
import java.util.concurrent.atomic.AtomicInteger

data class SessionRecord(
    val id: Int,
    val name: String,
    val session: TerminalSession
)

class TermuxService : Service() {

    companion object {
        const val CHANNEL_ID = "terminal_sessions"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_EXIT = "com.editor.es.ACTION_EXIT"

        private val sessions = LinkedHashMap<Int, SessionRecord>()
        private val sessionCounter = AtomicInteger(0)
        private val tagged = LinkedHashMap<String, Int>()
        var onExitRequested: (() -> Unit)? = null
        var onSessionsChanged: (() -> Unit)? = null

        var activeSession: TerminalSession? = null
            private set

        private var activeSessionId: Int? = null

        fun currentSessionId(): Int = activeSessionId ?: 0

        fun liveSession(): TerminalSession? = activeSession?.takeIf { it.isRunning }

        fun sessionById(id: Int): TerminalSession? = sessions[id]?.session?.takeIf { it.isRunning }

        fun allSessions(): List<SessionRecord> =
            sessions.values.filter { it.session.isRunning }.toList()

        fun sessionCount(): Int = sessions.values.count { it.session.isRunning }

        fun taggedSession(tag: String): Pair<Int, TerminalSession>? {
            val id = tagged[tag] ?: return null
            val record = sessions[id]?.takeIf { it.session.isRunning } ?: return null
            return record.id to record.session
        }

        fun registerTagged(context: Context, tag: String, session: TerminalSession, name: String? = null): Int {
            val id = sessionCounter.incrementAndGet()
            val sessionName = name ?: "Session $id"
            val record = SessionRecord(id, sessionName, session)
            sessions[id] = record
            tagged[tag] = id
            activeSession = session
            activeSessionId = id
            context.startForegroundService(Intent(context, TermuxService::class.java))
            notifyChanged()
            return id
        }

        fun unregisterTagged(context: Context, tag: String) {
            val id = tagged.remove(tag) ?: return
            val record = sessions.remove(id)
            record?.session?.finishIfRunning()
            if (activeSessionId == id) {
                val next = sessions.values.lastOrNull { it.session.isRunning }
                activeSession = next?.session
                activeSessionId = next?.id
            }
            if (sessions.isEmpty()) {
                context.stopService(Intent(context, TermuxService::class.java))
            }
            notifyChanged()
        }

        fun registerSession(context: Context, session: TerminalSession, name: String? = null): Int {
            val id = sessionCounter.incrementAndGet()
            val sessionName = name ?: "Session $id"
            val record = SessionRecord(id, sessionName, session)
            sessions[id] = record
            activeSession = session
            activeSessionId = id
            context.startForegroundService(Intent(context, TermuxService::class.java))
            notifyChanged()
            return id
        }

        fun setActiveSession(id: Int) {
            val record = sessions[id] ?: return
            activeSession = record.session
            activeSessionId = id
            notifyChanged()
        }

        fun killSession(context: Context, id: Int) {
            val record = sessions.remove(id) ?: return
            tagged.entries.removeAll { it.value == id }
            runCatching { record.session.finishIfRunning() }
            if (activeSessionId == id) {
                val next = sessions.values.lastOrNull { it.session.isRunning }
                activeSession = next?.session
                activeSessionId = next?.id
            }
            if (sessions.isEmpty()) {
                context.stopService(Intent(context, TermuxService::class.java))
            } else {
                Handler(Looper.getMainLooper()).post {
                    startForeground(NOTIFICATION_ID, buildNotification())
                }
            }
            notifyChanged()
        }

        fun unregisterSession(context: Context, id: Int) {
            sessions.remove(id)
            tagged.entries.removeAll { it.value == id }
            if (activeSessionId == id) {
                val next = sessions.values.lastOrNull { it.session.isRunning }
                activeSession = next?.session
                activeSessionId = next?.id
            }
            if (sessions.isEmpty()) {
                context.stopService(Intent(context, TermuxService::class.java))
            } else {
                Handler(Looper.getMainLooper()).post {
                    startForeground(NOTIFICATION_ID, buildNotification())
                }
            }
            notifyChanged()
        }

        private fun notifyChanged() {
            Handler(Looper.getMainLooper()).post { onSessionsChanged?.invoke() }
        }

        fun createChannel(context: Context) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.session_notification_channel),
                        NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runCatching { startForeground(NOTIFICATION_ID, buildNotification()) }
        if (intent?.action == ACTION_EXIT) {
            killAllSessions()
            Handler(Looper.getMainLooper()).post { onExitRequested?.invoke() }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_NOT_STICKY
    }

    private fun killAllSessions() {
        sessions.values.forEach { runCatching { it.session.finishIfRunning() } }
        sessions.clear()
        tagged.clear()
        activeSession = null
        activeSessionId = null
        notifyChanged()
    }

    private fun buildNotification(): Notification {
        val count = sessionCount()
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val exitIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TermuxService::class.java).setAction(ACTION_EXIT),
            PendingIntent.FLAG_IMMUTABLE
        )
        val text = if (count == 1) "1 active session" else "$count active sessions"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentIntent)
            .addAction(0, getString(R.string.session_notification_exit), exitIntent)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
