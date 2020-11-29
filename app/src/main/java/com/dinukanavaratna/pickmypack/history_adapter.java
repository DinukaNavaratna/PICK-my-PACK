package com.dinukanavaratna.pickmypack;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class history_adapter extends RecyclerView.Adapter<history_adapter.ProductViewHolder>  { //implements Filterable

    private Context mCtx;
    private List<shopping_groups_lists> groupList;
    private String filter;

    public history_adapter(Context mCtx, List<shopping_groups_lists> groupList, String filter) {
        this.mCtx = mCtx;
        this.groupList = groupList;
        this.filter = filter;
    }

    @Override
    public history_adapter.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.history_item, null);
        return new history_adapter.ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final history_adapter.ProductViewHolder holder, final int position) {
        final shopping_groups_lists group = groupList.get(position);

        holder.topic.setText(group.getTopic());
        holder.date.setText(group.getDate());
        holder.description.setText(group.getDescription());

        if(group.getStatus().equals("Pending")){
            holder.status.setImageResource(R.drawable.reminders_not_completed_dot);
        } else {
            holder.status.setImageResource(R.drawable.reminders_completed_dot);
        }

        Double total;
        Context context = appBase.getAppContext();
        SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
        Cursor resultSet = database.rawQuery("Select payment from shopping_pins WHERE group_id=" + group.getId() + " AND status='Completed' ORDER BY id ASC;", null);
        resultSet.moveToFirst();
        total=0.0;
        if (resultSet.getCount() > 0) {
            do {
                total = total + resultSet.getDouble(0);
            } while (resultSet.moveToNext());
        }
        holder.total.setText(Double.toString(total));

        holder.label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView topic, description, date, total;
        ImageView status;
        LinearLayout label;
        private LinearLayout.LayoutParams params;
        private LinearLayout rootView;

        public ProductViewHolder(View itemView) {
            super(itemView);

            topic = itemView.findViewById(R.id.group_topic_txt);
            description = itemView.findViewById(R.id.group_description_txt);
            date = itemView.findViewById(R.id.group_date_txt);
            total = itemView.findViewById(R.id.group_total);
            status = itemView.findViewById(R.id.group_activeDot);
            label = itemView.findViewById(R.id.viewLinearLayout);

            params = new LinearLayout.LayoutParams(0, 0);
            rootView = itemView.findViewById(R.id.viewLinearLayout);
        }
    }


}