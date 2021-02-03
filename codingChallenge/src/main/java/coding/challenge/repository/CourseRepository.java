package coding.challenge.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import coding.challenge.model.Course;
import coding.challenge.model.Teacher;

public interface CourseRepository extends JpaRepository<Course, Long> {
	
	List<Course> findAllByTeacher(Teacher teacher);
	
	@Query("SELECT c, SUM(c.price) FROM Enrolment e INNER JOIN e.course c "
			+ "WHERE YEAR(e.date) = :year AND MONTH(e.date) = :month "
			+ "GROUP BY c.id ORDER BY SUM(c.price) DESC")
	List<Object[]> findByDateYearAndMonth(@Param("year") int year, @Param("month") int month, Pageable pageable);
}
