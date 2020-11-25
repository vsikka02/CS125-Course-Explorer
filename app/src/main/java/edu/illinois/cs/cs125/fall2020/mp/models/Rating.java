package edu.illinois.cs.cs125.fall2020.mp.models;

/**
 * Rating class for storing client ratings of courses.
 */
public class Rating {
    /**Rating indicating that the course has not been rated yet. **/
    public static final double NOT_RATED = -1.0;

    private String id;
    private double rating;

    public Rating() {}

    public Rating(final String setId, final double setRating) {
        id = setId;
        rating = setRating;
    }
    public String getId(){ return id; }
    public double getRating(){ return rating; }
}
