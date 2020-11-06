package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Course;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** Course activity showing the description and title for course. */
public class CourseActivity extends AppCompatActivity implements Client.CourseClientCallbacks {

  private static final String TAG = CourseActivity.class.getSimpleName();

  private ActivityCourseBinding binding;

  private Summary summary;

  private Course course;

  /** @param savedInstanceState */
  @Override
  public void onCreate(@Nullable final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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

      try {
        course = completableFuture.get();
      } catch (ExecutionException e) {
        e.printStackTrace();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
      binding.title.setText(Summary.fullCourseString(course));
      binding.description.setText(course.getDescription());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
