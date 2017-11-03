/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Servlet Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloWorld
*/

package com.example.john.myapplication.backend;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.*;

public class MyServlet extends HttpServlet {
    static Logger Log = Logger.getLogger("com.example.john.myapplication.backend.MyServlet");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        Log.info("Sending the todo list email.");

        String outString;
        outString = "<p>Sending the todo list email.</p><p><strong>Note:</strong> ";
        outString = outString.concat("the servlet must be deployed to App Engine in order to ");
        outString = outString.concat("send the email. Running the server locally writes a message ");
        outString = outString.concat("to the log file instead of sending an email message.</p>");

        resp.getWriter().println(outString);

        // Note: Ensure that the [PRIVATE_KEY_FILENAME].json has read
        // permissions set.
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setServiceAccount(getServletContext().getResourceAsStream("/WEB-INF/" + MyConstants.PRIVATE_KEY_FILE))
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

        // As an admin, the app has access to read and write all data, regardless of Security Rules
        DatabaseReference ref = FirebaseDatabase
                .getInstance()
                .getReference("todoItems");

        // This fires when the servlet first runs, returning all the existing values
        // only runs once, until the servlet starts up again.
        ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object document = dataSnapshot.getValue();
                Log.info("new value: " + document);

                String todoText = "Don't forget to...\n\n";

                Iterator<DataSnapshot> children = dataSnapshot.getChildren().iterator();

                while (children.hasNext()) {
                    DataSnapshot childSnapshot = (DataSnapshot) children.next();
                    todoText = todoText + " * " + childSnapshot.getValue().toString() + "\n";
                }

                // Now send the email

                // Note: When an application running in the development server calls the Mail
                // service to send an email message, the message is printed to the log.
                // The Java development server does not send the email message.

                // You can test the email without waiting for the cron job to run by
                // loading http://[FIREBASE_PROJECT_ID].appspot.com/send-email in your browser.

                Properties props = new Properties();
                Session session = Session.getDefaultInstance(props, null);
                try {
                    Message msg = new MimeMessage(session);
                    //Make sure you substitute your project-id in the email From field
                    msg.setFrom(new InternetAddress("reminder@" + MyConstants.FIREBASE_PROJECT_ID + ".appspotmail.com",
                            "Todo Reminder"));
                    msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress(MyConstants.MY_EMAIL, "Recipient"));
                    msg.setSubject("Things to do today");
                    msg.setText(todoText);
                    Transport.send(msg);
                } catch (MessagingException | UnsupportedEncodingException e) {
                    Log.warning(e.getMessage());
                }

                // Note: in a production application you should replace the hard-coded email address
                // above with code that populates msg.addRecipient with the app user's email address.
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error: " + error);
            }
        });
    }
}