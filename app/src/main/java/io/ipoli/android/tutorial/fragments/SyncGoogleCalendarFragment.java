package io.ipoli.android.tutorial.fragments;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.ipoli.android.Constants;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.utils.LocalStorage;
import me.everything.providers.android.calendar.Calendar;
import me.everything.providers.android.calendar.CalendarProvider;

public class SyncGoogleCalendarFragment extends Fragment {
    private static final int READ_CAlENDAR_REQUEST_CODE = 100;
    @Inject
    Bus eventBus;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sync_google_calendar, container, false);
        unbinder = ButterKnife.bind(this, v);
        App.getAppComponent(getContext()).inject(this);
        return v;
    }

    @OnClick(R.id.sync_calendar)
    public void onSyncCalendar(View v) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_CALENDAR},
                    READ_CAlENDAR_REQUEST_CODE);
        } else {
            syncWithGoogleCalendar();
        }
    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_CAlENDAR_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                syncWithGoogleCalendar();
            }
        }
    }

    private void syncWithGoogleCalendar() {
        CalendarProvider provider = new CalendarProvider(getContext());
        List<Calendar> calendars = provider.getCalendars().getList();
        LocalStorage localStorage = LocalStorage.of(getContext());
        Set<String> calendarIds = new HashSet<>();
        for (Calendar c : calendars) {
            if (c.visible) {
                calendarIds.add(String.valueOf(c.id));
            }
        }
        localStorage.saveStringSet(Constants.KEY_CALENDARS_TO_SYNC, calendarIds);
        localStorage.saveStringSet(Constants.KEY_SELECTED_ANDROID_CALENDARS, calendarIds);
    }
}
