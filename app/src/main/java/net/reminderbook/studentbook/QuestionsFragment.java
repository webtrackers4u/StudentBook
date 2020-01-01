package net.reminderbook.studentbook;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;


import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class QuestionsFragment extends Fragment {

    //subjects
    private ArrayList<String> filter_subjects_array = new ArrayList<String>();
    ////
    private Global os;
    private QuestionsFragment parentFrag;
    private RecyclerView questionRecyclerView;
    private ListingAdapter recyclerViewAdapter;
    private ArrayList<QuestionModel> rowsArrayList = new ArrayList<>();

    ///filter views
    private TabLayout subjects_tab;
    private RadioGroup question_type_radio_group;
    private ImageView more_filter_button;

    //filter keys
    private String filter_question_type = "";
    private String filter_subject = "";
    private String filter_text = "";

    public String filter_marks = "";
    public String filter_overall_star = "";
    public String filter_favourite = "";
    public String filter_importance = "";
    ///var
    private int currentPage = 1;
    private int currentDataSize = 0;
    private int totalDataSize = 0;
    private int questionCount = 0;
    private boolean isLoading = false;

    ////
    private CoordinatorLayout root;

    private HttpReq http;
    // newInstance constructor for creating fragment with arguments
    // Store instance variables based on arguments passed
    @Override
    public void onCreate( Bundle savedInstanceState ) { super.onCreate(savedInstanceState); }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState ) {

        View view = inflater.inflate(R.layout.fragment_questions, container, false);

        os = new Global(getContext());
        http = new HttpReq(getContext(), Global.Services.API);
        http.set_api_key("x555ls8454");
        http.activateDebugging(true);

        questionRecyclerView = view.findViewById(R.id.questionRecyclerView);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewAdapter = new ListingAdapter(getContext(), rowsArrayList);
        questionRecyclerView.setAdapter(recyclerViewAdapter);

        /////
        questionRecyclerView.setOnScrollChangeListener(new RecyclerView.OnScrollChangeListener() {

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onScrollChange( View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY ) {
                if (!v.canScrollVertically(1) && !isLoading && questionCount < totalDataSize) {
                    isLoading = true;
                    populateData(String.valueOf(currentPage + 1));
                    os.toast("There are more questions to show.");
                }
            }
        });
        root = view.findViewById(R.id.rootWindow);
        ///Filter Section
        subjects_tab = view.findViewById(R.id.subjects_tab);
        more_filter_button = view.findViewById(R.id.more_filter_button);
        question_type_radio_group = view.findViewById(R.id.question_type_radio_group);
        populateSubjects();
        populateMoreFilter();
        ///////
        question_type_radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.no_type:
                        filter_question_type = "";
                        break;
                    case R.id.multiple_choice:
                        filter_question_type="Multiple choice";
                        break;
                    case R.id.descriptive:
                        filter_question_type="Descriptive";
                        break;
                }
                clearAllData();
                populateData("1");
            }
        });
        //////
        more_filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent((MainActivity)getActivity(), FilterActivity.class);
                i.putExtra("request_code", FilterActivity.ActivityConstants.FILTER_QUESTION);
                i.putExtra("favourite", filter_favourite);
                i.putExtra("importance", filter_importance);
                i.putExtra("overall_star", filter_overall_star);
                ((MainActivity)getActivity()).startActivityForResult(i, FilterActivity.ActivityConstants.FILTER_QUESTION);
            }
        });
        return view;
    }
    /***************************
     Populators
     ***************************/
    public void populateData( String page_no ) {
        http.calcelReq(RequestTags.QUESTION_LIST_REQUESTS);

        HttpReq.ParamsModel model = new HttpReq.ParamsModel();
        model.add("fetch_question", "OK");
        model.add("searchKey", filter_text);
        model.add("overAllStar_s", filter_overall_star);
        model.add("type_s", filter_question_type);
        model.add("marks_s", filter_marks);
        model.add("subjectId_s", filter_subject);
        model.add("page_no", page_no);
        model.add("memberId", os.get_logged_user());
        /////user filter///////
        model.add("fevorite", filter_favourite);
        model.add("star", filter_importance);


        http.setParams(model);


        http.onStart(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                addLoadingView();
                isLoading=true;
            }
        });
        http.onSuccess(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                dismissLoadingView();
                isLoading=false;
                String response = getData();
                try {
                    JSONObject all_data = new JSONObject(response);
                    JSONArray data = all_data.getJSONArray("data");
                    totalDataSize = all_data.getInt("total_data");
                    if (totalDataSize == 0) {
                        clearAllData();
                        addErrorView("Sorry! there are no questions to show");
                    }
                    for (int x = 0; x < data.length(); x++) {
                        questionCount++;
                        JSONObject question = data.getJSONObject(x);
                        QuestionModel question_list = new QuestionModel();

                        question_list.set_type(question.getString("type"));

                        /////question
                        if (question.getString("type").matches("Descriptive")) {
                            question_list.setView_type(ViewHolderType.DESCRIPTIVE_TYPE);
                        } else {
                            question_list.setView_type(ViewHolderType.MULTIPLE_TYPE);
                        }
                        question_list.setQuestionCount(questionCount);

                        question_list.set_questionId(question.getString("questionId"));
                        question_list.set_subjectId(question.getString("subjectId"));
                        question_list.set_overAllStar(question.getString("overAllStar"));
                        //question text
                        question_list.set_questionText(question.getString("questionText"));
                        question_list.set_questionImage(question.getString("questionImage"));
                        //answer 1
                        question_list.set_answerText1(question.getString("answerText1"));
                        question_list.set_answerImage1(question.getString("answerImage1"));
                        //answer 2
                        question_list.set_answerText2(question.getString("answerText2"));
                        question_list.set_answerImage2(question.getString("answerImage2"));
                        //answer 3
                        question_list.set_answerText3(question.getString("answerText3"));
                        question_list.set_answerImage3(question.getString("answerImage3"));
                        //answer 4
                        question_list.set_answerText4(question.getString("answerText4"));
                        question_list.set_answerImage4(question.getString("answerImage4"));
                        //correct answer
                        question_list.set_correctAnswer(question.getString("correctAnswer"));
                        //favourite
                        question_list.set_fevorite(question.getString("fevorite"));
                        //star
                        question_list.set_star(question.getString("star"));
                        question_list.setContext(getContext());

                        rowsArrayList.add(question_list);
                        recyclerViewAdapter.notifyDataSetChanged();
                        currentDataSize++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        http.onError(new HttpReq.CallbackListener(){
            @Override
            public void run() {
                super.run();
                dismissLoadingView();
                isLoading =false;
                addErrorView("Sorry! no internet connection please try again later");
            }
        });

        http.prepare();
        http.commit(RequestTags.QUESTION_LIST_REQUESTS);
    }
    private void populateSubjects(){

        try {
            JSONArray subjects = new JSONArray(os.get_member_subjects());
            for(int key=0; key<subjects.length();key++){
                JSONObject subject = subjects.getJSONObject(key);
                String subjectName = subject.getString("subjectName");
                String subjectId = subject.getString("subjectId");



                TabLayout.Tab tab =  subjects_tab.newTab().setText(subjectName);
                tab.setContentDescription(subjectId);

                subjects_tab.addTab(tab);
                // populate first row
                if(key==0){
                    filter_subject = subjectId;
                    populateData("1");
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        subjects_tab.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                clearAllData();
                filter_subject = tab.getContentDescription().toString();
                populateData("1");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }
    private void populateMoreFilter(){


    }

    /*******************************
     * Common Functions
     *******************************/
    private void addHeaderView() {
        if (rowsArrayList.isEmpty()) {
            QuestionModel header_view = new QuestionModel();
            header_view.setView_type(ViewHolderType.HEADER_TYPE);
            rowsArrayList.add(header_view);
            questionRecyclerView.scrollToPosition(rowsArrayList.size());
        }
    }
    private void addLoadingView() {
        QuestionModel loading_view = new QuestionModel();
        loading_view.setView_type(ViewHolderType.LOADING_TYPE);
        rowsArrayList.add(loading_view);
        questionRecyclerView.scrollToPosition(rowsArrayList.size());
    }
    private void dismissLoadingView() {
        if (rowsArrayList.size() > 0) {
            rowsArrayList.remove(rowsArrayList.size() - 1);
        }
        isLoading = false;
    }
    private void addErrorView( String error_message ) {
        clearAllData();
        QuestionModel error_view = new QuestionModel();
        error_view.setView_type(ViewHolderType.ERROR_TYPE);
        error_view.setError_message(error_message);
        rowsArrayList.add(error_view);
        recyclerViewAdapter.notifyDataSetChanged();
    }
    private void showMarksMenu(final TextView v) {
        PopupMenu popup = new PopupMenu(getContext(), v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String mark = (String) item.getTitle();
                if(mark.matches("Marks")){
                    filter_marks ="";
                    v.setText(mark);
                } else {
                    filter_marks = mark;
                    v.setText(mark);
                }
                clearAllData();
                populateData("1");
                return true;
            }
        });
        popup.inflate(R.menu.marks_selection_menu);
        popup.show();


    }
    public void clearAllData() {
        if (!rowsArrayList.isEmpty()) {
            rowsArrayList.clear();
            recyclerViewAdapter.notifyDataSetChanged();
            currentPage = 1;
            currentDataSize = 0;
            questionCount = 0;
        }
    }

    /**********************************
     * Listing Adapter
     *********************************/
    private class ListingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<QuestionModel> question_list;
        private Global os;
        private Context ctx;
        private HttpReq http;

        final private static int UPDATE_IMPORTANCY_TAG=0;
        final private static int UPDATE_FEVORITE_TAG=1;

        ListingAdapter(Context ctx, ArrayList<QuestionModel> itemList) {
            this.question_list = itemList;
            this.ctx = ctx;
            this.os = new Global(getContext());
            http = new HttpReq(getContext(),Global.Services.API);
            http.set_api_key(os.get_api_key());
            //http.activateDebugging(true);
        }
        private Context getContext(){
            return this.ctx;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Defailt
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.error_row, parent, false);
            RecyclerView.ViewHolder viewHolder = new ErrorViewHolder(view);

            switch (viewType){
                case ViewHolderType.DESCRIPTIVE_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.descriptive_question_row, parent, false);
                    viewHolder = new DescriptiveItemViewHolder(view);
                    break;
                case ViewHolderType.MULTIPLE_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.mcq_questions_row, parent, false);
                    viewHolder = new McqItemViewHolder(view);
                    break;
                case ViewHolderType.LOADING_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.loading_row, parent, false);
                    viewHolder = new LoadingViewHolder(view);
                    break;
                case ViewHolderType.HEADER_TYPE:
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_header_row, parent, false);
                    viewHolder = new ErrorViewHolder(view);
                    break;
            }
            return viewHolder;
        }
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof McqItemViewHolder) {
                populateMcqItemRows((McqItemViewHolder) viewHolder, position);
            } else if (viewHolder instanceof DescriptiveItemViewHolder) {
                populateDescriptiveItemRows((DescriptiveItemViewHolder) viewHolder, position);
            }  else if(viewHolder instanceof LoadingViewHolder){
                populateLoadingView((LoadingViewHolder) viewHolder, position);
            }  else if(viewHolder instanceof HeaderViewHolder){
                populateHeaderView((HeaderViewHolder) viewHolder, position);
            } else {
                populateErrorView((ErrorViewHolder) viewHolder, position);
            }
        }
        @Override
        public int getItemViewType(int position) {
            return question_list.get(position).getView_type();
        }
        @Override
        public int getItemCount() {
            return question_list == null ? 0 : question_list.size();
        }

        /****************************
         * ViewHolders
         ************************/
        //MCQ
        private class McqItemViewHolder extends RecyclerView.ViewHolder {
            //question
            TextView questionNumber;
            TextView questionText;
            ImageView questionImage;

            RatingBar overall_rating;
            //toggle_options_button
            TextView toggle_options_button;
            //answers containner
            LinearLayout answers_containner;
            //answer
            TextView answerText1, answerText2, answerText3, answerText4;
            ImageView answerImage1,answerImage2, answerImage3, answerImage4;

            //toggle_correct_answer_button
            TextView toggle_correct_answer_button;
            //correct_answer_number
            TextView correct_answer_number;
            //fevorite button
            ToggleButton favouriteButton;
            //Importancy
            RadioGroup importancy_slider;

            McqItemViewHolder(@NonNull View itemView) {
                super(itemView);

                questionNumber = itemView.findViewById(R.id.question_number);
                questionText = itemView.findViewById(R.id.question_text);
                questionImage = itemView.findViewById(R.id.question_image);

                overall_rating = itemView.findViewById(R.id.overall_rating);

                //show options button
                toggle_options_button = itemView.findViewById(R.id.toggle_options_button);
                //answers_containner
                answers_containner = itemView.findViewById(R.id.answers_containner);
                //answer 1
                answerText1 = itemView.findViewById(R.id.answerText1);
                answerImage1 = itemView.findViewById(R.id.answerImage1);

                //answer 2
                answerText2 = itemView.findViewById(R.id.answerText2);
                answerImage2 = itemView.findViewById(R.id.answerImage2);

                //answer 3
                answerText3 = itemView.findViewById(R.id.answerText3);
                answerImage3 = itemView.findViewById(R.id.answerImage3);

                //answer 4
                answerText4 = itemView.findViewById(R.id.answerText4);
                answerImage4 = itemView.findViewById(R.id.answerImage4);

                //toggle_correct_answer_button
                toggle_correct_answer_button = itemView.findViewById(R.id.toggle_correct_answer_button);
                //correct_answer_number
                correct_answer_number = itemView.findViewById(R.id.correct_answer_number);
                //toggle button
                favouriteButton = itemView.findViewById(R.id.favouriteButton);

                importancy_slider = itemView.findViewById(R.id.importancy_slider);
            }
        }
        //Descriptive
        private class DescriptiveItemViewHolder extends RecyclerView.ViewHolder {
            //question
            TextView questionNumber;
            TextView questionText;
            ImageView questionImage;

            //toggle_options_button
            TextView toggle_options_button;

            RatingBar overall_rating;
            //answers containner
            LinearLayout answers_containner;
            //answer
            TextView answerText1, answerText2, answerText3, answerText4;
            ImageView answerImage1,answerImage2, answerImage3, answerImage4;
            //fevorite button
            ToggleButton favouriteButton;
            RadioGroup importancy_slider;



            DescriptiveItemViewHolder(@NonNull View itemView) {
                super(itemView);

                questionNumber = itemView.findViewById(R.id.question_number);
                questionText = itemView.findViewById(R.id.question_text);
                questionImage = itemView.findViewById(R.id.question_image);

                overall_rating = itemView.findViewById(R.id.overall_rating);

                //show options button
                toggle_options_button = itemView.findViewById(R.id.toggle_options_button);
                //answers_containner
                answers_containner = itemView.findViewById(R.id.answers_containner);
                //answer 1
                answerText1 = itemView.findViewById(R.id.answerText1);
                answerImage1 = itemView.findViewById(R.id.answerImage1);

                //answer 2
                answerText2 = itemView.findViewById(R.id.answerText2);
                answerImage2 = itemView.findViewById(R.id.answerImage2);

                //answer 3
                answerText3 = itemView.findViewById(R.id.answerText3);
                answerImage3 = itemView.findViewById(R.id.answerImage3);

                //answer 4
                answerText4 = itemView.findViewById(R.id.answerText4);
                answerImage4 = itemView.findViewById(R.id.answerImage4);
                //toggle button
                favouriteButton = itemView.findViewById(R.id.favouriteButton);
                importancy_slider = itemView.findViewById(R.id.importancy_slider);
            }
        }
        //loading
        private class LoadingViewHolder extends RecyclerView.ViewHolder {

            LoadingViewHolder(@NonNull View itemView) {
                super(itemView);

            }
        }
        //Header
        private class HeaderViewHolder extends RecyclerView.ViewHolder {

            HeaderViewHolder(@NonNull View itemView) {
                super(itemView);

            }
        }
        //Error
        private class ErrorViewHolder extends RecyclerView.ViewHolder {
            private TextView textView;
            ErrorViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.text);

            }
        }

        /****************************
         * Populators
         ************************/
        //MCQ
        private void populateMcqItemRows( final McqItemViewHolder viewHolder, int position) {
            final QuestionModel question = question_list.get(position);
            //basic
            viewHolder.overall_rating.setRating(Integer.parseInt(question.get_overAllStar()));
            //question number
            viewHolder.questionNumber.setText(getCount(question.getQuestionCount()));
            //question text
            viewHolder.questionText.setVisibility(question.get_questionText().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.questionImage.setVisibility(question.get_questionImage().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.questionText,question.get_questionText());
            os.setImage(viewHolder.questionImage,question.get_questionImage(), false);
            //answer 1
            viewHolder.answerText1.setVisibility(question.get_answerText1().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage1.setVisibility(question.get_answerImage1().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText1,question.get_answerText1());
            os.setImage(viewHolder.answerImage1,question.get_answerImage1(), false);
            //answer 2
            viewHolder.answerText2.setVisibility(question.get_answerText2().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage2.setVisibility(question.get_answerImage2().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText2,question.get_answerText2());
            os.setImage(viewHolder.answerImage2,question.get_answerImage2(), false);
            //answer 3
            viewHolder.answerText3.setVisibility(question.get_answerText3().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage3.setVisibility(question.get_answerImage3().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText3,question.get_answerText3());
            os.setImage(viewHolder.answerImage3,question.get_answerImage3(), false);
            //answer 4
            viewHolder.answerText4.setVisibility(question.get_answerText4().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage4.setVisibility(question.get_answerImage4().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText4,question.get_answerText4());
            os.setImage(viewHolder.answerImage4,question.get_answerImage4(),false);
            //correct answer
            os.setTextWithImage(viewHolder.correct_answer_number, question.get_correctAnswer());
            ///importancy slider
            final boolean[] firstCheck = {true};
            viewHolder.importancy_slider.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged( RadioGroup group, int checkedId ) {
                    if(!firstCheck[0]) {
                        switch (checkedId) {
                            case R.id.im1:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "1");
                                break;
                            case R.id.im2:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "2");
                                break;
                            case R.id.im3:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "3");
                                break;
                            case R.id.im4:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "4");
                                break;
                            case R.id.im5:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "5");
                                break;
                        }
                    }
                }
            });
            switch (question.get_star()){
                case "1":
                    viewHolder.importancy_slider.check(R.id.im1);
                    break;
                case "2":
                    viewHolder.importancy_slider.check(R.id.im2);
                    break;
                case "3":
                    viewHolder.importancy_slider.check(R.id.im3);
                    break;
                case "4":
                    viewHolder.importancy_slider.check(R.id.im4);
                    break;
                case "5":
                    viewHolder.importancy_slider.check(R.id.im5);
                    break;
            }
            firstCheck[0] =false;
            //fevorite button
            viewHolder.favouriteButton.setChecked(question.get_fevorite().matches("yes"));
            viewHolder.favouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {

                    boolean isChecked = viewHolder.favouriteButton.isChecked();
                    if(!isChecked){
                        updatefevorite(question.get_questionId(), question.get_subjectId(), "no");
                        question.set_fevorite("no");
                    } else {

                        updatefevorite(question.get_questionId(), question.get_subjectId(), "yes");
                        question.set_fevorite("yes");
                    }
                }
            });
            //expand functions
            final int pos = position;

            //expand options
            viewHolder.answers_containner.setVisibility(question.isexpanded?View.VISIBLE:View.GONE);
            viewHolder.toggle_options_button.setText(question.isexpanded?"Hide Options":"Show Options");
            viewHolder.toggle_options_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    QuestionModel question = question_list.get(pos);
                    question.isexpanded = !question.isexpanded;
                    viewHolder.answers_containner.setVisibility(question.isexpanded?View.VISIBLE:View.GONE);
                    viewHolder.toggle_options_button.setText(question.isexpanded?"Hide Options":"Show Options");
                }
            });
            //expand correct answer
            viewHolder.correct_answer_number.setText("A"+question.get_correctAnswer());
            viewHolder.correct_answer_number.setVisibility(question.isAnswerExpanded?View.VISIBLE:View.GONE);
            viewHolder.toggle_correct_answer_button.setText(question.isAnswerExpanded?"Hide Answer":"Show Answer");
            viewHolder.toggle_correct_answer_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    QuestionModel question = question_list.get(pos);
                    question.isAnswerExpanded = !question.isAnswerExpanded;
                    viewHolder.correct_answer_number.setVisibility(question.isAnswerExpanded?View.VISIBLE:View.GONE);
                    viewHolder.toggle_correct_answer_button.setText(question.isAnswerExpanded?"Hide Answer":"Show Answer");
                }
            });

        }
        //Descriptive
        private void populateDescriptiveItemRows( final DescriptiveItemViewHolder viewHolder, int position) {
            final QuestionModel question = question_list.get(position);
            //basic
            viewHolder.overall_rating.setRating(Integer.valueOf(question.get_overAllStar()));
            //question number
            viewHolder.questionNumber.setText(getCount(question.getQuestionCount()));
            //question text
            viewHolder.questionText.setVisibility(question.get_questionText().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.questionImage.setVisibility(question.isEmptyQuestionImage()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.questionText,question.get_questionText());
            os.setImage(viewHolder.questionImage,question.get_questionImage(),false);
            //answer 1
            viewHolder.answerText1.setVisibility(question.get_answerText1().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage1.setVisibility(question.get_answerImage1().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText1,question.get_answerText1());
            os.setImage(viewHolder.answerImage1,question.get_answerImage1(),false);
            //answer 2
            viewHolder.answerText2.setVisibility(question.get_answerText2().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage2.setVisibility(question.get_answerImage2().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText2,question.get_answerText2());
            os.setImage(viewHolder.answerImage2,question.get_answerImage2(),false);
            //answer 3
            viewHolder.answerText3.setVisibility(question.get_answerText3().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage3.setVisibility(question.get_answerImage3().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText3,question.get_answerText3());
            os.setImage(viewHolder.answerImage3,question.get_answerImage3(),false);
            //answer 4
            viewHolder.answerText4.setVisibility(question.get_answerText4().isEmpty()?View.GONE:View.VISIBLE);
            viewHolder.answerImage4.setVisibility(question.get_answerImage4().isEmpty()? View.GONE:View.VISIBLE);
            os.setTextWithImage(viewHolder.answerText4,question.get_answerText4());
            os.setImage(viewHolder.answerImage4,question.get_answerImage4(),false);
            ///importancy slider
            final boolean[] firstCheck = {true};
            viewHolder.importancy_slider.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged( RadioGroup group, int checkedId ) {

                    if(!firstCheck[0]) {
                        switch (checkedId) {
                            case R.id.im1:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "1");
                                break;
                            case R.id.im2:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "2");
                                break;
                            case R.id.im3:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "3");
                                break;
                            case R.id.im4:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "4");
                                break;
                            case R.id.im5:
                                updateImportancy(question.get_questionId(), question.get_subjectId(), "5");
                                break;
                        }
                    }

                }
            });
            switch (question.get_star()){
                case "1":
                    viewHolder.importancy_slider.check(R.id.im1);

                    break;
                case "2":
                    viewHolder.importancy_slider.check(R.id.im2);

                    break;
                case "3":
                    viewHolder.importancy_slider.check(R.id.im3);

                    break;
                case "4":
                    viewHolder.importancy_slider.check(R.id.im4);

                    break;
                case "5":
                    viewHolder.importancy_slider.check(R.id.im5);

                    break;
                default:

                    break;
            }
            //fevorite button
            viewHolder.favouriteButton.setChecked(question.get_fevorite().matches("yes"));
            viewHolder.favouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {

                    boolean isChecked = viewHolder.favouriteButton.isChecked();
                    if(!isChecked){
                        updatefevorite(question.get_questionId(), question.get_subjectId(), "no");
                        question.set_fevorite("no");
                    } else {

                        updatefevorite(question.get_questionId(), question.get_subjectId(), "yes");
                        question.set_fevorite("yes");
                    }
                }
            });
            firstCheck[0]=false;
            //expand functions
            final int pos = position;
            final DescriptiveItemViewHolder viewHolder1 = viewHolder;
            //expand options
            viewHolder.answers_containner.setVisibility(question.isexpanded?View.VISIBLE:View.GONE);
            viewHolder1.toggle_options_button.setText(question.isexpanded?"Hide Answer":"Show Answer");
            viewHolder.toggle_options_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    QuestionModel question = question_list.get(pos);
                    question.isexpanded = !question.isexpanded;
                    viewHolder1.answers_containner.setVisibility(question.isexpanded?View.VISIBLE:View.GONE);
                    viewHolder1.toggle_options_button.setText(question.isexpanded?"Hide Answer":"Show Answer");
                }
            });
        }
        //Loading
        private void populateLoadingView(LoadingViewHolder viewHolder, int position) {

        }
        //Header
        private void populateHeaderView(HeaderViewHolder viewHolder, int position) {

        }
        //Error
        private void populateErrorView(ErrorViewHolder viewHolder, int position) {
            QuestionModel question = question_list.get(position);
            viewHolder.textView.setText(question.getError_message());
        }

        /****************************
         * Actions
         ************************/

        private void updateImportancy(final String questionId, final String subjectId, final String star ){
            http.calcelReq(UPDATE_IMPORTANCY_TAG);
            HttpReq.ParamsModel model = new HttpReq.ParamsModel();
            model.add("question_update_importancy", "OK");
            model.add("memberId",os.get_logged_user());
            model.add("questionId",questionId);
            model.add("subjectId",subjectId);
            model.add("star",star);

            http.setParams(model);
            http.onSuccess(new HttpReq.CallbackListener(){
                @Override
                public void run() {
                    super.run();
                    String response = this.getData();
                }
            });
            http.prepare();
            http.commit(UPDATE_IMPORTANCY_TAG);
        }
        private void updatefevorite( final String questionId, final String subjectId, final String fevorite ){

            http.calcelReq(UPDATE_FEVORITE_TAG);
            HttpReq.ParamsModel model = new HttpReq.ParamsModel();
            model.add("question_update_fevorite", "OK");
            model.add("memberId",os.get_logged_user());
            model.add("questionId",questionId);
            model.add("subjectId",subjectId);
            model.add("fevorite",fevorite);

            http.setParams(model);
            http.onSuccess(new HttpReq.CallbackListener(){
                @Override
                public void run() {
                    super.run();
                    String response = this.getData();
                }
            });
            http.prepare();
            http.commit(UPDATE_FEVORITE_TAG);
        }

        /****************************
         * Common Functions
         ***************************/
        //Get count
        private String getCount(int count){
            String cc;
            if(count<10){
                cc = "0"+count;
            } else {
                cc = String.valueOf(count);
            }
            return cc;
        }
    }
    /****************************
     * Question Model
     ***************************/
    private class QuestionModel{


        boolean isexpanded = false;
        boolean isAnswerExpanded = false;

        private String questionId;
        private String code;
        private String subjectId;
        private String classId;
        private String boardId;
        private String memberId;
        private String marks;
        private String viewOrder;
        private String overAllStar;
        private String type;
        private String questionText;
        private String questionImage;
        private String answerText1;
        private String answerImage1;
        private String answerText2;
        private String answerImage2;
        private String answerText3;
        private String answerImage3;
        private String answerText4;
        private String answerImage4;
        private String correctAnswer;
        private String fevorite;
        private String star;

        private boolean firstStarChecked=false;
        private int questionCount;
        private Context ctx;

        void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
        int getQuestionCount() { return questionCount; }
        private int view_type;
        private String search_text;
        private String error_message;
        //for error
        void setError_message( String error_message ) { this.error_message = error_message; }
        String getError_message() { return error_message; }
        //for filter
        public String getSearch_text() { return search_text; }
        public void setSearch_text( String search_text ) { this.search_text = search_text; }
        //for context
        public  void setContext(Context ctx){ this.ctx = ctx; }
        public Context getContext(){
            return this.ctx;
        }
        //for view type
        public int getView_type() { return view_type; }
        public void setView_type( int view_type ) { this.view_type = view_type; }
        //check login
        public boolean isLogin(){
            return !memberId.matches("");
        }
        //check question imge
        public boolean isEmptyQuestionImage(){return this.questionImage.matches("");}
        //questionId
        public String get_questionId() {return questionId;}
        public void set_questionId(String questionId) { this.questionId = questionId;}
        //code
        public String get_code() {return code;}
        public void set_code(String code) { this.code = code;}
        //subjectId
        public String get_subjectId() {return subjectId;}
        public void set_subjectId(String subjectId) { this.subjectId = subjectId;}
        //classId
        public String get_classId() {return classId;}
        public void set_classId(String classId) { this.classId = classId;}
        //boardId
        public String get_boardId() {return boardId;}
        public void set_boardId(String boardId) { this.boardId = boardId;}
        //memberId
        public String get_memberId() {return memberId;}
        public void set_memberId(String memberId) { this.memberId = memberId;}
        //marks
        public String get_marks() {return marks;}
        public void set_marks(String marks) { this.marks = marks;}
        //viewOrder
        public String get_viewOrder() {return viewOrder;}
        public void set_viewOrder(String viewOrder) { this.viewOrder = viewOrder;}
        //overAllStar
        public String get_overAllStar() {return overAllStar;}
        public void set_overAllStar(String overAllStar) { this.overAllStar = overAllStar;}
        //type
        public String get_type() {return type;}
        public void set_type(String type) { this.type = type;}
        //questionText
        public String get_questionText() {return questionText;}
        public void set_questionText(String questionText) { this.questionText = questionText;}
        //questionImage
        public String get_questionImage() {return questionImage;}
        public void set_questionImage(String questionImage) { this.questionImage = questionImage;}
        //answerText1
        public String get_answerText1() {return answerText1;}
        public void set_answerText1(String answerText1) { this.answerText1 = answerText1;}
        //answerImage1
        public String get_answerImage1() {return answerImage1;}
        public void set_answerImage1(String answerImage1) { this.answerImage1 = answerImage1;}
        //answerText2
        public String get_answerText2() {return answerText2;}
        public void set_answerText2(String answerText2) { this.answerText2 = answerText2;}
        //answerImage2
        public String get_answerImage2() {return answerImage2;}
        public void set_answerImage2(String answerImage2) { this.answerImage2 = answerImage2;}
        //answerText3
        public String get_answerText3() {return answerText3;}
        public void set_answerText3(String answerText3) { this.answerText3 = answerText3;}
        //answerImage3
        public String get_answerImage3() {return answerImage3;}
        public void set_answerImage3(String answerImage3) { this.answerImage3 = answerImage3;}
        //answerText4
        public String get_answerText4() {return answerText4;}
        public void set_answerText4(String answerText4) { this.answerText4 = answerText4;}
        //answerImage4
        public String get_answerImage4() {return answerImage4;}
        public void set_answerImage4(String answerImage4) { this.answerImage4 = answerImage4;}
        //correctAnswer
        public String get_correctAnswer() {return correctAnswer;}
        public void set_correctAnswer(String correctAnswer) { this.correctAnswer = correctAnswer;}
        //fevorite

        public void set_fevorite( String fevorite ) {
            this.fevorite = fevorite;
        }

        public String get_fevorite() {
            return this.fevorite;
        }

        public void set_star( String star ) {
            this.star = star;
        }

        public String get_star() {
            return this.star;
        }
    }
    /*****************************
     * Globals
     ****************************/
    private static class RequestTags {
        static final String QUESTION_LIST_REQUESTS="RequestTags:QUESTION_LIST_REQUESTS";
    }
    private static class ViewHolderType{
        static final int MULTIPLE_TYPE=1;
        static final int DESCRIPTIVE_TYPE=2;
        static final int LOADING_TYPE = 3;
        static final int HEADER_TYPE = 4;
        static final int ERROR_TYPE = 5;
    }

    public void Search(){
        Toast.makeText(getContext(),"cvcf", Toast.LENGTH_LONG).show();
    }
}

