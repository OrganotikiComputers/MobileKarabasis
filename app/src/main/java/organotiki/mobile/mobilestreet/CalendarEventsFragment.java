package organotiki.mobile.mobilestreet;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import organotiki.mobile.mobilestreet.CityFilterFragment;
import organotiki.mobile.mobilestreet.Communicator;
import organotiki.mobile.mobilestreet.CustomerBrowserFragment;
import organotiki.mobile.mobilestreet.R;
import organotiki.mobile.mobilestreet.VolleyRequests;
import organotiki.mobile.mobilestreet.objects.Customer;
import organotiki.mobile.mobilestreet.objects.GlobalVar;
import organotiki.mobile.mobilestreet.objects.UserEvents;

public class CalendarEventsFragment extends DialogFragment {
    private static final String TAG = "MainActivity";
    private Calendar currentCalender = Calendar.getInstance(Locale.getDefault());
    private SimpleDateFormat dateFormatForDisplaying = new SimpleDateFormat("dd-M-yyyy hh:mm:ss a", Locale.getDefault());
    private SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMM - yyyy", Locale.getDefault());
    private boolean shouldShow = false;
    private CompactCalendarView compactCalendarView;
    private TextView textView_month;
    Realm realm;
    GlobalVar gVar;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try{
            View mainTabView = inflater.inflate(R.layout.fragment_calendar_events,container,false);
            realm = Realm.getDefaultInstance();
            gVar = realm.where(GlobalVar.class).findFirst();
            final List<String> mutableBookings = new ArrayList<>();

            final ListView bookingsListView = mainTabView.findViewById(R.id.bookings_listview);
            final Button showPreviousMonthBut = mainTabView.findViewById(R.id.prev_button);
            final Button showNextMonthBut = mainTabView.findViewById(R.id.next_button);
            textView_month=mainTabView.findViewById(R.id.textView_month);
            final ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, mutableBookings);
            bookingsListView.setAdapter(adapter);
            compactCalendarView = mainTabView.findViewById(R.id.compactcalendar_view);

            // below allows you to configure color for the current day in the month
            // compactCalendarView.setCurrentDayBackgroundColor(getResources().getColor(R.color.black));
            // below allows you to configure colors for the current day the user has selected
            // compactCalendarView.setCurrentSelectedDayBackgroundColor(getResources().getColor(R.color.dark_red));
            compactCalendarView.setUseThreeLetterAbbreviation(false);
            compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
            compactCalendarView.setIsRtl(false);
            compactCalendarView.displayOtherMonthDays(false);
            //compactCalendarView.setIsRtl(true);

            RealmResults<UserEvents> userEvents=realm.where(UserEvents.class).equalTo("username",gVar.getMyUser().getUsername()).findAll();
            for(UserEvents event:userEvents){
                String StringDate=event.getDue_date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy"); // here set the pattern as you date in string was containing like date/month/year
                Date d = null;
                try {
                    d = sdf.parse(StringDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                int month = (cal.get(Calendar.MONTH));
                int dd = (cal.get(Calendar.DATE)-1);
                int year = (cal.get(Calendar.YEAR));


                addEvents(dd,month,year,event);
            }
        /*loadEvents();
        loadEventsForYear(2017);*/
            compactCalendarView.invalidate();

            //logEventsByMonth(compactCalendarView);

            // below line will display Sunday as the first day of the week
            // compactCalendarView.setShouldShowMondayAsFirstDay(false);

            // disable scrolling calendar
            // compactCalendarView.shouldScrollMonth(false);

            // show days from other months as greyed out days
            // compactCalendarView.displayOtherMonthDays(true);

            // show Sunday as first day of month
            // compactCalendarView.setShouldShowMondayAsFirstDay(false);

            //set initial title
            textView_month.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
            Date currentTime = Calendar.getInstance().getTime();
            List<Event> bookingsFromTodayMap = compactCalendarView.getEvents(currentTime);
            if (bookingsFromTodayMap != null) {
                Log.d(TAG, bookingsFromTodayMap.toString());
                mutableBookings.clear();
                for (Event booking : bookingsFromTodayMap) {
                    mutableBookings.add((String) booking.getData());
                }
                adapter.notifyDataSetChanged();
            }
            //set title on calendar scroll
            compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
                @Override
                public void onDayClick(Date dateClicked) {
                    textView_month.setText(dateFormatForMonth.format(dateClicked));
                    List<Event> bookingsFromMap = compactCalendarView.getEvents(dateClicked);
                    Log.d(TAG, "inside onclick " + dateFormatForDisplaying.format(dateClicked));
                    if (bookingsFromMap != null) {
                        Log.d(TAG, bookingsFromMap.toString());
                        mutableBookings.clear();
                        for (Event booking : bookingsFromMap) {
                            mutableBookings.add((String) booking.getData());
                        }
                        adapter.notifyDataSetChanged();
                    }

                }

                @Override
                public void onMonthScroll(Date firstDayOfNewMonth) {
                    textView_month.setText(dateFormatForMonth.format(firstDayOfNewMonth));
                }
            });

            showPreviousMonthBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compactCalendarView.scrollLeft();
                }
            });

            showNextMonthBut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    compactCalendarView.scrollRight();
                }
            });



            compactCalendarView.setAnimationListener(new CompactCalendarView.CompactCalendarAnimationListener() {
                @Override
                public void onOpened() {
                }

                @Override
                public void onClosed() {
                }
            });



            // uncomment below to show indicators above small indicator events
            // compactCalendarView.shouldDrawIndicatorsBelowSelectedDays(true);

            // uncomment below to open onCreate
            //openCalendarOnCreate(v);

            return mainTabView;
        }catch (Exception ex){
            return null;
        }
    }

    @NonNull
    private View.OnClickListener getCalendarShowLis() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!compactCalendarView.isAnimating()) {
                    if (shouldShow) {
                        compactCalendarView.showCalendar();
                    } else {
                        compactCalendarView.hideCalendar();
                    }
                    shouldShow = !shouldShow;
                }
            }
        };
    }

    @NonNull
    private View.OnClickListener getCalendarExposeLis() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!compactCalendarView.isAnimating()) {
                    if (shouldShow) {
                        compactCalendarView.showCalendarWithAnimation();
                    } else {
                        compactCalendarView.hideCalendarWithAnimation();
                    }
                    shouldShow = !shouldShow;
                }
            }
        };
    }

    private void openCalendarOnCreate(View v) {
        final RelativeLayout layout = v.findViewById(R.id.main_content);
        ViewTreeObserver vto = layout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < 16) {
                    layout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                compactCalendarView.showCalendarWithAnimation();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        textView_month.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
        // Set to current day on resume to set calendar to latest day
        // toolbar.setTitle(dateFormatForMonth.format(new Date()));
    }

    private void loadEvents() {
        /*addEvents(-1, -1);
        addEvents(Calendar.DECEMBER, -1);
        addEvents(Calendar.AUGUST, -1);*/
    }

    private void loadEventsForYear(int year) {
       /* addEvents(Calendar.DECEMBER, year);
        addEvents(Calendar.AUGUST, year);*/
    }

    private void logEventsByMonth(CompactCalendarView compactCalendarView) {
        currentCalender.setTime(new Date());
        currentCalender.set(Calendar.DAY_OF_MONTH, 1);
        currentCalender.set(Calendar.MONTH, Calendar.AUGUST);
        List<String> dates = new ArrayList<>();
        for (Event e : compactCalendarView.getEventsForMonth(new Date())) {
            dates.add(dateFormatForDisplaying.format(e.getTimeInMillis()));
        }
        Log.d(TAG, "Events for Aug with simple date formatter: " + dates);
        Log.d(TAG, "Events for Aug month using default local and timezone: " + compactCalendarView.getEventsForMonth(currentCalender.getTime()));
    }

    private void addEvents(int day,int month, int year,UserEvents event) {
        currentCalender.setTime(new Date());
        currentCalender.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = currentCalender.getTime();

        currentCalender.setTime(firstDayOfMonth);
        if (month > -1) {
            currentCalender.set(Calendar.MONTH, month);
        }
        if (year > -1) {
            currentCalender.set(Calendar.ERA, GregorianCalendar.AD);
            currentCalender.set(Calendar.YEAR, year);
        }
        currentCalender.add(Calendar.DATE, day);
        setToMidnight(currentCalender);
        long timeInMillis = currentCalender.getTimeInMillis();

        List<Event> events = getEvents(timeInMillis, day,event);

        compactCalendarView.addEvents(events);

    }

    private List<Event> getEvents(long timeInMillis, int day,UserEvents event) {

           if(event.getTaskstatus().equals("Completed")){
               if(TextUtils.isEmpty(event.getAccountcode())){
                   return Arrays.asList(new Event(Color.argb(255, 1, 50, 32), timeInMillis, event.getLeadname()+" "+event.getStreet() +" "+event.getCity() +" "+event.getPhone()+" "+event.getSubject()+" ΟΛΟΚΛΗΡΩΘΗΚΕ"));
               }else{
                   return Arrays.asList(new Event(Color.argb(255, 1, 50, 32), timeInMillis, event.getAccountcode()+" "+event.getAccountname() +" "+event.getSubject()+" ΟΛΟΚΛΗΡΩΘΗΚΕ"));
               }
           }else{
               if(TextUtils.isEmpty(event.getAccountcode())){
                   return Arrays.asList(new Event(Color.BLUE, timeInMillis, event.getLeadname()+" "+event.getStreet() +" "+event.getCity() +" "+event.getPhone()+" "+event.getSubject()));
               }else {
                   return Arrays.asList(new Event(Color.BLUE, timeInMillis, event.getAccountcode() + " " + event.getAccountname() + " " + event.getSubject()));
               }
           }
       /* } else if ( day > 2 && day <= 4) {
            return Arrays.asList(
                    new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis)),
                    new Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + new Date(timeInMillis)));
        } else {
            return Arrays.asList(
                    new Event(Color.argb(255, 169, 68, 65), timeInMillis, "Event at " + new Date(timeInMillis) ),
                    new Event(Color.argb(255, 100, 68, 65), timeInMillis, "Event 2 at " + new Date(timeInMillis)),
                    new Event(Color.argb(255, 70, 68, 65), timeInMillis, "Event 3 at " + new Date(timeInMillis)));
        }*/
    }

    private void setToMidnight(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}