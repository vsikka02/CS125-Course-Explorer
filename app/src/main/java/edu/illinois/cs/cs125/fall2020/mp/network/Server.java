package edu.illinois.cs.cs125.fall2020.mp.network;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import edu.illinois.cs.cs125.fall2020.mp.application.CourseableApplication;
import edu.illinois.cs.cs125.fall2020.mp.models.Rating;
import edu.illinois.cs.cs125.fall2020.mp.models.Summary;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Development course API server.
 *
 * <p>Normally you would run this server on another machine, which the client would connect to over
 * the internet. For the sake of development, we're running the server right alongside the app on
 * the same device. However, all communication between the course API client and course API server
 * is still done using the HTTP protocol. Meaning that eventually it would be straightforward to
 * move this server to another machine where it could provide data for all course API clients.
 *
 * <p>You will need to add functionality to the server for MP1 and MP2.
 */
public final class Server extends Dispatcher {
  @SuppressWarnings({"unused", "RedundantSuppression"})
  private static final String TAG = Server.class.getSimpleName();

  private final Map<String, String> summaries = new HashMap<>();

  private final int courseUrlLength = 4;

  // summary/2020/fall
  private MockResponse getSummary(@NonNull final String path) {
    String[] parts = path.split("/");
    if (parts.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    String summary = summaries.get(parts[0] + "_" + parts[1]);
    if (summary == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(summary);
  }

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private final Map<Summary, String> courses = new HashMap<>();

  // course/year/semester/department/number
  private MockResponse getCourse(@NonNull final String path) {
    String[] parts = path.split("/");
    if (parts.length != courseUrlLength) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    Summary searchSummary = new Summary(parts[0], parts[1], parts[2], parts[3], "searchSummary");
    String course = courses.get(searchSummary);
    if (course == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(course);
  }

  public static boolean isUUID(String string) {
    try {
      UUID.fromString(string);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }


  private final Map<Summary, Map<String, Rating>> ratings = new HashMap<>();
  private MockResponse getRating(@NonNull final String path) throws JsonProcessingException {
    String[] urlParts = path.split("\\?client=");
    String[] parts = urlParts[0].split("/");
    if (parts.length != courseUrlLength || urlParts.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    if (!isUUID(urlParts[1])) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    Summary searchSummary = new Summary(parts[0], parts[1], parts[2], parts[3], "searchSummary");
    String course = courses.get(searchSummary);
    if (course == null) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    }
    String searchUUID = urlParts[1];
    if (ratings.get(searchSummary) == null) {
      Map<String, Rating> inner = new HashMap<>();
      ratings.put(searchSummary, inner);
    }
    Rating defaultRating = new Rating(searchUUID, Rating.NOT_RATED);
    Rating rating = ratings.get(searchSummary).getOrDefault(searchUUID, defaultRating);
    ObjectMapper objectMapper = new ObjectMapper();
    String ratingDetails = objectMapper.writeValueAsString(rating);
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK).setBody(ratingDetails);
  }

  private MockResponse postRating(@NonNull final String path, @NonNull final RecordedRequest request) throws JsonProcessingException {
    String[] urlParts = path.split("\\?client=");
    String[] parts = urlParts[0].split("/");
    if (parts.length != courseUrlLength || urlParts.length != 2) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    if (!isUUID(urlParts[1])) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    String searchUUID = urlParts[1];
    String ratingDetails = request.getBody().readUtf8();

    Rating rating;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      rating = objectMapper.readValue(ratingDetails, Rating.class);
    } catch (JsonProcessingException e){
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    if (!(rating.getId().equals(searchUUID))) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
    }
    Summary searchSummary = new Summary(parts[0], parts[1], parts[2], parts[3], "searchSummary");
    if (ratings.get(searchSummary).get(searchUUID) == null) {
      ratings.get(searchSummary).put(searchUUID, rating);
    }
    ratings.get(searchSummary).put(searchUUID, rating);
    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_MOVED_TEMP).addHeader("Location: " + parts[3] + "?client=" + searchUUID);
  }

  @NonNull
  @Override
  public MockResponse dispatch(@NonNull final RecordedRequest request) {
    try {
      String path = request.getPath();
      if (path == null || request.getMethod() == null) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
      } else if (path.equals("/") && request.getMethod().equalsIgnoreCase("HEAD")) {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK);
      } else if (path.startsWith("/summary/")) {
        return getSummary(path.replaceFirst("/summary/", ""));
      } else if (path.startsWith("/course/")) {
        return getCourse(path.replaceFirst("/course/", ""));
      } else if (path.startsWith("/rating/")) {
        if (request.getMethod().equals("GET")) {
          return getRating(path.replace("/rating/", ""));
        } else if (request.getMethod().equals("POST")) {
          return postRating(path.replace("/rating/", ""), request);
        }
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST);
      }
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NOT_FOUND);
    } catch (Exception e) {
      return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }
  }

  private static boolean started = false;

  /**
   * Start the server if has not already been started.
   *
   * <p>We start the server in a new thread so that it operates separately from and does not
   * interfere with the rest of the app.
   */
  public static void start() {
    if (!started) {
      new Thread(Server::new).start();
      started = true;
    }
  }

  private final ObjectMapper mapper = new ObjectMapper();

  private Server() {
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    loadSummary("2020", "fall");
    loadCourses("2020", "fall");

    try {
      MockWebServer server = new MockWebServer();
      server.setDispatcher(this);
      server.start(CourseableApplication.SERVER_PORT);

      String baseUrl = server.url("").toString();
      if (!CourseableApplication.SERVER_URL.equals(baseUrl)) {
        throw new IllegalStateException("Bad server URL: " + baseUrl);
      }
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }

  @SuppressWarnings("SameParameterValue")
  private void loadSummary(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + "_summary.json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    summaries.put(year + "_" + semester, json);
  }

  @SuppressWarnings("SameParameterValue")
  private void loadCourses(@NonNull final String year, @NonNull final String semester) {
    String filename = "/" + year + "_" + semester + ".json";
    String json =
        new Scanner(Server.class.getResourceAsStream(filename), "UTF-8").useDelimiter("\\A").next();
    try {
      JsonNode nodes = mapper.readTree(json);
      for (Iterator<JsonNode> it = nodes.elements(); it.hasNext(); ) {
        JsonNode node = it.next();
        Summary course = mapper.readValue(node.toString(), Summary.class);
        courses.put(course, node.toPrettyString());
      }
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
