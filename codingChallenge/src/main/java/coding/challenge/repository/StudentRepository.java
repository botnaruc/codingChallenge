package coding.challenge.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import coding.challenge.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
	
	public List<Student> findAllByFirstName(String firstName, Pageable pageable);
}
