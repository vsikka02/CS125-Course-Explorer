package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Course;
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;

/** Course activity showing the description and title for course. */
public class CourseActivity extends AppCompatActivity implements Client.CourseClientCallbacks {

  private static final String TAG = CourseActivity.class.getSimpleName();

  private ActivityCourseBinding binding;

  private Summary summary;

  private Course course;

  private Rating rating;


  /** @param savedInstanceState */
  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CourseableApplication application = (CourseableApplication) getApplication();
    application.getCourseClient();
    Log.i(TAG, "Course Activity Started");

    Intent intent = getIntent();

    ObjectMapper mapper =
        new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      summary = mapper.readValue(intent.getStringExtra("COURSE"), Summary.class);

      CompletableFuture<Course> completableFuture = new CompletableFuture<>();
      Client.start()
          .getCourse(
              summary,
              new Client.CourseClientCallbacks() {
                @Override
                public void courseResponse(final Summary summary1, final Course course1) {
                  completableFuture.complete(course1);
                }
              });
      CompletableFuture<Rating> completableFuture2 = new CompletableFuture<>();
      Client.start()
              .getRating(
                      summary, application.getCourseClientID(),
                      new Client.CourseClientCallbacks() {
                        @Override
                        public void yourRating(Summary summary2, Rating rating2) {
                          completableFuture2.complete(rating2);
                        }
                      });

      try {
        course = completableFuture.get();
        rating = completableFuture2.get();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
      binding.title.setText(Summary.fullCourseString(course));
      binding.description.setText(course.getDescription());
      binding.rating.setRating((float) rating.getRating());
      binding.rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
          Client.start().postRating(summary, new Rating(application.getCourseClientID(), v), new Client.CourseClientCallbacks() {
            @Override
            public void yourRating(Summary summary, Rating rating) {
              completableFuture2.complete(rating);
            }
          });
        }
      });

    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
