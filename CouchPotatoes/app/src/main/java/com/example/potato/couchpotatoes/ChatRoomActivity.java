package com.example.potato.couchpotatoes;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.wdullaer.swipeactionadapter.SwipeActionAdapter;
import com.wdullaer.swipeactionadapter.SwipeDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatRoomActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private DBHelper dbHelper = DBHelper.getInstance();
    private ArrayList<String> listItems = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private SwipeActionAdapter mAdapter;
    private ListView listView;
    private Map<String,String> chats = new HashMap<>();
    private String userID = dbHelper.getAuth().getUid();
    private String displayName = dbHelper.getAuthUserDisplayName();
    private String userNames = ""; // The list of users in a chat exl. displayName

    // For the side navigation bar
    private DrawerLayout mDrawer;
    private NavigationView navView;
    private android.widget.TextView sidebarUserName;
    private android.widget.TextView sidebarUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // places toolbar into the screen
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);

        // set up the side navigation bar on the left side of screen
        mDrawer = (DrawerLayout) findViewById(R.id.chatroom_drawer_layout);
        navView = (NavigationView) findViewById(R.id.chatroom_nav_view);
        setSideBarDrawer( mDrawer, navView, toolbar , dbHelper);

        // Use a ListView to display the list of chats
        listView = findViewById(R.id.chatList);
        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listItems);

        // Wrap your content in a SwipeActionAdapter
        mAdapter = new SwipeActionAdapter(listAdapter);
        // Pass a reference of your ListView to the SwipeActionAdapter
        mAdapter.setListView(listView);
        // Set the SwipeActionAdapter as the Adapter for your ListView
        listView.setAdapter(mAdapter);

        mAdapter.addBackground(SwipeDirection.DIRECTION_FAR_LEFT,R.layout.row_bg_left_far)
                .addBackground(SwipeDirection.DIRECTION_NORMAL_LEFT,R.layout.row_bg_left);

        // Listen to swipes
        mAdapter.setSwipeActionListener(new SwipeActionAdapter.SwipeActionListener(){
            @Override
            public boolean hasActions(int position, SwipeDirection direction){
                return (direction.isLeft());
            }

            @Override
            public boolean shouldDismiss(int position, SwipeDirection direction){
                // Only dismiss an item when swiping normal left
                return false;
            }


            //Listener for swipe direction
            @Override
            public void onSwipe(int[] positionList, SwipeDirection[] directionList){
                for(int i=0;i<positionList.length;i++) {
                    SwipeDirection direction = directionList[i];
                    AlertDialog.Builder builder;
                    int position = positionList[i];
                    final String chatID = chats.get( String.valueOf( listView.getItemAtPosition( position )));

                    switch (direction) {
                        case DIRECTION_FAR_LEFT:
                            builder = new AlertDialog.Builder(ChatRoomActivity.this);
                            builder.setTitle("Delete Conversation")
                                    .setMessage("This will permanently delete the conversation history.")
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dbHelper.removeFromChatUser(chatID, userID);
                                            dbHelper.removeFromUserChat(userID, chatID);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).create().show();
                            break;

                        case DIRECTION_NORMAL_LEFT:
                            builder = new AlertDialog.Builder(ChatRoomActivity.this);
                            builder.setTitle("Delete Conversation")
                                    .setMessage("This will permanently delete the conversation history.")
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dbHelper.removeFromChatUser(chatID, userID);
                                            dbHelper.removeFromUserChat(userID, chatID);
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    }).create().show();
                            break;
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        // Fetch and display all chats the current user belongs to.
        // The names of all chat members that belong to a chat are displayed and identify each chat.
        dbHelper.getDb().getReference( dbHelper.getUserChatPath() + userID ).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> elems = dataSnapshot.getChildren().iterator();

                // Make sure not to display already existing chatIDs more than once
                listItems.clear();

                // Get the next chat
                while ( elems.hasNext() ) {
                    String chatID = elems.next().getKey();

                    // Fetch the names of all users that belong to the selected chat
                    dbHelper.getDb().getReference( dbHelper.getChatUserPath() + chatID ).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterator<DataSnapshot> users = dataSnapshot.getChildren().iterator();

                            userNames = "";

                            // Concatenate the names of all users that belong to the current chat, delimited by a comma.
                            while ( users.hasNext() ) {
                                String currUser = (String) users.next().getValue();
                                // Do not display own name if other users exist in chat
                                if (currUser.equals(displayName))
                                    continue;
                                if ( !userNames.equals( "" ) ) {
                                    userNames += ", ";
                                }
                                userNames += currUser;

                            }

                            // Display app name if only the current user is present in the chat
                                // NOTE: If another a user removes themselves from a chat with a different user,
                                //         the other user's chat will now display the app name. This may result
                            //             in duplicate chats with the same name.
                            if ( userNames.equals( "" ) ) {
                                    userNames += "Couch Potatoes";
                            }

                            // Keep track of the chatID corresponding to the list of user names
                            chats.put( userNames, dataSnapshot.getKey() );

                            // Add the list of user names identifying the current chat to the ListView
                            listItems.add( userNames );

                            // Notify ListAdapter of changes
                            listAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        // Add an event handler to begin the MessageActivity corresponding to the clicked chatID
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                String chatID = chats.get( String.valueOf( parent.getItemAtPosition( position ) ) );

                // Create new Intent, keeping track of the selected chatID
                Intent intent = new Intent( getApplicationContext(), MessageActivity.class );
                intent.putExtra( "chatID", chatID );
                intent.putExtra("otherUsers", String.valueOf(parent.getItemAtPosition(position)));
                intent.putExtra( "message", "1" );

                // Begin the messaging activity corresponding to the selected chat
                startActivity( intent );
            }
        });
    }

    // Make sure the navView highlight the correct location
    @Override
    public void onResume() {
        super.onResume();
        // highlight the current location
        navView.setCheckedItem(R.id.nav_chats);
    }

    // Handles pressing back button in bottom navigation bar when sidebar is on the screen
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.chatroom_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /*
     * The method sets up the navigation drawer (a.k.a. the sidebar) on the
     * left side of the screen.
     */
    private void setSideBarDrawer( DrawerLayout mDrawer, NavigationView navView,
                                   Toolbar toolbar, DBHelper helper) {
        // enables toggle button on toolbar to open the sidebar
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        // set up side navigation bar layout
        navView.setNavigationItemSelectedListener(this);

        // Want to display icons in original color scheme
        navView.setItemIconTintList(null);

        // highlight the current location
        navView.setCheckedItem(R.id.nav_matches);

        // sets up TextViews in sidebar to display the user's name and email
        sidebarUserName = (android.widget.TextView) navView.getHeaderView(0)
                .findViewById(R.id.sidebar_username);
        sidebarUserEmail = (android.widget.TextView) navView.getHeaderView(0)
                .findViewById(R.id.sidebar_user_email);
        setSideBarText( sidebarUserName, sidebarUserEmail, helper );
    }

    /*
     * This method sets the text of the TextViews in the sidebar to display the
     * user's name and email.
     */
    private void setSideBarText( TextView nameView, TextView emailView, DBHelper helper ) {
        // fetches user's name and email
        String displayName = helper.getAuthUserDisplayName();
        String displayEmail = helper.getUser().getEmail();

        nameView.setText( displayName );
        emailView.setText( displayEmail );
    }

    // Handles action in the sidebar menu
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent( getApplicationContext(), ProfileActivity.class );
            startActivity( intent );
            finish();

        } else if (id == R.id.nav_matches) {
            finish();

        } else if (id == R.id.nav_chats) {
            // user is already at the Chats page; do nothing

        } else if (id == R.id.nav_settings) {
            startActivity( new Intent( getApplicationContext(), AppSettingsActivity.class ) );
        }
        else if (id == R.id.nav_info) {
            Intent intent = new Intent( getApplicationContext(), AboutUsActivity.class );
            startActivity( intent );


        } else if (id == R.id.nav_logout) {
            // logs out and redirects user to LoginActivity.xml
            dbHelper.getAuth().signOut();
            startActivity( new Intent( getApplicationContext(), LoginActivity.class ) );
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.chatroom_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {}
}