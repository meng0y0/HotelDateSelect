package com.htkj.hoteldateselect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.htkj.hoteldateselect.adapter.MonthTimeAdapter;
import com.htkj.hoteldateselect.bean.DayTimeEntity;
import com.htkj.hoteldateselect.bean.MonthTimeEntity;
import com.htkj.hoteldateselect.bean.UpdataCalendar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView startTime;          //开始时间
    private TextView stopTime;           //结束时间
    private TextView plan_time_txt_month;
    private RecyclerView reycler;
    private MonthTimeAdapter adapter;
    private ArrayList<MonthTimeEntity> datas = new ArrayList<>();

    public static int today = 0;

    public static DayTimeEntity startDay;
    public static DayTimeEntity stopDay;

    private int mSuspensionHeight;
    private int mCurrentPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);

        initView();
        initData();

        EventBus.getDefault().register(this);

    }

    private void initData() {
        startDay = new DayTimeEntity(0, 0, 0, 0);
        stopDay = new DayTimeEntity(-1, -1, -1, -1);

        Calendar c = Calendar.getInstance();
        today = c.get(Calendar.DAY_OF_MONTH);

        c.add(Calendar.MONTH, 1);
        int nextYear = c.get(Calendar.YEAR);
        int nextMonth = c.get(Calendar.MONTH);

        plan_time_txt_month.setText(nextYear + "年" + nextMonth + "月");

        for (int i = 0; i < 3; i++) {
            datas.add(new MonthTimeEntity(nextYear, nextMonth, nextYear + "年" + nextMonth + "月"));
            if (nextMonth == 12) {
                nextMonth = 0;
                nextYear = nextYear + 1;
            }
            nextMonth = nextMonth + 1;
        }

        adapter = new MonthTimeAdapter(datas, MainActivity.this);
        reycler.setAdapter(adapter);

    }

    private void initView() {
        startTime = findViewById(R.id.plan_time_txt_start);
        stopTime = findViewById(R.id.plan_time_txt_stop);
        plan_time_txt_month = findViewById(R.id.plan_time_txt_month);
        reycler = findViewById(R.id.plan_time_calender);
        final LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayout.VERTICAL, false);

        reycler.setLayoutManager(layoutManager);

        reycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mSuspensionHeight = plan_time_txt_month.getHeight();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                View view = layoutManager.findViewByPosition(mCurrentPosition + 1);
                if (view != null) {
                    if (view.getTop() <= mSuspensionHeight) {
                        plan_time_txt_month.setY(-(mSuspensionHeight - view.getTop()));
                    } else {
                        plan_time_txt_month.setY(0);
                    }
                }

                if (mCurrentPosition != layoutManager.findFirstVisibleItemPosition()) {
                    mCurrentPosition = layoutManager.findFirstVisibleItemPosition();
                    plan_time_txt_month.setY(0);
                    plan_time_txt_month.setText(datas.get(mCurrentPosition).getSticky());
                }
            }
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UpdataCalendar event) throws ParseException {
        adapter.notifyDataSetChanged();
//        startTime.setText("入住:" + startDay.getYear() + "年" + startDay.getMonth() + "月" + startDay.getDay() + "日" + "\n");
//        if (stopDay.getDay() == -1) {
//            stopTime.setText("结束" + "\n" + "时间");
//        } else {
//            stopTime.setText("离店:" + stopDay.getYear() + "年" + stopDay.getMonth() + "月" + stopDay.getDay() + "日" + "\n");
//        }

        long s = dateToStamp(stopDay.getYear() + "年" + stopDay.getMonth() + "月" + stopDay.getDay() + "日") -
                dateToStamp(startDay.getYear() + "年" + startDay.getMonth() + "月" + startDay.getDay() + "日");
        startTime.setText(String.valueOf(s));
        stopTime.setText(String.valueOf(s / (1000 * 60 * 60 * 24)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /*
     * 将时间转换为时间戳
     */
    public static long dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        return ts;
    }
}
