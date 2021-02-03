package coding.challenge.controler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.validation.ValidationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import coding.challenge.exception.ResourceNotFoundException;
import coding.challenge.model.Course;
import coding.challenge.model.Enrolment;
import coding.challenge.model.Student;
import coding.challenge.repository.CourseRepository;
import coding.challenge.repository.EnrolmentRepository;
import coding.challenge.repository.StudentRepository;

@RestController
@RequestMapping("/api/enrolments")
public class EnrolmentController {

	@Autowired
	private CourseRepository courseRepository;
	@Autowired
	private EnrolmentRepository enrolmentRepository;
	@Autowired
	private StudentRepository studentRepository;

	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<Enrolment>>> getAll() {
		List<EntityModel<Enrolment>> enrolments = StreamSupport
				.stream(enrolmentRepository.findAll().spliterator(), false)
				.map(enrolment -> EntityModel.of(enrolment,
						linkTo(methodOn(EnrolmentController.class).getEnrolmentById(enrolment.getId())).withSelfRel(),
						linkTo(methodOn(EnrolmentController.class).getAll()).withRel("enrolments")))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(enrolments, linkTo(methodOn(EnrolmentController.class).getAll()).withSelfRel()));
	}

	@GetMapping("{id}")
	public ResponseEntity<EntityModel<Enrolment>> getEnrolmentById(@PathVariable(value = "id") Long enrolmentId) {
		return enrolmentRepository.findById(enrolmentId)
				.map(enrolment -> EntityModel.of(enrolment,
						linkTo(methodOn(EnrolmentController.class).getEnrolmentById(enrolment.getId())).withSelfRel(),
						linkTo(methodOn(EnrolmentController.class).getAll()).withRel("enrolments")))
				.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/course/{courseId}")
	public ResponseEntity<CollectionModel<EntityModel<Enrolment>>> getAllEnrolmentsByCourse(
			@PathVariable(value = "courseId") Long courseId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResourceNotFoundException("Course with id " + courseId + " not found"));

		List<EntityModel<Enrolment>> enrolments = StreamSupport
				.stream(enrolmentRepository.findAllByCourse(course).spliterator(), false)
				.map(enrolment -> EntityModel.of(enrolment,
						linkTo(methodOn(EnrolmentController.class).getEnrolmentById(enrolment.getId())).withSelfRel(),
						linkTo(methodOn(EnrolmentController.class).getAll()).withRel("enrolments")))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(enrolments, linkTo(methodOn(EnrolmentController.class).getAll()).withSelfRel()));
	}

	@GetMapping("/student/{studentId}")
	public ResponseEntity<CollectionModel<EntityModel<Enrolment>>> getAllEnrolmentsByStudent(
			@PathVariable(value = "studentId") Long studentId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " not found"));

		List<EntityModel<Enrolment>> enrolments = StreamSupport
				.stream(enrolmentRepository.findAllByStudent(student).spliterator(), false)
				.map(enrolment -> EntityModel.of(enrolment,
						linkTo(methodOn(EnrolmentController.class).getEnrolmentById(enrolment.getId())).withSelfRel(),
						linkTo(methodOn(EnrolmentController.class).getAll()).withRel("enrolments")))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(enrolments, linkTo(methodOn(EnrolmentController.class).getAll()).withSelfRel()));
	}

	@PostMapping("/{courseId}/{studentId}")
	public ResponseEntity<?> enroll(@PathVariable(value = "courseId") Long courseId,
			@PathVariable(value = "studentId") Long studentId) {
		Course course = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResourceNotFoundException("Course with id " + courseId + " not found"));

		if (course.getStartDate().isAfter(LocalDate.now())) {
			throw new ValidationException("Course with id " + studentId + " already started at "
					+ course.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		}

		Student toEnrollStudent = studentRepository.findById(studentId)
				.orElseThrow(() -> new ResourceNotFoundException("Student with id " + studentId + " not found"));

		enrolmentRepository.findByCourseAndStudent(courseId, studentId).ifPresent(s -> {
			throw new ValidationException(
					"Student with id " + studentId + " already enrolled to course with id " + courseId);
		});

		Enrolment enrolment = new Enrolment(course, toEnrollStudent);

		enrolmentRepository.save(enrolment);

		EntityModel<Enrolment> enrolmentResource = EntityModel.of(enrolment,
				linkTo(methodOn(EnrolmentController.class).getEnrolmentById(enrolment.getId())).withSelfRel());

		try {
			return ResponseEntity.created(new URI(enrolmentResource.getRequiredLink(IanaLinkRelations.SELF).getHref()))
					.body(enrolmentResource);
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to build response for create enrolment with course id "
					+ courseId + " and student id " + studentId);
		}
	}
}
