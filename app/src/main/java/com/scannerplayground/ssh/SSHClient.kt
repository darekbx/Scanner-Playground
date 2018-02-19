package com.scannerplayground.ssh

import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.lang.Exception


class SSHClient {

    companion object {
        val HOST = "raspberrypi"
        val USER = "pi"
        val PASS = "raspberry"
        val PORT = 22

        val SCAN_COMMAND = "sudo python /home/pi-scanner/scan.py"
    }

    private var session: Session? = null

    fun open(limit: Long = 5, response: (String) -> Unit, error: (String) -> Unit) =
            Observable
                    .fromCallable { JSch() }
                    .map { createSession(it).apply { connect() } }
                    .doOnNext { session = it }
                    .flatMap { runCommand(limit, it, response) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .doOnTerminate { session?.disconnect() }
                    .subscribe({ next -> }, { e -> error(e.message ?: "Error") }, { session?.disconnect() })

    private fun runCommand(limit: Long, session: Session, response: (String) -> Unit) =
            Observable
                    .fromCallable {
                        with(session.openChannel("exec") as ChannelExec) {
                            setCommand(SCAN_COMMAND)
                            connect()
                            val output = inputStream.use {
                                try {
                                    it.bufferedReader(charset = Charsets.UTF_8).use {
                                        it.readText()
                                    }
                                } catch (e: Exception) {
                                    ""
                                }

                            }
                            disconnect()
                            response(output)
                        }
                    }
                    .repeat(limit)

    private fun createSession(jsch: JSch) =
            jsch.getSession(USER, HOST, PORT)
                    .apply {
                        setPassword(PASS)
                        setConfig("StrictHostKeyChecking", "no")
                        timeout = 3000
                    }

}