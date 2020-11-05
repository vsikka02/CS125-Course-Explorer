package edu.illinois.cs.cs125.fall2020.mp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;

import edu.illinois.cs.cs125.fall2020.mp.R;
import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.databinding.ActivityCourseBinding;
import edu.illinois.cs.cs125.fall2020.mp.models.Course;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import edu.illinois.cs.cs125.fall2020.mp.network.Client;

public class CourseActivity extends AppCompatActivity implements Client.CourseClientCallbacks {

  private static final String TAG = CourseActivity.class.getSimpleName();


  private ActivityCourseBinding binding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.i(TAG, "Course Activity Started");

    Intent intent = getIntent();

    ObjectMapper mapper = new ObjectMapper();
    try {
      Course course = mapper.readValue(intent.getStringExtra("COURSE"), Course.class);
      binding = DataBindingUtil.setContentView(this, R.layout.activity_course);
      binding.description.setText(Summary.fullCourseString(course));
      binding.title.setText(course.getDescription());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
