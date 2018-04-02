package fr.paug.androidmakers.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import java.util.*;
import java.util.stream.Collectors;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.paug.androidmakers.R;
import fr.paug.androidmakers.manager.AgendaRepository;
import fr.paug.androidmakers.model.Room;
import fr.paug.androidmakers.model.ScheduleSlot;
import fr.paug.androidmakers.model.Session;
import fr.paug.androidmakers.ui.activity.SessionDetailActivity;
import fr.paug.androidmakers.ui.adapter.ScheduleSession;

/**
 * Created by Jade on 20/03/2018
 */

public class MakerDroidFragment extends Fragment implements AIListener {


    private static final String TAG = MakerDroidFragment.class.getSimpleName();
    private static final String LANGUAGE_PARAMETER_KEY = "language";
    private static final String CUSTOM_TIME_PARAMETER_KEY = "custom-time";


    private Unbinder unbinder;

    @BindView(R.id.bot_layout)
    LinearLayout botLayout;

    @BindView(R.id.bot_button)
    ImageButton botButton;

    @BindView(R.id.bot_listening)
    ImageButton botListening;

    @BindView(R.id.bot_send)
    ImageButton botSend;

    @BindView(R.id.bot_treating)
    ProgressBar botTreatingProgressBar;

    @BindView(R.id.edit_text_ask)
    EditText editTextAsk;


    private final AIConfiguration config = new AIConfiguration("97ef1441bcd540038c4add623c6f9610",
            AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System);

    private AIService aiService;

    final AIDataService aiDataService = new AIDataService(config);

    // Requesting permission to RECORD_AUDIO
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    final long currentTime = new Date().getTime();

    private int defaultPadding;
    private int largePadding;
    private LinearLayout.LayoutParams paramsQuestion;
    private LinearLayout.LayoutParams paramsAnswer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aiService = AIService.getService(this.getContext(), config);
        aiService.setListener(this);
        // Keeps this Fragment alive during configuration changes
        setRetainInstance(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_makerdroid, container, false);
        setHasOptionsMenu(true);

        unbinder = ButterKnife.bind(this, view);

        ActivityCompat.requestPermissions(this.getActivity(), permissions, REQUEST_RECORD_AUDIO_PERMISSION);


        botButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                aiService.startListening();
            }
        });

        botListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aiService.stopListening();
            }
        });

        botSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendQuestion(editTextAsk.getText().toString());
            }
        });

        defaultPadding = (int) getResources().getDimension(R.dimen.default_padding);
        largePadding = (int) getResources().getDimension(R.dimen.large_padding);

        // layout params for the question bubble
        paramsQuestion = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsQuestion.weight = 1.0f;
        paramsQuestion.gravity = Gravity.RIGHT;
        paramsQuestion.setMargins(largePadding, 0, largePadding, largePadding);

        // layout params for the answer bubble
        paramsAnswer = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsAnswer.weight = 1.0f;
        paramsAnswer.gravity = Gravity.LEFT;
        paramsAnswer.setMargins(largePadding, 0, largePadding, largePadding);

        // click on send
        editTextAsk.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendQuestion(textView.getText().toString());
                    return true;
                }
                return false;
            }
        });

        editTextAsk.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count == 0) {
                    // change icon to mic
                    botButton.setVisibility(View.VISIBLE);
                    botListening.setVisibility(View.GONE);
                    botTreatingProgressBar.setVisibility(View.GONE);
                    botSend.setVisibility(View.GONE);
                } else {
                    // change icon to send
                    botButton.setVisibility(View.GONE);
                    botListening.setVisibility(View.GONE);
                    botTreatingProgressBar.setVisibility(View.GONE);
                    botSend.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void sendQuestion(String text) {
        if (!text.isEmpty()) {
            final AIRequest aiRequest = new AIRequest();
            aiRequest.setQuery(text);
            new AiTask().execute(aiRequest);

            hideKeyboard();

            editTextAsk.setText("");

            botButton.setVisibility(View.GONE);
            botListening.setVisibility(View.GONE);
            botTreatingProgressBar.setVisibility(View.VISIBLE);
            botTreatingProgressBar.animate();
            botSend.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        aiService.setListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onResult(AIResponse aiResponse) {
        processAiResponse(aiResponse);
    }

    @Override
    public void onError(AIError aiError) {
        try {
            Log.i(TAG, "Result Error : " + aiError.getMessage());
            botButton.setVisibility(View.VISIBLE);
            botListening.setVisibility(View.GONE);
            botTreatingProgressBar.setVisibility(View.GONE);
            Toast.makeText(this.getContext(), "An error occurred (" + aiError.getMessage() + ").\nPlease try again.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Exception " + e.getMessage());
            Toast.makeText(this.getContext(), "An error occurred.\nPlease try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard() {
        // hide virtual keyboard
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(editTextAsk.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }
    @Override
    public void onAudioLevel(float v) {

    }

    @Override
    public void onListeningStarted() {
        Log.d(TAG, "onListeningStarted");
        botButton.setVisibility(View.GONE);
        botListening.setVisibility(View.VISIBLE);
        botTreatingProgressBar.setVisibility(View.GONE);
        botSend.setVisibility(View.GONE);
    }

    @Override
    public void onListeningCanceled() {
        Log.d(TAG, "onListeningCanceled");
        botButton.setVisibility(View.VISIBLE);
        botListening.setVisibility(View.GONE);
        botTreatingProgressBar.setVisibility(View.GONE);
        botSend.setVisibility(View.GONE);
    }

    @Override
    public void onListeningFinished() {
        Log.d(TAG, "onListeningFinished");
        botButton.setVisibility(View.GONE);
        botListening.setVisibility(View.GONE);
        botSend.setVisibility(View.GONE);
        botTreatingProgressBar.setVisibility(View.VISIBLE);
        botTreatingProgressBar.animate();
    }


    private void addQuestionView(String text) {
        TextView tvQuestion = new TextView(this.getContext());
        tvQuestion.setFocusable(true);
        tvQuestion.setFocusableInTouchMode(true);
        tvQuestion.setText(text);
        tvQuestion.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_bot_question));
        tvQuestion.setLayoutParams(paramsQuestion);
        tvQuestion.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
        botLayout.addView(tvQuestion);
        tvQuestion.requestFocus();
    }

    private void addAnswerView(String text) {
        TextView tvAnswer = new TextView(this.getContext());
        tvAnswer.setFocusable(true);
        tvAnswer.setFocusableInTouchMode(true);
        tvAnswer.setText(text);
        tvAnswer.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_bot_answer));
        tvAnswer.setLayoutParams(paramsAnswer);
        tvAnswer.setPadding(defaultPadding, defaultPadding, defaultPadding, defaultPadding);
        botLayout.addView(tvAnswer);
        tvAnswer.requestFocus();
    }

    private String treatQuestion(HashMap<String, JsonElement> parameters) {
        try {
            if (parameters.get(LANGUAGE_PARAMETER_KEY) != null) {
                String userLang = parameters.get(LANGUAGE_PARAMETER_KEY).getAsString();
                String sessionTime = parameters.get(CUSTOM_TIME_PARAMETER_KEY).getAsString();

                if (getResources().getString(R.string.bot_current_questions).equalsIgnoreCase(sessionTime)){
                    return  treatCurrentSessionQuestions(sessionTime,userLang);

                }else if ((getResources().getString(R.string.bot_next_questions).equalsIgnoreCase(sessionTime))){
                    return  treatNextSessionQuestions(userLang);
                }

                String returnedMsg = "I can filter by sessions for now";
                Log.d(TAG, returnedMsg);
                return returnedMsg;
            }

            return "I can only filter by language for now";

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return "An error occurred, please try again.";
        }
    }


    private String treatCurrentSessionQuestions(String sessionTime, String userlang){

        List<ScheduleSlot> resultSessions = new ArrayList<>();

        //currentTime : 1524470700000l for test session (23/04 à 9h:05)!!

        List<ScheduleSlot> slots = filtredSlotByLanguage(userlang);

        resultSessions = slots.stream()
                .peek(num -> System.out.println("will filter " + num))
                .filter(x -> x.startDate <= currentTime
                            && x.endDate > currentTime )
                .collect(Collectors.toList());

        //
        /* before Java 8*/
         /*for (ScheduleSlot slot : AgendaRepository.getInstance().getScheduleSlots()) {
            Session session = AgendaRepository.getInstance().getSession(slot.sessionId);
            if (session != null && session.language != null ) {
                if (getResources().getString(Session.getLanguageFullName(session.language)).equalsIgnoreCase(userlang)){
                    if (slot.startDate <= currentTime && slot.endDate > currentTime){
                        resultSessions.add(slot);
                    }
                }
            }
          }*/

        addListView(resultSessions);
        return "current sessions are " + resultSessions.size() + "/" + AgendaRepository.getInstance().getScheduleSlots().size() + " sessions " ;
    }


    private String treatNextSessionQuestions(String userlang){

        /* for test currentTime : 1524466800000l <- 23/04 à 8h  */
        List<ScheduleSlot> filtredByLanguage = filtredSlotByLanguage(userlang);


        /* retrieve the next session' startDate */
        ScheduleSlot a = filtredByLanguage
                .stream()
                .peek(num -> System.out.println("will filter " + num))
                .filter(x -> x.startDate > currentTime)
                .findFirst()
                .orElse(null);

        /* retrieve all the next sessions start from X date */
        List<ScheduleSlot> result = filtredByLanguage
                .stream()// convert list to stream
                .filter(slot -> a.startDate == (slot.startDate))
                .collect(Collectors.toList());

    /* before Java8
        long nextStartSessiontime = 0l;
        Collections.sort(slots);
        for (ScheduleSlot slot :slots ) {
            Session session = AgendaRepository.getInstance().getSession(slot.sessionId);
            if (session != null && session.language != null ) {
                if (slot.startDate > currentTime){
                    nextStartSessiontime = slot.startDate ;
                    break;
                }
            }
        }
        resultSessions = AgendaRepository.getInstance().filtredScheduleSlot(nextStartSessiontime,userlang);
        */

        addListView(result);
        return "next sessions are " + result.size() + "/" + AgendaRepository.getInstance().getScheduleSlots().size() + " sessions " ;
    }

    private List<ScheduleSlot> filtredSlotByLanguage(String userlang){

        List<ScheduleSlot> filtredByLanguage =  AgendaRepository.getInstance().getScheduleSlots()
                .stream()
                .filter(x -> AgendaRepository.getInstance().getSession(x.sessionId) != null
                        && AgendaRepository.getInstance().getSession(x.sessionId).language != null
                        && getResources().getString(Session.getLanguageFullName(AgendaRepository.getInstance().getSession(x.sessionId).language)).equalsIgnoreCase(userlang))
                .sorted()
                .collect(Collectors.toList());

        return  filtredByLanguage;
    }

    private void processAiResponse(AIResponse aiResponse) {
        try {

            botButton.setVisibility(View.VISIBLE);
            botListening.setVisibility(View.GONE);
            botTreatingProgressBar.setVisibility(View.GONE);


            final Status status = aiResponse.getStatus();
            Log.i(TAG, "Status code: " + status.getCode());
            Log.i(TAG, "Status type: " + status.getErrorType());

            final Result result = aiResponse.getResult();
            Log.i(TAG, "Action: " + aiResponse.getResult().getAction());
            Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

            final String speech = result.getFulfillment().getSpeech();
            Log.i(TAG, "Speech: " + speech);


            final Metadata metadata = result.getMetadata();
            if (metadata != null) {
                Log.i(TAG, "Intent id: " + metadata.getIntentId());
                Log.i(TAG, "Intent name: " + metadata.getIntentName());
            }

            addQuestionView(result.getResolvedQuery());

            // TODO all cases + clean up + export Strings
            if (aiResponse.getResult().getAction().equalsIgnoreCase("action.sessions.byfilter")) {
                // Get parameters
                String parameterString = "";

                if (result.getParameters() != null && !result.getParameters().isEmpty()) {
                    for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                        parameterString += "[" + entry.getKey() + ": " + entry.getValue() + "] ";
                    }
                    addAnswerView(treatQuestion(result.getParameters()));
                } else {
                    addAnswerView("I got that, but no params... Try again");
                }

            } else {
                addAnswerView("Bummer, I did not get that. Can you repeat please?");
            }

        } catch (Exception e) {
            Log.e(TAG, "Exception " + e.getMessage());
        }
    }

    private void addCarouselView(List<ScheduleSlot> slots) {

        LinearLayout.LayoutParams defaultWrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        HorizontalScrollView hScrollView = new HorizontalScrollView(getContext());
        hScrollView.setLayoutParams(defaultWrapParams);
        hScrollView.setPadding(0, 0, 0, largePadding);

        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setLayoutParams(defaultWrapParams);
        ll.setPadding(largePadding, 0, largePadding, 0);


        LinearLayout.LayoutParams viewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        viewParams.setMargins(defaultPadding, 0, defaultPadding, 0);


        for (ScheduleSlot slot : slots) {
            Session session = AgendaRepository.getInstance().getSession(slot.sessionId);
            TextView tvSession = new TextView(this.getContext());
            tvSession.setText(session != null ? session.title : "No session");
            tvSession.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_bot_answer));
            tvSession.setLayoutParams(viewParams);
            ll.addView(tvSession);
        }

        hScrollView.addView(ll);
        botLayout.addView(hScrollView);

    }

    private void addListView(List<ScheduleSlot> slots) {

        LinearLayout.LayoutParams defaultWrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);


        LinearLayout ll = new LinearLayout(getContext());
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(defaultWrapParams);
        ll.setPadding(largePadding, 0, largePadding, largePadding);


        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        for (final ScheduleSlot slot : slots) {
            final Session session = AgendaRepository.getInstance().getSession(slot.sessionId);
            final Room sessionRoom = AgendaRepository.getInstance().getRoom(slot.room);

            TextView tvSession = new TextView(this.getContext());

            final String sessionDate = DateUtils.formatDateRange(
                    getContext(),
                    new Formatter(getResources().getConfiguration().locale),
                    slot.startDate,
                    slot.endDate,
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_TIME,
                    null).toString();


            tvSession.setText(
                    "Title: " + (session != null ? session.title : "No session") +
                            "\nSubtype: " + session.subtype +
                            "\nRoom: " + sessionRoom.name +
                            "\nDate: " + sessionDate
            );

            tvSession.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_bot_list));
            tvSession.setLayoutParams(contentParams);
            tvSession.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ScheduleSession scheduleSession = new ScheduleSession(slot, session.title, session.language);
                    SessionDetailActivity.startActivity(getContext(), scheduleSession);
                }
            });
            ll.addView(tvSession);
        }

        botLayout.addView(ll);

    }

    private class AiTask extends AsyncTask<AIRequest, Void, AIResponse> {
        @Override
        protected AIResponse doInBackground(AIRequest... requests) {
            final AIRequest request = requests[0];
            try {
                return aiDataService.request(request);
            } catch (AIServiceException e) {
                Log.e(TAG, "Exception " + e.getMessage());
                Toast.makeText(MakerDroidFragment.this.getContext(), "An error occurred.\nPlease try again.", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(AIResponse aiResponse) {
            if (aiResponse != null) {
                processAiResponse(aiResponse);
            }
        }
    }
}
