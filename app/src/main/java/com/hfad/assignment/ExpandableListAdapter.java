package com.hfad.assignment;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


public class ExpandableListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final int HEADER = 0;
    public static final int CHILD = 1;

    private List<Item> data;

    public String header_name;
    public int rowIndex;

    public ExpandableListAdapter(List<Item> data) {
        this.data = data;
    }

    BorderDrawable borderDrawable = new BorderDrawable();
    private SQLiteDatabase fd_db;
    TimeZone nzTimeZone = TimeZone.getTimeZone("Pacific/Auckland");
    Calendar nzCalendar = Calendar.getInstance(nzTimeZone);



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View view = null;
        Context context = parent.getContext();
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
                LayoutInflater inflater2 = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater2.inflate(R.layout.list_table, parent, false);
                FoodTableViewHolder table_row = new FoodTableViewHolder(view);

                return table_row;
        }
        return null;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Item item = data.get(position);
        switch (item.type) {
            case HEADER:
                final ListHeaderViewHolder itemController = (ListHeaderViewHolder) holder;
                itemController.refferalItem = item;
                itemController.header_title.setText(item.text);
                header_name = item.text;
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
                borderDrawable.setColor(Color.BLACK); // 테두리 색상 설정
                borderDrawable.setCornersRadius(5); // 테두리 모서리 곡률 설정

                final FoodTableViewHolder tableController = (FoodTableViewHolder) holder;
                tableController.mTableLayout.removeAllViews();

                TableRow row = new TableRow(tableController.mTableLayout.getContext());

                TextView textView = new TextView(row.getContext());
                textView.setText(item.text);
                textView.setGravity(Gravity.CENTER);
                textView.setLayoutParams(new TableRow.LayoutParams(550, TableRow.LayoutParams.MATCH_PARENT));

                TextView textView2 = new TextView(row.getContext());
                textView2.setText(item.tmp1);
                textView2.setGravity(Gravity.CENTER);
                textView2.setLayoutParams(new TableRow.LayoutParams(230, TableRow.LayoutParams.WRAP_CONTENT));
//                textView2.setHeight(measuredHeight);

                TextView textView3 = new TextView(row.getContext());
                textView3.setText(item.tmp2);
                textView3.setGravity(Gravity.CENTER);
                textView3.setLayoutParams(new TableRow.LayoutParams(230, TableRow.LayoutParams.WRAP_CONTENT));
//                textView3.setHeight(measuredHeight);

                ImageButton del_btn = new ImageButton(row.getContext());
                if(item.text.equals("Food Name")){
                    del_btn.setEnabled(false);
                    textView.setTextSize(23); textView.setTextColor(Color.BLACK); textView.setBackgroundResource(R.drawable.table_outsideline);
                    textView2.setTextSize(23); textView2.setTextColor(Color.BLACK); textView2.setBackgroundResource(R.drawable.table_outsideline);
                    textView3.setTextSize(23); textView3.setTextColor(Color.BLACK); textView3.setBackgroundResource(R.drawable.table_outsideline);
                }else{
                    del_btn.setImageResource(R.drawable.delete);
                    textView.setTextSize(22);
                    textView2.setTextSize(22);
                    textView3.setTextSize(22);
                }

//                del_btn.setMaxWidth(1);
                del_btn.setBackgroundColor(0xffffffff);
                del_btn.setLayoutParams(new TableRow.LayoutParams(60, 60));
                del_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 클릭된 버튼의 부모 TableRow 찾기
                        TableRow row = (TableRow) view.getParent();

                        // TableRow의 첫 번째 셀(TextView) 찾기
                        TextView firstCell = (TextView) row.getChildAt(0);

                        // 첫 번째 셀의 텍스트 값 가져오기
                        String text = firstCell.getText().toString();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        String currentDate = sdf.format(nzCalendar.getTime());

                        System.out.println(currentDate);
                        System.out.println("check start");
                        DBmanage dbHelper = new DBmanage(view.getContext());
                        fd_db=dbHelper.getWritableDatabase();

                        if(item.meal_t.equals("Breakfast")){
                            fd_db.execSQL("DELETE FROM morning_food WHERE date = '" + currentDate + "' AND mf_name = '"+ text +"'");
                            Toast.makeText(view.getContext(), "The Food Record Deleted", Toast.LENGTH_LONG).show();
                        }else if(item.meal_t.equals("Lunch")){
                            fd_db.execSQL("DELETE FROM lunch_food WHERE date = '" + currentDate + "' AND lf_name = '"+ text +"'");
                            Toast.makeText(view.getContext(), "The Food Record Deleted", Toast.LENGTH_LONG).show();
                        }else if(item.meal_t.equals("Dinner")){
                            fd_db.execSQL("DELETE FROM dinner_food WHERE date = '" + currentDate + "' AND df_name = '"+ text +"'");
                            Toast.makeText(view.getContext(), "The Food Record Deleted", Toast.LENGTH_LONG).show();
                        }

                        fd_db.close();

                        TableLayout tableLayout = (TableLayout) row.getParent();
                        rowIndex = tableLayout.indexOfChild(row);

                        tableLayout.removeView(row);


                    }
                });

                row.addView(textView);
                row.addView(textView2);
                row.addView(textView3);
                row.addView(del_btn);
                tableController.mTableLayout.addView(row);


                setCellHeights(row);

                break;
        }
    }


    private void setCellHeights(final TableRow row) {
        row.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 뷰의 레이아웃이 완전히 로드된 후 높이를 측정하고 설정
                int maxCellHeight = 0;

                for (int i = 0; i < row.getChildCount(); i++) {
                    View childView = row.getChildAt(i);

                    // 현재 TextView의 높이
                    int cellHeight = childView.getHeight();

                    if (cellHeight > maxCellHeight) {
                        maxCellHeight = cellHeight;
                    }
                }

                // 모든 셀의 높이를 동일하게 설정
                for (int i = 0; i < row.getChildCount(); i++) {
                    View cellView = row.getChildAt(i);
                    TableRow.LayoutParams params = (TableRow.LayoutParams) cellView.getLayoutParams();
                    params.height = maxCellHeight;
                    cellView.setLayoutParams(params);
                }


                // 리스너 제거 (한 번만 실행하기 위해)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    row.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    row.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });
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
        public TextView header_title;
        public ImageView btn_expand_toggle;
        public Item refferalItem;

        public ListHeaderViewHolder(View itemView) {
            super(itemView);
            header_title = (TextView) itemView.findViewById(R.id.header_title);
            btn_expand_toggle = (ImageView) itemView.findViewById(R.id.btn_expand_toggle);
        }
    }

    public class FoodTableViewHolder extends RecyclerView.ViewHolder {
        public TableLayout mTableLayout;

        public FoodTableViewHolder(View itemView) {
            super(itemView);
            mTableLayout = (TableLayout) itemView.findViewById(R.id.plan_tableLayout);
        }

    }


    public static class Item {
        public int type;
        public String text;
        public String tmp1, tmp2;
        public String meal_t;
        public List<Item> invisibleChildren;

        public Item() {
        }

        public Item(int type, String text, String tmp1, String tmp2, String meal_t) {
            this.type = type;
            this.text = text;
            this.tmp1 = tmp1;
            this.tmp2 = tmp2;
            this.meal_t = meal_t;
        }
    }


}
