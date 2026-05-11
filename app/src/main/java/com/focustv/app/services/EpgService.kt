package com.focustv.app.services

import com.focustv.app.BuildConfig
import com.focustv.app.core.LiveProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EpgService {
    suspend fun load(): List<LiveProgram> = withContext(Dispatchers.IO) {
        val url = BuildConfig.EPG_URL
        if (url.isBlank()) return@withContext emptyList()
        try {
            val response = HttpClient.client.newCall(HttpClient.request(url)).execute()
            val xml = response.body?.string().orEmpty()
            parseXmlTv(xml)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseXmlTv(xml: String): List<LiveProgram> {
        val result = mutableListOf<LiveProgram>()
        val factory = XmlPullParserFactory.newInstance()
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var event = parser.eventType
        var channel = ""
        var start = 0L
        var stop = 0L
        var title = ""
        var desc = ""

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "programme" -> {
                            channel = parser.getAttributeValue(null, "channel") ?: ""
                            start = parseDate(parser.getAttributeValue(null, "start") ?: "")
                            stop = parseDate(parser.getAttributeValue(null, "stop") ?: "")
                            title = ""
                            desc = ""
                        }
                        "title" -> title = parser.nextText()
                        "desc" -> desc = parser.nextText()
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "programme") {
                        result += LiveProgram(channel, title, desc, start, stop)
                    }
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun parseDate(v: String): Long {
        return try {
            val compact = v.take(14)
            val sdf = SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
            sdf.timeZone = TimeZone.getDefault()
            sdf.parse(compact)?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }
}
