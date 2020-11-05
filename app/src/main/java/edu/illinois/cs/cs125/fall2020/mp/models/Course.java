package edu.illinois.cs.cs125.fall2020.mp.models;

public class Course extends Summary {
  private String description;

  /**
   * Get the description for this Course.
   *
   * @return the year for this Course
   */
  public final String getDescription() {
    return description;
  }

  public Course() { }

  public Course(
          final String setYear,
          final String setDepartment,
          final String setSemester,
          final String setTitle,
          final String setNumber,
          final String setDescription
  ) {
    super(setYear, setSemester, setDepartment, setNumber, setTitle);
    description = setDescription;
  }
}
