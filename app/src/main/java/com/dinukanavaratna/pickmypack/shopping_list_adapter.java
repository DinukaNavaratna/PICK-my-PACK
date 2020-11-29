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
import android.widget.RelativeLayout;
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

public class shopping_list_adapter extends RecyclerView.Adapter<shopping_list_adapter.ProductViewHolder>  { //implements Filterable

    private Context mCtx;
    private List<shopping_list_items> customerList;
    private String filter;

    public shopping_list_adapter(Context mCtx, List<shopping_list_items> customerList, String filter) {
        this.mCtx = mCtx;
        this.customerList = customerList;
        this.filter = filter;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_item, null);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, final int position) {
        final shopping_list_items list = customerList.get(position);

        holder.price.setText(list.getPrice()+"/=");
        holder.description.setText(list.getDescription());

        if(list.getStatus().equals("Pending")){
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
                            if(list.getStatus().equals("Pending")) {
                                String qry = "UPDATE shopping_pins SET status='Completed' WHERE id=" + list.getId() + ";";
                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                insertData data = new insertData(nameValuePairs);
                            } else {
                                String qry = "UPDATE shopping_pins SET status='Pending' WHERE id=" + list.getId() + ";";
                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                insertData data = new insertData(nameValuePairs);
                            }
                            if (mCtx instanceof shopping_list) {
                                ((shopping_list) mCtx).refreshPage();
                            }
                        }
                        if (optionNumber == 1) {
                            final shopping_list sp = new shopping_list();
                            List<String> productList = new ArrayList<>();
                            List<Integer> productListId = new ArrayList<>();

                            Context context = appBase.getAppContext();
                            SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
                            SharedPreferences user = PreferenceManager.getDefaultSharedPreferences(mCtx.getApplicationContext());
                            String uid = user.getString("id", "");
                            Cursor resultSet1 = database.rawQuery("Select id, topic from shopping_group WHERE user_id="+uid+" ORDER BY id ASC;", null);
                            resultSet1.moveToFirst();
                            productList.clear();
                            productListId.clear();
                            if(resultSet1.getCount() > 0) {
                                productList.clear();
                                productListId.clear();
                                do {
                                    productList.add(resultSet1.getString(1));
                                    productListId.add(resultSet1.getInt(0));
                                } while (resultSet1.moveToNext());
                            }
                            String topicfromdb = "";
                            int count = 0;
                            for (Integer string : productListId) {
                                if (string == list.getGroupId()) {
                                    topicfromdb = productList.get(count);
                                    break;
                                }
                                count = count + 1;
                            }
                            IOSDialog.Builder builder = new IOSDialog.Builder(mCtx);
                            builder.setTitle("Edit");

                            final LinearLayout layout = new LinearLayout(mCtx);
                            layout.setOrientation(LinearLayout.VERTICAL);

                            final AutoCompleteTextView topic = new AutoCompleteTextView(mCtx);
                            topic.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            topic.setCursorVisible(true);
                            topic.setText(topicfromdb);
                            topic.setHint("Topic");
                            ArrayAdapter<String> productsAdapter = new ArrayAdapter<String>(mCtx, android.R.layout.simple_dropdown_item_1line, productList);
                            productsAdapter.setNotifyOnChange(true);
                            topic.setThreshold(1);
                            topic.setAdapter(productsAdapter);
                            layout.addView(topic);

                            final EditText price = new EditText(mCtx);
                            price.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            price.setCursorVisible(true);
                            price.setText(list.getPrice());
                            price.setHint("Price");
                            layout.addView(price);

                            final EditText description = new EditText(mCtx);
                            description.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                            description.setCursorVisible(true);
                            description.setText(list.getDescription());
                            description.setHint("Description");
                            layout.addView(description);

                            builder.setContentView(layout);
                            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Context context = appBase.getAppContext();
                                    SQLiteDatabase database = context.openOrCreateDatabase("LocalDB", MODE_PRIVATE, null);
                                    final Cursor resultSet = database.rawQuery("Select id from shopping_group WHERE topic='" + topic.getText().toString() + "';", null); //Select * from requests WHERE donation_count<3 ORDER BY id;
                                    if (resultSet.getCount() == 1) {
                                        resultSet.moveToFirst();
                                        String qry = "UPDATE shopping_pins SET group_id="+resultSet.getInt(0)+" , description='"+description.getText().toString()+"' , payment="+price.getText().toString()+" WHERE id="+list.getId()+";";
                                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                        nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                        insertData data = new insertData(nameValuePairs);

                                        Intent myIntent = new Intent(mCtx, shopping_list.class);
                                        mCtx.startActivity(myIntent);
                                    } else {
                                        IOSDialog.Builder builder = new IOSDialog.Builder(mCtx);
                                        builder.setTitle("Set the list - " + topic.getText().toString());
                                        // Set up the input
                                        final EditText input = new EditText(mCtx);
                                        input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                        input.setCursorVisible(true);
                                        builder.setContentView(input);
                                        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                SharedPreferences user = PreferenceManager.getDefaultSharedPreferences(mCtx.getApplicationContext());
                                                String uid = user.getString("id", "");

                                                String qry1 = "INSERT INTO shopping_group (user_id, topic, description) VALUES (" + uid + ", '" + topic.getText().toString() + "', '" + input.getText().toString() + "');";
                                                List<NameValuePair> nameValuePairs1 = new ArrayList<NameValuePair>();
                                                nameValuePairs1.add(new BasicNameValuePair("querytoDB", qry1));
                                                insertData data1 = new insertData(nameValuePairs1);

                                                String qry = "UPDATE shopping_pins SET group_id=(SELECT id FROM shopping_group ORDER BY id DESC LIMIT 1) , description='"+description.getText().toString()+"' , payment="+price.getText().toString()+" WHERE id="+list.getId()+";";
                                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                                nameValuePairs.add(new BasicNameValuePair("querytoDB", qry));
                                                insertData data = new insertData(nameValuePairs);

                                                Intent myIntent = new Intent(mCtx, shopping_list.class);
                                                mCtx.startActivity(myIntent);
                                            }
                                        });
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                        }
                        if (optionNumber == 2) {
                            Intent myIntent = new Intent(Intent.ACTION_SEND);
                            myIntent.setType("text/plain");
                            String shareBody = "- PICK my PACK -\n\n\n#ID: "+list.getId()+"\n#Price: "+list.getPrice()+"/=\n\n"+list.getDescription()+"\n\n#Directions\nhttps://www.google.com/maps/dir/?api=1&destination="+list.getLat()+","+list.getLat()+"&waypoints=6.8699688,79.8882835%7C6.8670544,79.8847353%7C6.8410598,79.9017028&travelmode=driving";
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
        return customerList.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        TextView description, price;
        ImageView status;
        LinearLayout label;
        private LinearLayout.LayoutParams params;
        private LinearLayout rootView;

        public ProductViewHolder(View itemView) {
            super(itemView);

            description = itemView.findViewById(R.id.description_txt);
            price = itemView.findViewById(R.id.price_txt);
            status = itemView.findViewById(R.id.activeDot);
            label = itemView.findViewById(R.id.viewLinearLayout);

            params = new LinearLayout.LayoutParams(0, 0);
            rootView = itemView.findViewById(R.id.viewLinearLayout);
        }
    }


}