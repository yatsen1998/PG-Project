package comp5216.sydney.edu.au.todolist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Define variables
    ListView listView;
    ArrayList<String> items;
    ArrayAdapter<String> itemsAdapter;
    EditText addItemEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use "activity_main.xml" as the layout
        setContentView(R.layout.activity_main);
        // Reference the "listView" variable to the id "lstView" in the layout
        listView = findViewById(R.id.lstView);
        addItemEditText = findViewById(R.id.txtNewItem);
        // Create an ArrayList of String
        items = new ArrayList<String>();
        items.add("item one");
        items.add("item two");
        // Create an adapter for the list view using Android's built-in item layout
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        // Connect the listView and the adapter
        listView.setAdapter(itemsAdapter);
        setupListViewListener();
    }

    public void onAddItemClick(View view) {
        String toAddString = addItemEditText.getText().toString();
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String currentTime = format1.format(now);

        Toast.makeText(this, "Added this at " + currentTime, Toast.LENGTH_SHORT).show();
        if (toAddString.length() > 0) {
            itemsAdapter.add(toAddString + "  " + currentTime); // Add text to list view adapter
            addItemEditText.setText("");
        }
    }

    private void setupListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long rowId) {
                Log.i("MainActivity", "Long Clicked Item" + position);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.dialog_delete_tile)
                        .setMessage(R.string.dialog_delete_msg)
                        .setPositiveButton(R.string.delete, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                            items.remove(position);
                                            itemsAdapter.notifyDataSetChanged();
                                    }
                                })
                        .setNegativeButton(R.string.cancel, new
                                DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                });
                builder.create().show();
                return true;
            }
        });

        ActivityResultLauncher<Intent> mLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Extract name value from result extras
                        String editedItem = result.getData().getExtras().getString("item");
                        int position = result.getData().getIntExtra("position", -1);
                        items.set(position, editedItem);
                        Log.i("Updated item in list ", editedItem + ", position: " + position);
                        // Make a standard toast that just contains text
                        Toast.makeText(getApplicationContext(), "Updated: " + editedItem, Toast.LENGTH_SHORT).show();
                    }
                    itemsAdapter.notifyDataSetChanged();
                }
        );

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                String updateItem = (String) itemsAdapter.getItem(position);
                Log.i("MainActivity", "Clicked item" + position + ": " + updateItem);

                Intent intent = new Intent(MainActivity.this, EditToDoItemActivity.class);
                if (intent != null) {
                    intent.putExtra("item", updateItem);
                    intent.putExtra("position", position);

                    mLauncher.launch(intent);
                    itemsAdapter.notifyDataSetChanged();
                }
            }
        });
    }
}