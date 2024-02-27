package ru.panyukovnn.calendarbot.service

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarListEntry
import com.google.api.services.calendar.model.Event
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class GoogleCalendarEventsFetcher {

    private val JSON_FACTORY: JsonFactory = GsonFactory.getDefaultInstance()
    private val TOKENS_DIRECTORY_PATH = "/tmp/calendar-bot/tokens"
    private val SCOPES = listOf(CalendarScopes.CALENDAR_READONLY)
    private val CREDENTIALS_FILE_PATH = "credentials.json"

    fun fetchDayEvens(now: LocalDateTime): List<Event> {
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
        val service = Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
            .setApplicationName("calendar-bot")
            .build()

        val start = DateTime(now.minusMinutes(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )
        val end = DateTime(now.plusDays(1).plusMinutes(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        )

        val calendars = service.calendarList().list().setPageToken(null).execute()
            .filter { it.key.equals("items") }
            .map { it.value as List<CalendarListEntry> }
            .flatMap { it }
            .filter { it.selected == true }

        return calendars.flatMap {
            service.events()
                .list(it.id)
                .setTimeMin(start)
                .setTimeMax(end)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute()
                .items
        }
            .sortedBy { it.start.dateTime?.value  }
    }

    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        val clientSecrets = GoogleClientSecrets.load(
            JSON_FACTORY,
            InputStreamReader(FileInputStream(CREDENTIALS_FILE_PATH))
        )

        val flow = GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        val receiver = LocalServerReceiver.Builder().setPort(8010).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }
}