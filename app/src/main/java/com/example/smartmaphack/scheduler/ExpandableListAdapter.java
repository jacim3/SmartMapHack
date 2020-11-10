package com.example.smartmaphack.scheduler;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartmaphack.R;
import com.example.smartmaphack.alarm.AlarmReceiver;
import com.example.smartmaphack.dbhelper.DBHelper;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static final int HEADER = 0;
    static final int CHILD = 1;

    private List<Item> data;
    private static final int HOUR = 3600000;

    private Context context;

    private List<String> sProgress;
    private List<String> sErgent;
    private List<String> sDeadLine;

    private List<Integer> proGress;
    private List<Integer> erGent;
    private List<Integer> deadLine;


    public ExpandableListAdapter(List<Item> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {

        View view = null;
        context = parent.getContext();

        final DBHelper dbHelper = new DBHelper(context, "Locational.db", null, 1);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        String[] sData = dbHelper.briefInfo().toArray(new String[0]);   //String [] <->ArrayList 변환

        sProgress = new ArrayList<>();
        sErgent = new ArrayList<>();
        sDeadLine = new ArrayList<>();

        proGress = new ArrayList<>();
        erGent = new ArrayList<>();
        deadLine = new ArrayList<>();

        for (int i = 0; i < sData.length; i++) {

            if (dbHelper.dateInfo().get(i) - calendar.getTimeInMillis() > HOUR) {
                sProgress.add(sData[i]);        //해당하는 구체적인 데이터 저장.
                proGress.add(i);                //인덱스값 저장
            } else if (dbHelper.dateInfo().get(i) - calendar.getTimeInMillis() < HOUR && dbHelper.dateInfo().get(i) - calendar.getTimeInMillis() > 0) {
                sErgent.add(sData[i]);
                erGent.add(i);
            } else {
                sDeadLine.add(sData[i]);
                deadLine.add(i);
            }
        }

        float dp = context.getResources().getDisplayMetrics().density;
        int subItemPaddingLeft = (int) (18 * dp);
        int subItemPaddingTopAndBottom = (int) (5 * dp);

        switch (type) {
            case HEADER:
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_header, parent, false);
                ListHeaderViewHolder header = new ListHeaderViewHolder(view);
                return header;
            case CHILD:
                TextView itemTextView = new TextView(context);
                itemTextView.setPadding(subItemPaddingLeft, subItemPaddingTopAndBottom, 0, subItemPaddingTopAndBottom);
                itemTextView.setTextColor(0xFFFFBB00);
                itemTextView.setLayoutParams(
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                return new RecyclerView.ViewHolder(itemTextView) {
                };
        }
        return null;
    }

    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Item item = data.get(position);

        switch (item.type) {
            case HEADER:
                final ListHeaderViewHolder itemController = (ListHeaderViewHolder) holder;
                itemController.refferalItem = item;
                itemController.header_title.setText(item.text);

                if (item.invisibleChildren == null) {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.circle_minus);
                } else {
                    itemController.btn_expand_toggle.setImageResource(R.drawable.circle_plus);
                }

                itemController.btn_expand_toggle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.invisibleChildren == null) {
                            item.invisibleChildren = new ArrayList<Item>();
                            int count = 0;
                            int pos = data.indexOf(itemController.refferalItem);
                            while (data.size() > pos + 1 && data.get(pos + 1).type == CHILD) {
                                item.invisibleChildren.add(data.remove(pos + 1));
                                count++;
                            }
                            notifyItemRangeRemoved(pos + 1, count);
                            itemController.btn_expand_toggle.setImageResource(R.drawable.circle_plus);
                        } else {
                            int pos = data.indexOf(itemController.refferalItem);
                            int index = pos + 1;
                            for (Item i : item.invisibleChildren) {
                                data.add(index, i);
                                index++;
                            }
                            notifyItemRangeInserted(pos + 1, index - pos - 1);
                            itemController.btn_expand_toggle.setImageResource(R.drawable.circle_minus);
                            item.invisibleChildren = null;
                        }
                    }
                });
                break;
            case CHILD:
                final TextView itemTextView = (TextView) holder.itemView;
                itemTextView.setText(data.get(position).text);


                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final View view = v;
                        Context context = v.getContext();
                        final AlertDialog.Builder dlg = new AlertDialog.Builder(context,R.style.MyPopup);
                        final DBHelper dbHelper = new DBHelper(context, "Locational.db", null, 1);

                        final int id = Integer.parseInt(dbHelper.getString().split(":")[0].trim());

                        final String[] GData = dbHelper.detailInfo().toArray(new String[0]);   //String [] <->ArrayList 변환
                        dlg.setTitle("상세 정보");


                        for (int i = 0; i < proGress.size(); i++) {
                            if (position == i + 1) {
                                dlg.setMessage("\n"+GData[proGress.get(i)] + "\n");
                            }
                        }
                        for (int i = 0; i < erGent.size(); i++) {
                            if (position == proGress.size() + 2 + i) {
                                dlg.setMessage("\n"+GData[erGent.get(i)] + "\n");
                            }
                        }
                        for (int i = 0; i < deadLine.size(); i++) {
                            if (position == proGress.size() + erGent.size() + 3 + i) {
                                dlg.setMessage("\n"+GData[deadLine.get(i)] + "\n");
                            }
                        }

                        dlg.setPositiveButton("확인", null);
                        dlg.setNegativeButton("삭제하기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                for (int i = 0; i < proGress.size(); i++) {
                                    if (position == i + 1) {
                                        int A = Integer.parseInt(GData[proGress.get(i)].split(". ")[0]);
                                        alarmDelete(A,dbHelper);
                                        dbHelper.delete(A);
                                    }
                                }

                                for (int i = 0; i < erGent.size(); i++) {
                                    if (position == proGress.size() + 2 + i) {
                                        int B = Integer.parseInt(GData[erGent.get(i)].split(". ")[0]);
                                        alarmDelete(B,dbHelper);
                                        dbHelper.delete(B);
                                    }
                                }

                                for (int i = 0; i < deadLine.size(); i++) {
                                    if (position == proGress.size() + erGent.size() + 3 + i) {
                                        int C = Integer.parseInt(GData[deadLine.get(i)].split(". ")[0]);
                                        alarmDelete(C,dbHelper);
                                        dbHelper.delete(C);
                                    }
                                }

                                Snackbar.make(view, "등록한 일정을 삭제했습니다.", Snackbar.LENGTH_LONG).show();

                                //포지션을 지정해 줘야 삭제가 제대로 이루어진다.

                                data.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, data.size());
                            }
                        });
                        AlertDialog alertDialog = dlg.create();
                        alertDialog.show();
                        alertDialog.getWindow().setLayout(650,700);

                        return false;
                    }
                });
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).type;
    }


    @Override
    public int getItemCount() {
        return data.size();
    }

    private static class ListHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header_title;
        ImageView btn_expand_toggle;
        Item refferalItem;

        ListHeaderViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
        }
    }

    public static class Item {
        int type;
        String text;
        List<Item> invisibleChildren;

        public Item() {

        }

        Item(int type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    private void alarmDelete(int A, DBHelper dbHelper) {


        int count = 0;

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent[] sender = new PendingIntent[dbHelper.dateInfo().size()];

        for (int i = 1; i <= dbHelper.dateInfo().size(); i++) {
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            int pars = Integer.parseInt(dbHelper.detailInfo().get(i - 1).split(". ")[0]);
            sender[count] = PendingIntent.getBroadcast(context, pars, alarmIntent, 0);
            if (pars == A ) {
                if (dbHelper.dateInfo().size() > 0) {
                    assert alarmManager != null;
                    alarmManager.cancel(sender[count]);
                }
                break;
            }
            count++;
        }
    }
}
