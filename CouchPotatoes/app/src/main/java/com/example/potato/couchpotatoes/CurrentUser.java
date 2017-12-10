package com.example.potato.couchpotatoes;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Admin on 11/7/17.
 */

public class CurrentUser extends User {
    private static CurrentUser uniqueInstance;
    private ArrayList<String> contactList = new ArrayList<String>();
    private LinkedList<String> matchedUsersId = new LinkedList<>();
    // may or may not need. Alternative is to populate one a linked list in HomeActivity instead.
    private LinkedList<MatchedUser> matchedUsers = new LinkedList<>();

    private CurrentUser () {}

    private CurrentUser ( String email, String uid, String firstName, String middleName, String lastName, String dob,
                  String gender, String city, String state, String country, String bio,
                  double latitude, double longitude, boolean locked, boolean suspended ) {
        super( email, uid, firstName, middleName, lastName, dob, gender, city, state, country, bio,
                latitude, longitude, locked, suspended );
    }

    public static CurrentUser getInstance( String email, String uid, String firstName, String middleName, String lastName,
                                           String dob, String gender, String city, String state, String country, String bio,
                                           double latitude, double longitude, boolean locked, boolean suspended ) {
        if ( uniqueInstance == null ) {
            uniqueInstance = new CurrentUser(
                    email, uid, firstName, middleName, lastName, dob, gender, city, state, country, bio,
                    latitude, longitude, locked, suspended
            );
        }
        return uniqueInstance;
    }

    public static CurrentUser getInstance() {
        if ( uniqueInstance == null ) {
            uniqueInstance = new CurrentUser();
        }
        return uniqueInstance;
    }

    /**
     * Parses the DataSnapshot and initializes the CurrentUser from those fields
     * @param dataSnapshot snapshot of data to initialize CurrentUser from
     */
    public void initializeFromDataSnapshot(DataSnapshot dataSnapshot) {
        for ( DataSnapshot field : dataSnapshot.getChildren() ) {
            // Log.d( "TEST", field.toString() );
            switch (field.getKey()) {
                case "email":
                    setEmail((String) field.getValue());
                    break;
                case "uid":
                    setUid((String) field.getValue());
                    break;
                case "firstName":
                    setFirstName((String) field.getValue());
                    break;
                case "middleName":
                    setFirstName((String) field.getValue());
                    break;
                case "lastName":
                    setFirstName((String) field.getValue());
                    break;
                case "dob":
                    setDob((String) field.getValue());
                    break;
                case "gender":
                    setGender((String) field.getValue());
                    break;
                case "city":
                    setFirstName((String) field.getValue());
                    break;
                case "state":
                    setFirstName((String) field.getValue());
                    break;
                case "country":
                    setFirstName((String) field.getValue());
                    break;
                case "bio":
                    setBio((String) field.getValue());
                    break;
                case "latitude":
                    long num = (long) field.getValue();
                    setLatitude(0.0);
                    if (field.getValue() != null) {
                        setLatitude((double) num);
                    }
                    break;
                case "longitude":
                    num = (long) field.getValue();
                    setLatitude(0.0);
                    if (field.getValue() != null) {
                        setLongitude((double) num);
                    }
                    break;
                case "locked":
                    setLocked((boolean) field.getValue());
                    break;
                case "suspended":
                    setSuspended((boolean) field.getValue());
                    break;
                default:
                    break;
            }
        }
    }

    public ArrayList<String> getContactList() {
        return contactList;
    }

    public void setContactList(ArrayList<String> contactList) {
        this.contactList = contactList;
    }
}
