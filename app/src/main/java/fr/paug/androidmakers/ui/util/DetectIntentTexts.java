package fr.paug.androidmakers.ui.util;

// [START dialogflow_import_libraries]
// Imports the Google Cloud client library
import android.content.Context;
import android.content.res.AssetManager;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.cloud.dialogflow.v2.TextInput.Builder;
import com.google.common.collect.Lists;

import java.io.FileInputStream;
import java.io.InputStream;
// [END dialogflow_import_libraries]


/**
 * DialogFlow API Detect Intent sample with text inputs.
 */
public class DetectIntentTexts {

    // [START dialogflow_detect_intent_text]
    /**
     * Returns the result of detect intent with texts as inputs.
     *
     * Using the same `session_id` between requests allows continuation of the conversation.
     * @param projectId Project/Agent Id.
     * @param text The text intents to be detected based on what a user says.
     * @param sessionId Identifier of the DetectIntent session.
     * @param languageCode Language code of the query.
     */
    public static void detectIntentTexts(String projectId, String text, String sessionId,
                                         String languageCode, Context context) throws Exception {
        AssetManager am = context.getAssets();
        InputStream is = am.open("makerdroid-dd6ee29eccd4.json");

        GoogleCredentials credentials = GoogleCredentials.fromStream(is)
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));

        SessionsSettings sessionsSettings =
                SessionsSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        // Instantiates a client
        try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
            // Set the session name using the sessionId (UUID) and projectID (my-project-id)
            SessionName session = SessionName.of(projectId, sessionId);
            System.out.println("Session Path: " + session.toString());

            // Detect intents for the text input
            // Set the text (hello) and language code (en-US) for the query
            Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

            // Build the query with the TextInput
            QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

            // Performs the detect intent request
            DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

            // Display the query result
            QueryResult queryResult = response.getQueryResult();

            System.out.println("====================");
            System.out.format("Query Text: '%s'\n", queryResult.getQueryText());
            System.out.format("Detected Intent: %s (confidence: %f)\n",
                    queryResult.getIntent().getDisplayName(), queryResult.getIntentDetectionConfidence());
            System.out.format("Fulfillment Text: '%s'\n", queryResult.getFulfillmentText());
        }
    }
    // [END dialogflow_detect_intent_text]
}