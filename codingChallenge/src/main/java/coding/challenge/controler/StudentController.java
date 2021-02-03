package coding.challenge.controler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coding.challenge.exception.ResourceNotFoundException;
import coding.challenge.model.Enrolment;
import coding.challenge.model.Student;
import coding.challenge.repository.EnrolmentRepository;
import coding.challenge.repository.StudentRepository;

@RestController
@RequestMapping("/api/students")
public class StudentController {

	@Autowired
	private StudentRepository studentRepository;
	@Autowired
	private EnrolmentRepository enrolmentRepository;

	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<Student>>> getAll() {
		List<EntityModel<Student>> students = StreamSupport.stream(studentRepository.findAll().spliterator(), false)
				.map(student -> EntityModel.of(student,
						linkTo(methodOn(StudentController.class).getStudentById(student.getId())).withSelfRel(),
						linkTo(methodOn(StudentController.class).getAll()).withRel("students")))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(students, linkTo(methodOn(CourseController.class).getAll()).withSelfRel()));
	}

	@GetMapping("{id}")
	public ResponseEntity<EntityModel<Student>> getStudentById(@PathVariable(value = "id") Long studentId) {
		return studentRepository.findById(studentId)
				.map(student -> EntityModel.of(student,
						linkTo(methodOn(StudentController.class).getStudentById(student.getId())).withSelfRel(),
						linkTo(methodOn(StudentController.class).getAll()).withRel("students")))
				.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<?> createStudent(@Valid @RequestBody Student student) {
		student = studentRepository.save(student);

		EntityModel<Student> courseResource = EntityModel.of(student,
				linkTo(methodOn(StudentController.class).getStudentById(student.getId())).withSelfRel());

		try {
			return ResponseEntity.created(new URI(courseResource.getRequiredLink(IanaLinkRelations.SELF).getHref()))
					.body(courseResource);
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to build response for create " + student);
		}
	}

	@PutMapping("{id}")
	public ResponseEntity<?> updateStudent(@PathVariable(value = "id") Long studentId,
			@Valid @RequestBody Student student) {
		Student toUpdateStudent = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " not found"));

		toUpdateStudent.setFirstName(student.getFirstName());
		toUpdateStudent.setLastName(student.getLastName());
		toUpdateStudent.setBirthDate(student.getBirthDate());

		studentRepository.save(toUpdateStudent);

		Link link = linkTo(methodOn(CourseController.class).getCourseById(studentId)).withSelfRel();

		try {
			return ResponseEntity.noContent().location(new URI(link.getHref())).build();
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to build response for update " + student);
		}
	}

	@DeleteMapping("{id}")
	public ResponseEntity<?> deleteStudent(@PathVariable(value = "id") Long studentId) {
		Student toDeleteStudent = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " not found"));

		List<Enrolment> toDeleteEnrolmentList = enrolmentRepository.findAllByStudent(toDeleteStudent);

		for (Enrolment e : toDeleteEnrolmentList) {
			enrolmentRepository.delete(e);
		}

		studentRepository.delete(toDeleteStudent);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/filter/first_name/{firstName}/paginate/{page}/{size}")
	public List<Student> paginate(@PathVariable(value = "firstName") String firstName,
			@PathVariable(value = "page") int page, @PathVariable(value = "size") int size) {
		Pageable pageable = PageRequest.of(page, size);

		return studentRepository.findAllByFirstName(firstName, pageable);
	}
}
