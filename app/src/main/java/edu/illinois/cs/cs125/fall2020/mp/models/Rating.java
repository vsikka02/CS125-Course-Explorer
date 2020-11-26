package edu.illinois.cs.cs125.fall2020.mp.models;

/** Rating class for storing client ratings of courses. */
public class Rating {
  /** Rating indicating that the course has not been rated yet. * */
  public static final double NOT_RATED = -1.0;

  private String id;
  private double rating;

  /**
   * Empty Constructor for Jackson to Deserialize and Serialize Rating object correctly.
   */
  public Rating() {}

  /**
   * Creates a Rating object with the given fields.
   * @param setId
   * @param setRating
   */
  public Rating(final String setId, final double setRating) {
    id = setId;
    rating = setRating;
  }

  /**
   * Gets the UUID for this rating.
   * @return String formatted UUID for the rating.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the actual Rating for the course object.
   * @return Double between 0 - 5.
   */
  public double getRating() {
    return rating;
  }
}
