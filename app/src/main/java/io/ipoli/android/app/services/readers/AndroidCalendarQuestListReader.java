package io.ipoli.android.app.services.readers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Minutes;

import java.util.Set;

import io.ipoli.android.Constants;
import io.ipoli.android.app.utils.LocalStorage;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.persistence.RecurrentQuestPersistenceService;
import me.everything.providers.android.calendar.CalendarProvider;
import me.everything.providers.android.calendar.Event;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 5/11/16.
 */
public class AndroidCalendarQuestListReader implements ListReader<Quest> {

    private final Set<String> questIds;
    private final Context context;
    private final CalendarProvider calendarProvider;
    private final RecurrentQuestPersistenceService recurrentQuestPersistenceService;

    public AndroidCalendarQuestListReader(Context context, CalendarProvider calendarProvider, LocalStorage localStorage, RecurrentQuestPersistenceService recurrentQuestPersistenceService) {
        this.context = context;
        this.calendarProvider = calendarProvider;
        this.recurrentQuestPersistenceService = recurrentQuestPersistenceService;
        this.questIds = localStorage.readStringSet(Constants.KEY_ANDROID_CALENDAR_QUESTS_TO_UPDATE);
    }

    @Override
    public Observable<Quest> read() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return Observable.empty();
        }

        return Observable.just(questIds).concatMapIterable(questIds -> questIds).concatMap(questId -> {
            Event e = calendarProvider.getEvent(Integer.valueOf(questId));
            Quest q = new Quest(e.title);
            DateTime startDateTime = new DateTime(e.dTStart, DateTimeZone.forID(e.eventTimeZone));
            DateTime endDateTime = new DateTime(e.dTend, DateTimeZone.forID(e.eventTimeZone));
            q.setDuration(Minutes.minutesBetween(startDateTime, endDateTime).getMinutes());
            q.setStartMinute(startDateTime.getMinuteOfDay());
            q.setEndDate(startDateTime.toLocalDate().toDate());
            q.setSource("google-calendar");
            if (e.originalId != null) {
                return recurrentQuestPersistenceService.findByExternalSourceMappingId("googleCalendar", e.originalId).flatMap(recurrentQuest -> {
                    q.setRecurrentQuest(recurrentQuest);
                    return Observable.just(q);
                });
            } else {
                return Observable.just(q);
            }
        }).compose(applyAPISchedulers());
    }

    private <T> Observable.Transformer<T, T> applyAPISchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
