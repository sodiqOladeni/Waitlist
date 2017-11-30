package com.example.android.waitlist;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import com.example.android.waitlist.data.WaitlistContract.WaitlistEntry;
import com.example.android.waitlist.data.WaitlistDbHelper;

public class  MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    private GuestListAdapter mAdapter;
    private SQLiteDatabase mDb;

    private EditText mNewGuestNameEditText;
    private EditText mNewPartySizeEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView waitlistRecyclerView;

        // Set local attributes to corresponding views
        waitlistRecyclerView = this.findViewById(R.id.all_guests_list_view);

        mNewGuestNameEditText = findViewById(R.id.person_name_edit_text);
        mNewPartySizeEditText = findViewById(R.id.party_count_edit_text);

        // Set layout for the RecyclerView, because it's a list we are using the linear layout
        waitlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        WaitlistDbHelper mDbHelper = new WaitlistDbHelper(this);
        mDb = mDbHelper.getWritableDatabase();
        Cursor cursor = getAllGuest();
        mAdapter = new GuestListAdapter(this, cursor);

        // Link the adapter to the RecyclerView
        waitlistRecyclerView.setAdapter(mAdapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {

                long id = (long) viewHolder.itemView.getId();

                removeGuest(id);

                mAdapter.swapCursor(getAllGuest());

            }
        }).attachToRecyclerView(waitlistRecyclerView);
    }


    /**
     * This method is called when user clicks on the Add to waitlist button
     *
     * @param view The calling view (button)
     */
    public void addToWaitlist(View view) {

        if (mNewGuestNameEditText.getText().length() == 0 ||
                mNewPartySizeEditText.getText().length() == 0){
            return;
        }
        int partySize = 1;

        try {
            partySize = Integer.parseInt(mNewPartySizeEditText.getText().toString());
        }catch (Exception ex){
            Log.e(LOG_TAG, "Failed to parse party size text to number: " + ex.getMessage());
        }
        addNewGuest(mNewGuestNameEditText.getText().toString(), partySize);

        // Update the cursor in the adapter to trigger UI to display the new list
        mAdapter.swapCursor(getAllGuest());

        //clear UI text fields
        mNewPartySizeEditText.clearFocus();
        mNewGuestNameEditText.getText().clear();
        mNewPartySizeEditText.getText().clear();
    }

    private Cursor getAllGuest(){
        return mDb.query(
                WaitlistEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WaitlistEntry.COLUMN_TIMESTAMP
        );
    }

    private long addNewGuest(String name, int partySize){
        ContentValues cv = new ContentValues();
        cv.put(WaitlistEntry.COLUMN_GUEST_NAME, name);
        cv.put(WaitlistEntry.COLUMN_PARTY_SIZE, partySize);

        return mDb.insert(WaitlistEntry.TABLE_NAME, null, cv);
    }

    private boolean removeGuest(long id){
        return  mDb.delete(WaitlistEntry.TABLE_NAME,
                WaitlistEntry._ID + "=" + id, null) > 0;
    }
}
