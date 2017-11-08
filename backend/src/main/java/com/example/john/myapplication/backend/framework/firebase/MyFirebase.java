package com.example.john.myapplication.backend.framework.firebase;

import com.example.john.myapplication.backend.MyConstants;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.logging.Logger;

import javax.servlet.ServletContext;

/**
 * Created by john on 04/11/17.
 */

public class MyFirebase {
    static Logger Log = Logger.getLogger("com.example.john.myapplication.backend.framework.firebase.MyFirebase");

    private static void wakeup(ServletContext servletContext) {
        // Note: Ensure that the [PRIVATE_KEY_FILENAME].json has read
        // permissions set.
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(servletContext.getResourceAsStream("/WEB-INF/" + MyConstants.PRIVATE_KEY_FILE))
                .setDatabaseUrl("https://" + MyConstants.FIREBASE_PROJECT_ID + ".firebaseio.com/")
                .build();

        try {
            FirebaseApp.getInstance();
        } catch (Exception error) {
            Log.info("doesn't exist...");
        }

        try {
            FirebaseApp.initializeApp(options);
        } catch (Exception error) {
            Log.info("already exists...");
        }
    }


    public static DatabaseReference getDatabaseReference(ServletContext servletContext, String path) {

        // JB - Makes sure Firebase is ready.
        wakeup(servletContext);

        // As an admin, the app has access to read and write all data, regardless of Security Rules
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference(path);

        return ref;
    }
}
