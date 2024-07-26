package organotiki.mobile.mobilestreet;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Thanasis on 7/6/2016.
 */
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


    Integer position;
    Communicator comm;
    boolean limit;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        comm = (Communicator) getActivity();
        // Create a new instance of DatePickerDialog and return it

        DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
        DatePicker mDatePicker = dialog.getDatePicker();

        if (limit) {
            Calendar cal = Calendar.getInstance();
            Date maxDate = cal.getTime();
            cal.add(Calendar.MONTH, -3);
            Date minDate = cal.getTime();

            mDatePicker.setMaxDate(maxDate.getTime());
            mDatePicker.setMinDate(minDate.getTime());
        }

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d("asdfg", day+"/"+String.valueOf(month+1)+"/"+year);
         comm.respondDate(position, year, month+1, day);
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setLimit(boolean limit) {
        this.limit = limit;
    }
}