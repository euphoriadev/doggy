package ru.euphoria.doggy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.flexbox.FlexboxLayout;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindColor;
import butterknife.BindView;
import ru.euphoria.doggy.api.VKApi;
import ru.euphoria.doggy.api.model.User;
import ru.euphoria.doggy.common.DataHolder;
import ru.euphoria.doggy.common.Pair;
import ru.euphoria.doggy.util.AndroidUtil;
import ru.euphoria.doggy.util.ArrayUtil;
import ru.euphoria.doggy.util.MessageStats;
import ru.euphoria.doggy.util.UserUtil;

import static ru.euphoria.doggy.MessageStatsActivity.KEY_HOLDER_STATS;

public class MessageGraphActivity extends BaseActivity {
    private MessageStats stats;
    private int peer;

    public static void start(Context context) {
        Intent starter = new Intent(context, MessageGraphActivity.class);
        context.startActivity(starter);
    }

    @BindView(R.id.card_chart_members) CardView cardMembers;
    @BindView(R.id.chart_messages) LineChart chartMessages;
    @BindView(R.id.chart_members) PieChart chartMembers;
    @BindView(R.id.chart_legend) FlexboxLayout chatLegend;

    @BindColor(R.color.graph_line_blue) int blueColor;
    @BindColor(R.color.graph_line_pink) int pinkColor;

    private int secondaryTextColor;

    @SuppressLint("CheckResult")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages_graph);
        getSupportActionBar().setTitle(R.string.messages_graph);
        getSupportActionBar().setSubtitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        peer = getIntent().getIntExtra("peer", -1);
        secondaryTextColor = AndroidUtil.getAttrColor(this, android.R.attr.textColorSecondary);
        stats = (MessageStats) DataHolder.getObject(KEY_HOLDER_STATS);

        makeMessagesChart();

        if (peer > VKApi.PEER_OFFSET) {
            makeMembersChart();
        } else {
            cardMembers.setVisibility(View.GONE);
        }
    }

    private void makeMessagesChart() {
        List<Pair<Long, Integer>> days = stats.days().copyAsList();
        Collections.sort(days, (o1, o2) -> Long.compare(o1.first, o2.first));

        ArrayList<Entry> entries = new ArrayList<>(days.size());
        for (Pair<Long, Integer> day : days) {
            entries.add(new Entry(day.first, day.second));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Messages");
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setDrawValues(false);
        dataSet.setDrawIcons(false);
        dataSet.setDrawCircles(true);
        dataSet.setColor(blueColor);
        dataSet.setCircleColor(blueColor);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setLineWidth(3f);

        LineData data = new LineData(dataSet);

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return dateFormat.format(TimeUnit.DAYS.toMillis((long) value));
            }
        };

        XAxis x = chartMessages.getXAxis();
        x.setValueFormatter(formatter);
        x.setLabelCount(4, false);
        x.setTextColor(secondaryTextColor);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setSpaceMin(4f);
        x.setXOffset(6f);
        x.setYOffset(6f);

        YAxis y = chartMessages.getAxisLeft();
        y.setLabelCount(6, false);
        y.setTextColor(secondaryTextColor);
        y.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        y.setDrawGridLines(false);
        y.setAxisLineColor(secondaryTextColor);
        y.setAxisMinimum(0);
        y.setSpaceMin(4f);
        y.setXOffset(6f);
        y.setYOffset(6f);

        chartMessages.getDescription().setEnabled(false);
        chartMessages.getAxisRight().setEnabled(false);
        chartMessages.getLegend().setEnabled(false);

        chartMessages.setPinchZoom(false);
        chartMessages.setTouchEnabled(true);
        chartMessages.setDragEnabled(true);
        chartMessages.setScaleEnabled(true);

        chartMessages.setData(data);
        chartMessages.setDrawGridBackground(false);
        chartMessages.setMarker(new MessageMarkerView(this));
        chartMessages.invalidate();
    }

    private void makeMembersChart() {
        List<Pair<Integer, Integer>> members = stats.members().copyByCount();

        ArrayList<PieEntry> entries = new ArrayList<>(members.size());
        for (Pair<Integer, Integer> member : members) {
            User user = UserUtil.getCachedUser(member.first);
            if (user == null) continue;

            entries.add(new PieEntry(member.second, user.toString()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Active Members");
        dataSet.setDrawValues(false);
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(4f);

        ArrayList<Integer> colors = new ArrayList<>();
        colors.addAll(ArrayUtil.asList(ColorTemplate.VORDIPLOM_COLORS));
        colors.addAll(ArrayUtil.asList(ColorTemplate.JOYFUL_COLORS));
        colors.addAll(ArrayUtil.asList(ColorTemplate.COLORFUL_COLORS));
        colors.addAll(ArrayUtil.asList(ColorTemplate.LIBERTY_COLORS));
        colors.addAll(ArrayUtil.asList(ColorTemplate.PASTEL_COLORS));

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(16f);
        data.setValueTextColor(Color.WHITE);

        chartMembers.getDescription().setEnabled(false);
        chartMembers.setRotationEnabled(false);
        chartMembers.setDrawEntryLabels(false);
        chartMembers.setUsePercentValues(false);
        chartMembers.setData(data);
        chartMembers.setHoleColor(Color.TRANSPARENT);
        chartMembers.setHoleRadius(65f);
        chartMembers.highlightValues(null);
        chartMembers.invalidate();

        makeMembersLegend(chartMembers.getLegend());
    }

    private void makeMembersLegend(Legend legend) {
        legend.setEnabled(false);
        LayoutInflater inflater = getLayoutInflater();
        for (LegendEntry entry : legend.getEntries()) {
            View item = inflater.inflate(R.layout.legend_item, chatLegend, false);
            View color = item.findViewById(R.id.chart_legend_item_color);
            TextView label = item.findViewById(R.id.chart_legend_item_label);

            color.setBackgroundTintList(ColorStateList.valueOf(entry.formColor));
            label.setText(entry.label);
            chatLegend.addView(item);
        }
    }

    private class MessageMarkerView extends MarkerView {
        private DateFormat dateFormat = DateFormat.getDateInstance();

        private TextView date;
        private TextView title;
        private TextView count;
        private View color;

        public MessageMarkerView(Context context) {
            super(context, R.layout.graph_marker_message);

            this.date = findViewById(R.id.marker_item_date);
            this.title = findViewById(R.id.marker_item_title);
            this.count = findViewById(R.id.marker_item_count);
            this.color = findViewById(R.id.marker_item_color);
        }

        @Override
        public void draw(Canvas canvas, float posX, float posY) {
            super.draw(canvas, posX, posY);
        }

        @Override
        public MPPointF getOffset() {
            float x = -(getWidth() / 2);
            float y = -getHeight();
            return new MPPointF(x, y);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            String format = dateFormat.format(TimeUnit.DAYS.toMillis((long) e.getX()));
            date.setText(format);

            count.setText(String.valueOf((int) e.getY()));

            super.refreshContent(e, highlight);
        }
    }
}
