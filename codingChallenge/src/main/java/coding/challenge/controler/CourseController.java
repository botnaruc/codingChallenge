package coding.challenge.controler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import coding.challenge.exception.ResourceNotFoundException;
import coding.challenge.model.Course;
import coding.challenge.model.Enrolment;
import coding.challenge.model.Teacher;
import coding.challenge.repository.CourseRepository;
import coding.challenge.repository.EnrolmentRepository;
import coding.challenge.repository.TeacherRepository;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

	@Autowired
	private CourseRepository courseRepository;
	@Autowired
	private TeacherRepository teacherRepository;
	@Autowired
	private EnrolmentRepository enrolmentRepository;

	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<Course>>> getAll() {
		List<EntityModel<Course>> courses = StreamSupport.stream(courseRepository.findAll().spliterator(), false)
				.map(course -> EntityModel.of(course,
						linkTo(methodOn(CourseController.class).getCourseById(course.getId())).withSelfRel(),
						linkTo(methodOn(CourseController.class).getAll()).withRel("courses")))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(courses, linkTo(methodOn(CourseController.class).getAll()).withSelfRel()));
	}

	@GetMapping("{id}")
	public ResponseEntity<EntityModel<Course>> getCourseById(@PathVariable(value = "id") Long courseId) {
		return courseRepository.findById(courseId)
				.map(course -> EntityModel.of(course,
						linkTo(methodOn(CourseController.class).getCourseById(course.getId())).withSelfRel(),
						linkTo(methodOn(CourseController.class).getAll()).withRel("courses"),
						linkTo(methodOn(CourseController.class).getTeacherByCourse(course.getId())).withRel("teacher")))
				.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/teacher/{teacherId}")
	public ResponseEntity<CollectionModel<EntityModel<Course>>> getAllCoursesByTeacher(
			@PathVariable(value = "teacherId") Long teacherId) {
		Teacher teacher = teacherRepository.findById(teacherId)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher with id " + teacherId + " not found"));

		List<EntityModel<Course>> courses = StreamSupport
				.stream(courseRepository.findAllByTeacher(teacher).spliterator(), false)
				.map(course -> EntityModel.of(course,
						linkTo(methodOn(CourseController.class).getCourseById(course.getId())).withSelfRel(),
						linkTo(methodOn(CourseController.class).getAll()).withRel("courses")))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(courses, linkTo(methodOn(CourseController.class).getAll()).withSelfRel()));
	}

	@GetMapping("{id}/teacher")
	public ResponseEntity<EntityModel<Teacher>> getTeacherByCourse(@PathVariable(value = "id") Long courseId) {
//		avoid query -> causes teacher proxy issue return for teacherRepository.findByCourseId(courseId)
//		details: https://stackoverflow.com/questions/52656517/no-serializer-found-for-class-org-hibernate-proxy-pojo-bytebuddy-bytebuddyinterc
//		Course course = courseRepository.findById(courseId)
//				.orElseThrow(() -> new ResourceNotFoundException("Course with id " + courseId + " not found"));
		
		boolean exists = courseRepository.existsById(courseId);
		
		if(!exists) {
			throw new ResourceNotFoundException("Course with id " + courseId + " not found");
		}
		
		return teacherRepository.findByCourseId(courseId)
				.map(teacher -> EntityModel.of(teacher,
						linkTo(methodOn(CourseController.class).getTeacherByCourse(teacher.getId())).withSelfRel()))
				.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PutMapping("{id}/teacher/{teacherId}")
	public ResponseEntity<?> updateCourseTeacher(@PathVariable(value = "id") Long courseId,
			@PathVariable(value = "teacherId") Long teacherId) {
		Course toUpdateCourse = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResourceNotFoundException("Course with id " + courseId + " not found"));
		Teacher teacher = teacherRepository.findById(teacherId)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher with id " + teacherId + " not found"));

		toUpdateCourse.setTeacher(teacher);

		courseRepository.save(toUpdateCourse);

		Link link = linkTo(methodOn(CourseController.class).getTeacherByCourse(courseId)).withSelfRel();

		try {
			return ResponseEntity.noContent().location(new URI(link.getHref())).build();
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body(
					"Unable to build response for update course id " + courseId + " with teacher id " + teacherId);
		}
	}

	@PostMapping
	public ResponseEntity<?> createCourse(@Valid @RequestBody Course course) {
		course = courseRepository.save(course);

		EntityModel<Course> courseResource = EntityModel.of(course,
				linkTo(methodOn(CourseController.class).getCourseById(course.getId())).withSelfRel());

		try {
			return ResponseEntity.created(new URI(courseResource.getRequiredLink(IanaLinkRelations.SELF).getHref()))
					.body(courseResource);
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to build response for create " + course);
		}
	}

	@PutMapping("{id}")
	public ResponseEntity<?> updateCourse(@PathVariable(value = "id") Long courseId,
			@Valid @RequestBody Course course) {
		Course toUpdateCourse = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResourceNotFoundException("Course with id " + courseId + " not found"));

		toUpdateCourse.setName(course.getName());
		toUpdateCourse.setDescription(course.getDescription());
		toUpdateCourse.setPrice(course.getPrice());
		toUpdateCourse.setStartDate(course.getStartDate());
		toUpdateCourse.setEndDate(course.getEndDate());

		courseRepository.save(toUpdateCourse);

		Link link = linkTo(methodOn(CourseController.class).getCourseById(courseId)).withSelfRel();

		try {
			return ResponseEntity.noContent().location(new URI(link.getHref())).build();
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to build response for update " + course);
		}
	}

	@DeleteMapping("{id}")
	public ResponseEntity<?> deleteCourse(@PathVariable(value = "id") Long courseId) {
		Course toDeleteCourse = courseRepository.findById(courseId)
				.orElseThrow(() -> new ResourceNotFoundException("Course with id " + courseId + " not found"));

		List<Enrolment> enrolmentToDeleteList = enrolmentRepository.findAllByCourse(toDeleteCourse);

		for (Enrolment e : enrolmentToDeleteList) {
			enrolmentRepository.delete(e);
		}

		courseRepository.delete(toDeleteCourse);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/best_sold")
	public ResponseEntity<CollectionModel<EntityModel<Course>>> getBestSoldByYearAndMonth(
			@RequestParam(name = "year") Integer year, @RequestParam(name = "month") Integer month) {
		Pageable pageable = PageRequest.of(0, 3);

		List<Object[]> courseIdCountArray = courseRepository.findByDateYearAndMonth(year, month, pageable);

		List<Course> list = new ArrayList<>();
		for (Object[] o : courseIdCountArray) {
			Course courseId = (Course) o[0];
			list.add(courseId);
		}

		List<EntityModel<Course>> courses = StreamSupport.stream(list.spliterator(), false)
				.map(course -> EntityModel.of(course,
						linkTo(methodOn(CourseController.class).getCourseById(course.getId())).withSelfRel()))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(courses, linkTo(methodOn(CourseController.class).getAll()).withSelfRel()));
	}
}
