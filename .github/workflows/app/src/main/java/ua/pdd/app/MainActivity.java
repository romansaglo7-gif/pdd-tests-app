package ua.pdd.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvCounter, tvQuestion, tvFeedback;
    private ImageView ivQuestion;
    private LinearLayout llAnswers;
    private Button btnNext;

    private List<Question> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int errorsCount = 0;

    static class Question {
        String text;
        String imageFile;
        List<String> options;
        int correctIndex;
        String explanation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCounter = findViewById(R.id.tvCounter);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvFeedback = findViewById(R.id.tvFeedback);
        ivQuestion = findViewById(R.id.ivQuestion);
        llAnswers = findViewById(R.id.llAnswers);
        btnNext = findViewById(R.id.btnNext);

        loadQuestions();

        if (!questionList.isEmpty()) {
            displayQuestion();
        } else {
            tvQuestion.setText("Базу питань не знайдено.");
        }

        btnNext.setOnClickListener(v -> {
            currentIndex++;
            if (currentIndex < questionList.size()) {
                displayQuestion();
            } else {
                showFinalResults();
            }
        });
    }

    private void loadQuestions() {
        try {
            InputStream is = getAssets().open("questions.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            JSONArray array = new JSONArray(json);
            List<Question> all = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Question q = new Question();
                q.text = obj.optString("question", obj.optString("text", ""));
                q.imageFile = obj.optString("image", "");
                q.explanation = obj.optString("explanation", "");
                q.correctIndex = obj.optInt("correct_answer", 0);

                JSONArray answersArray = obj.getJSONArray("answers");
                q.options = new ArrayList<>();
                for (int j = 0; j < answersArray.length(); j++) {
                    q.options.add(answersArray.getString(j));
                }
                all.add(q);
            }

            Collections.shuffle(all);
            int takeCount = Math.min(20, all.size());
            questionList = new ArrayList<>(all.subList(0, takeCount));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayQuestion() {
        btnNext.setVisibility(View.GONE);
        tvFeedback.setVisibility(View.GONE);
        llAnswers.removeAllViews();

        Question q = questionList.get(currentIndex);
        tvCounter.setText("Питання " + (currentIndex + 1) + " / " + questionList.size() + " (Помилок: " + errorsCount + ")");
        tvQuestion.setText(q.text);

        if (q.imageFile != null && !q.imageFile.isEmpty()) {
            try {
                InputStream is = getAssets().open("images/" + q.image деFile);
                Bitmap bmp = BitmapFactory.decodeStream(is);
                is.close();
                ivQuestion.setImageBitmap(bmp);
                ivQuestion.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                ivQuestion.setVisibility(View.GONE);
            }
        } else {
            ivQuestion.setVisibility(View.GONE);
        }

        for (int i = 0; i < q.options.size(); i++) {
            final int index = i;
            Button btn = new Button(this);
            btn.setText(q.options.get(i));
            btn.setTransformationMethod(null);
            btn.setTextSize(14f);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 8, 0, 8);
            btn.setLayoutParams(lp);

            btn.setOnClickListener(v -> checkAnswer(index));
            llAnswers.addView(btn);
        }
    }

    private void checkAnswer(int selectedIndex) {
        Question q = questionList.get(currentIndex);

        for (int i = 0; i < llAnswers.getChildCount(); i++) {
            llAnswers.getChildAt(i).setEnabled(false);
        }

        if (selectedIndex == q.correctIndex) {
            tvFeedback.setBackgroundColor(Color.parseColor("#C8E6C9"));
            tvFeedback.setTextColor(Color.parseColor("#1B5E20"));
            tvFeedback.setText("Правильно!");
        } else {
            errorsCount++;
            tvFeedback.setBackgroundColor(Color.parseColor("#FFCDD2"));
            tvFeedback.setTextColor(Color.parseColor("#B71C1C"));
            String exp = q.explanation.isEmpty() ? "" : "\n\n" + q.explanation;
            tvFeedback.setText("Неправильно. Правильна відповідь: " + (q.correctIndex + 1) + exp);
        }

        tvFeedback.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.VISIBLE);
        tvCounter.setText("Питання " + (currentIndex + 1) + " / " + questionList.size() + " (Помилок: " + errorsCount + ")");
    }

    private void showFinalResults() {
        llAnswers.removeAllViews();
        ivQuestion.setVisibility(View.GONE);
        btnNext.setVisibility(View.GONE);
        tvFeedback.setVisibility(View.GONE);

        tvCounter.setText("Тест завершено");
        boolean passed = errorsCount <= 2;

        if (passed) {
            tvQuestion.setText("Вітаємо! Іспит складено!\nПомилок: " + errorsCount + " з 2 допустимих.");
        } else {
            tvQuestion.setText("Іспит не складено.\nПомилок: " + errorsCount + " (допускається максимум 2).");
        }
    }
}
