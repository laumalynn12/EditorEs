package com.editor.es.desktop

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.editor.es.proot.ProotConfig
import com.editor.es.service.TermuxService
import java.io.File

object DesktopManager {

    private const val Tag = "desktop"
    private const val VncSocketName = ".editor.vnc"

    fun isInstalled(context: Context): Boolean = ProotConfig.isInstalled(context)

    fun vncSocketPath(context: Context): String =
        File(ProotConfig.rootfsDir(context), "tmp/$VncSocketName").absolutePath

    fun startDesktop(context: Context, onLine: (String) -> Unit, onDone: (Boolean, String?) -> Unit) {
        if (!isInstalled(context)) {
            onDone(false, "ubuntu not installed")
            return
        }
        TermuxService.taggedSession(Tag)?.let { (_, session) ->
            session.finishIfRunning()
        }
        val socketGuestPath = "/tmp/$VncSocketName"
        val script = """
            rm -f $socketGuestPath /tmp/.X1-lock /tmp/.X11-unix/X1
            export HOME=/root
            export USER=root
            mkdir -p /root/.vnc
            printf '#!/usr/bin/env bash\nunset SESSION_MANAGER\nunset DBUS_SESSION_BUS_ADDRESS\nexec startxfce4\n' > /root/.vnc/xstartup
            chmod +x /root/.vnc/xstartup
            Xtigervnc :1 -geometry 1280x720 -depth 24 -rfbunixpath $socketGuestPath -SecurityTypes None -localhost -AlwaysShared &
            VNC_PID=${'$'}!
            for i in 1 2 3 4 5 6 7 8 9 10; do
              [ -S $socketGuestPath ] && break
              sleep 1
            done
            if [ -S $socketGuestPath ]; then
              echo '==> vnc ready'
              DISPLAY=:1 startxfce4 &
              wait ${'$'}VNC_PID
            else
              echo '==> vnc failed'
              exit 1
            fi
        """.trimIndent()
        val args = ProotConfig.commandArgs(context, script, "/root")
        val env = ProotConfig.prootEnv(context)
        val session = com.termux.terminal.TerminalSession(
            ProotConfig.prootBinary(context),
            context.filesDir.absolutePath,
            args.toTypedArray(),
            env,
            null
        )
        TermuxService.registerTagged(context, Tag, session, "Desktop")
        var foundReady = false
        Thread {
            for (i in 1..15) {
                val transcript = runCatching {
                    session.getEmulator().screen.getTranscriptText()
                }.getOrNull() ?: ""
                transcript.lines().forEach { line ->
                    onLine(line)
                    if (line.contains("==> vnc ready")) foundReady = true
                    if (line.contains("==> vnc failed")) {
                        onDone(false, "vnc server failed to start")
                        return@Thread
                    }
                }
                if (foundReady) {
                    onDone(true, null)
                    return@Thread
                }
                if (!session.isRunning) {
                    onDone(false, "session exited before vnc ready")
                    return@Thread
                }
                Thread.sleep(1000)
            }
            if (!foundReady) onDone(false, "timeout waiting for vnc")
        }.start()
    }

    fun openViewer(context: Context) {
        val socketHostPath = vncSocketPath(context)
        val uri = "vnc://localhost?UnixSocket=$socketHostPath&SecurityType=1&ViewOnly=false&ConnectionName=EditorEs%20Desktop"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage(context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun stopDesktop(context: Context) {
        TermuxService.unregisterTagged(context, Tag)
    }

    fun isRunning(): Boolean =
        TermuxService.taggedSession(Tag)?.second?.isRunning == true
}

</content>