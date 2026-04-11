package com.plantris.pastelist;

import android.app.TimePickerDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Locale;

public final class DateSheet {

    private DateSheet() {
        // Utility class.
    }

    public interface OnDateTimeSelectedListener {
        void onDateTimeSelected(String date, String time);
    }

    public static void show(
            @NonNull AppCompatActivity activity,
            String initialDate,
            String initialTime,
            @NonNull OnDateTimeSelectedListener listener
    ) {
        BottomSheetDialog dateDialog = new BottomSheetDialog(activity);
        ViewGroup root = activity.findViewById(android.R.id.content);
        View dateView = activity.getLayoutInflater().inflate(R.layout.add_task_date, root, false);
        dateDialog.setContentView(dateView);

        CalendarView calendarView = dateView.findViewById(R.id.calendarView);
        LinearLayout optionAddTime = dateView.findViewById(R.id.optionAddTime);
        calendarView.setMinDate(System.currentTimeMillis());
        TextView tvAddTime = dateView.findViewById(R.id.tvAddTime);
        Button btnSaveDate = dateView.findViewById(R.id.btnSaveDate);
        Button btnNoDate = dateView.findViewById(R.id.btnNoDate);

        final String[] selectedDate = {initialDate == null ? "" : initialDate};
        final String[] selectedTime = {initialTime == null ? "" : initialTime};

        tvAddTime.setText(selectedTime[0].isEmpty() ? activity.getString(R.string.add_time) : selectedTime[0]);

        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) ->
                selectedDate[0] = String.format(Locale.getDefault(), "%02d.%02d.%d", dayOfMonth, month + 1, year)
        );

        optionAddTime.setOnClickListener(timeView -> {
            Calendar now = Calendar.getInstance();
            TimePickerDialog timePicker = new TimePickerDialog(
                    activity,
                    (pickerView, hourOfDay, minute) -> {
                        selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        tvAddTime.setText(selectedTime[0]);
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    true
            );
            timePicker.show();
        });

        btnSaveDate.setOnClickListener(saveView -> {
            if (selectedDate[0].isEmpty()) {
                selectedTime[0] = "";
            }
            listener.onDateTimeSelected(selectedDate[0], selectedTime[0]);
            dateDialog.dismiss();
        });

        btnNoDate.setOnClickListener(clearView -> {
            listener.onDateTimeSelected("", "");
            dateDialog.dismiss();
        });

        dateDialog.show();
    }
}
