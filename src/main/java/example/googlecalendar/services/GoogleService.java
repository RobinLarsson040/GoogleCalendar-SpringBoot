package example.googlecalendar.services;

import java.io.IOException;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Events;
import example.googlecalendar.entities.CalendarDto;
import example.googlecalendar.entities.EventDto;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;


@Service
public class GoogleService {

    private static final String APPLICATION_NAME = "Movie-Nights";
    private static HttpTransport httpTransport = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private final DateTime minDate = new DateTime(new Date());

    public List<EventDto> getEvents(String tokenValue, String calendarId) {
        GoogleCredential credential = new GoogleCredential().setAccessToken(tokenValue);
        Calendar calendar =
                new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
        Events events = null;
        try {
            events = calendar.events().list(calendarId)
                    .setMaxResults(50)
                    .setTimeMin(minDate)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        List<EventDto> eventDtos = new ArrayList<>();
        events.getItems().forEach(e -> {
            EventDto eventDto = new EventDto();
            BeanUtils.copyProperties(e, eventDto);
            eventDto.setStartTime(e.getStart().getDateTime().toString());
            eventDtos.add(eventDto);
        });
        return eventDtos;
    }

    public List<CalendarDto> getCalendars(String tokenValue) throws IOException {
        GoogleCredential credential = new GoogleCredential().setAccessToken(tokenValue);
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName("applicationName").build();
        CalendarList calendarList;
        List<CalendarDto> calendarDtos = new ArrayList<>();
        String pageToken = null;
        do {
            calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();

            for (CalendarListEntry calendarListEntry : items) {
                CalendarDto calendarDto = new CalendarDto();
                BeanUtils.copyProperties(calendarListEntry, calendarDto);
                calendarDtos.add(calendarDto);
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null);

        return calendarDtos;
    }


}
