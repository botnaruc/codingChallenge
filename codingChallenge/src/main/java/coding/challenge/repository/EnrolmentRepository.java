package coding.challenge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import coding.challenge.model.Course;
import coding.challenge.model.Enrolment;
import coding.challenge.model.Student;

public interface EnrolmentRepository extends JpaRepository<Enrolment, Long> {
	
	@Query("SELECT e FROM Enrolment e WHERE e.course.id = :courseId AND e.student.id = :studentId")
	Optional<Enrolment> findByCourseAndStudent(@Param("courseId") Long courseId, @Param("studentId") Long studentId);
	
	List<Enrolment> findAllByCourse(Course course);
	
	List<Enrolment> findAllByStudent(Student student);
}
