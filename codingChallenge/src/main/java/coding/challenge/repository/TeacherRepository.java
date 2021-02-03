package coding.challenge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import coding.challenge.model.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
	
	@Query("SELECT c.teacher FROM Course c WHERE c.id = :courseId")
	public Optional<Teacher> findByCourseId(@Param("courseId") Long courseId);
}
