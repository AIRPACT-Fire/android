package edu.wsu.lar.airpact_fire.data.realm.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {

    @PrimaryKey
    public String username;
    public String password;
    public RealmList<Session> sessions;  // App usage sessions by this user
    public RealmList<Post> posts;        // Queued and submitted posts
    public int distanceMetric;           // Integer in Reference.DistanceMetric enum

    @Override
    public String toString() {
        return username;
    }
}
