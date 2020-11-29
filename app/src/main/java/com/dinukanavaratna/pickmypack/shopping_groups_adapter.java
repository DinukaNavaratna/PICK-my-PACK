package com.dinukanavaratna.pickmypack;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ligl.android.widget.iosdialog.IOSDialog;
import com.ligl.android.widget.iosdialog.IOSSheetDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import static android.content.Context.MODE_PRIVATE;

public class shopping_groups_adapter extends RecyclerView.Adapter<shopping_groups_adapter.ProductViewHolder>  { //implements Filterable

    private Context mCtx;
    private List<shopping_groups_lists> groupList;
    private String filter;

    public shopping_groups_adapter(Context mCtx, List<shopping_groups_lists> groupList, String filter) {
        this.mCtx = mCtx;
        this.groupList = groupList;
        this.filter = filter;
    }

    @Override
    public shopping_groups_adapter.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list, null);
        return new shopping_groups_adapter.ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final shopping_groups_adapter.ProductViewHolder holder, final int position) {
        final shopping_groups_lists group = groupList.get(position);

        holder.topic.setText(group.getTopic());
        holder.date.setText(group.getDate());
        holder.description.setText(group.getDescription());

        if(group.getStatus().equals("Pending")){
            holder.status.setImageResource(R.drawable.reminders_not_completed_dot);
        } else {
            holder.status.setImageResource(R.drawable.reminders_completed_dot);
        }

        holder.label.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                IOSSheetDialog.SheetItem[] items = new IOSSheetDialog.SheetItem[3];
                items[0] = new IOSSheetDialog.SheetItem("Complete/Pending", IOSSheetDialog.SheetItem.BLUE);
                items[1] = new IOSSheetDialog.SheetItem("Edit", IOSSheetDialog.SheetItem.BLUE);
                items[2] = new IOSSheetDialog.SheetItem("Share", IOSSheetDialog.SheetItem.BLUE);
                IOSSheetDialog dialog2 = new IOSSheetDialog.Builder(mCtx).setData(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int optionNumber) {
                        if (optionNumber == 0) {
                            if(group.getStatus().equals("Pending")) {
                                String qry = "UPDATE shopping_group SET status='Completed' WHERE id=" + group.getId() + ";";
                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                insertData data = new insertData(nameValuePairs);
                                String qry1 = "UPDATE shopping_pins SET status='Completed' WHERE group_id=" + group.getId() + ";";
                                List<NameValuePair> nameValuePairs1 = new ArrayList<NameValuePair>();
                                nameValuePairs1.add(new BasicNameValuePair("querytoDB", qry1));
                                insertData data1 = new insertData(nameValuePairs1);
                            } else {
                                String qry = "UPDATE shopping_group SET status='Pending' WHERE id=" + group.getId() + ";";
                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                insertData data = new insertData(nameValuePairs);
                            }
                            if (mCtx instanceof shopping_groups) {
                                ((shopping_groups) mCtx).refreshPage();
                            }
                        }
                        if (optionNumber == 1) {
                            final shopping_groups sp = new shopping_groups();
                            List<String> productList = new ArrayList<>();

                            IOSDialog.Builder builder = new IOSDialog.Builder(mCtx);
                            builder.setTitle("Edit");

                            final LinearLayout layout = new LinearLayout(mCtx);
                            layout.setOrientation(LinearLayout.VERTICAL);

                            final AutoCompleteTextView topic = new AutoCompleteTextView(mCtx);
                            topic.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            topic.setCursorVisible(true);
                            topic.setText(group.getTopic());
                            topic.setHint("Topic");
                            ArrayAdapter<String> productsAdapter = new ArrayAdapter<String>(mCtx, android.R.layout.simple_dropdown_item_1line, productList);
                            productsAdapter.setNotifyOnChange(true);
                            topic.setThreshold(1);
                            topic.setAdapter(productsAdapter);
                            layout.addView(topic);

                            final EditText description = new EditText(mCtx);
                            description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            description.setCursorVisible(true);
                            description.setText(group.getDescription());
                            description.setHint("Description");
                            layout.addView(description);

                            builder.setContentView(layout);
                            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String qry = "UPDATE shopping_group SET topic='"+topic.getText().toString()+"', description='"+description.getText().toString()+"' WHERE id="+group.getId()+";";
                                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                    nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                    insertData data = new insertData(nameValuePairs);

                                    Intent myIntent = new Intent(mCtx, shopping_groups.class);
                                    mCtx.startActivity(myIntent);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                        }
                        if (optionNumber == 2) {
                            String waypoints = "";
                            String list = "";
                            int x = 1;
                            Context context = appBase.getAppContext();
                            SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
                            Cursor resultSet1 = database.rawQuery("Select lat, lng, description, status, payment, id from shopping_pins WHERE group_id=" + group.getId() + " ORDER BY id ASC;", null);
                            resultSet1.moveToFirst();
                            if (resultSet1.getCount() > 0) {
                                do {
                                    if(waypoints.equals("")) {
                                        waypoints += "destination="+resultSet1.getDouble(0) + "," + resultSet1.getDouble(1)+"&waypoints=";
                                    } else {
                                        if(x == 1) {
                                            waypoints += resultSet1.getDouble(0) + "," + resultSet1.getDouble(1);
                                            x = 2;
                                        } else {
                                            waypoints += "%7C" + resultSet1.getDouble(0) + "," + resultSet1.getDouble(1);
                                        }
                                    }
                                    list += "\n$ ID: "+resultSet1.getString(5)+"\n$ "+resultSet1.getString(3)+"\n$ "+resultSet1.getString(4)+"/=\n$ "+resultSet1.getString(2)+"\nhttps://www.google.com/maps/dir/?api=1&destination="+resultSet1.getDouble(0) + "," + resultSet1.getDouble(1)+"\n";
                                } while (resultSet1.moveToNext());
                            }

                            Intent myIntent = new Intent(Intent.ACTION_SEND);
                            myIntent.setType("text/plain");
                            String shareBody = "- PICK my PACK -\n\n\n#"+group.getTopic()+"\n"+group.getDescription()+"\n\n\n#Directions\nhttps://www.google.com/maps/dir/?api=1&"+waypoints+"&travelmode=driving\n\n\n#Details ->\n"+list;
                            myIntent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
                            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                            mCtx.startActivity(Intent.createChooser(myIntent, "Share Using..."));
                        }
                    }
                }).show();
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView topic, description, date;
        ImageView status;
        LinearLayout label;
        private LinearLayout.LayoutParams params;
        private LinearLayout rootView;

        public ProductViewHolder(View itemView) {
            super(itemView);

            topic = itemView.findViewById(R.id.group_topic_txt);
            description = itemView.findViewById(R.id.group_description_txt);
            date = itemView.findViewById(R.id.group_date_txt);
            status = itemView.findViewById(R.id.group_activeDot);
            label = itemView.findViewById(R.id.viewLinearLayout);

            params = new LinearLayout.LayoutParams(0, 0);
            rootView = itemView.findViewById(R.id.viewLinearLayout);
        }
    }


}