package edu.illinois.cs.cs125.fall2020.mp.models;

/** Model holding each individual course description shown in the CourseActivity. **/
public class Course extends Summary {
  private String description;

  /**
   * Get the description for this Course.
   *
   * @return the year for this Course.
   */
  public final String getDescription() {
    return description;
  }
  /** Create an empty Course. **/
  public Course() {}

  /**
   * Create a Course object with the provided fields.
   *
   * @param setYear
   * @param setDepartment
   * @param setSemester
   * @param setTitle
   * @param setNumber
   * @param setDescription
   */
  public Course(
      final String setYear,
      final String setDepartment,
      final String setSemester,
      final String setTitle,
      final String setNumber,
      final String setDescription) {
    super(setYear, setSemester, setDepartment, setNumber, setTitle);
    description = setDescription;
  }
}
