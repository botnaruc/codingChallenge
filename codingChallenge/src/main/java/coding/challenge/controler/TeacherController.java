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
import coding.challenge.model.Course;
import coding.challenge.model.Teacher;
import coding.challenge.repository.CourseRepository;
import coding.challenge.repository.TeacherRepository;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

	@Autowired
	private TeacherRepository teacherRepository;
	@Autowired
	private CourseRepository courseRepository;

	@GetMapping
	public ResponseEntity<CollectionModel<EntityModel<Teacher>>> getAll() {
		List<EntityModel<Teacher>> teachers = StreamSupport.stream(teacherRepository.findAll().spliterator(), false)
				.map(teacher -> EntityModel.of(teacher,
						linkTo(methodOn(TeacherController.class).getTeacherById(teacher.getId())).withSelfRel(),
						linkTo(methodOn(TeacherController.class).getAll()).withRel("teachers")))
				.collect(Collectors.toList());

		return ResponseEntity
				.ok(CollectionModel.of(teachers, linkTo(methodOn(CourseController.class).getAll()).withSelfRel()));
	}

	@GetMapping("{id}")
	public ResponseEntity<EntityModel<Teacher>> getTeacherById(@PathVariable(value = "id") Long teacherId) {
		return teacherRepository.findById(teacherId)
				.map(teacher -> EntityModel.of(teacher,
						linkTo(methodOn(TeacherController.class).getTeacherById(teacher.getId())).withSelfRel(),
						linkTo(methodOn(TeacherController.class).getAll()).withRel("teachers")))
				.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<?> createTeacher(@Valid @RequestBody Teacher teacher) {
		teacher = teacherRepository.save(teacher);

		EntityModel<Teacher> teacherResource = EntityModel.of(teacher,
				linkTo(methodOn(TeacherController.class).getTeacherById(teacher.getId())).withSelfRel());

		try {
			return ResponseEntity.created(new URI(teacherResource.getRequiredLink(IanaLinkRelations.SELF).getHref()))
					.body(teacherResource);
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to build response for create " + teacher);
		}
	}

	@PutMapping("{id}")
	public ResponseEntity<?> updateTeacher(@PathVariable(value = "id") Long teacherId,
			@Valid @RequestBody Teacher teacher) {
		Teacher toUpdateTeacher = teacherRepository.findById(teacherId)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher with id " + teacherId + " not found"));

		toUpdateTeacher.setFirstName(teacher.getFirstName());
		toUpdateTeacher.setLastName(teacher.getLastName());
		toUpdateTeacher.setBirthDate(teacher.getBirthDate());

		teacherRepository.save(toUpdateTeacher);

		Link link = linkTo(methodOn(TeacherController.class).getTeacherById(teacherId)).withSelfRel();

		try {
			return ResponseEntity.noContent().location(new URI(link.getHref())).build();
		} catch (URISyntaxException e) {
			return ResponseEntity.badRequest().body("Unable to build response for update " + teacher);
		}
	}

	@DeleteMapping("{id}")
	public ResponseEntity<?> deleteTeacher(@PathVariable(value = "id") Long teacherId) {
		Teacher toDeleteTeacher = teacherRepository.findById(teacherId)
				.orElseThrow(() -> new ResourceNotFoundException("Teacher with id " + teacherId + " not found"));

		List<Course> toUpdateCourseList = courseRepository.findAllByTeacher(toDeleteTeacher);

		for (Course c : toUpdateCourseList) {
			c.setTeacher(null);
			courseRepository.save(c);
		}

		teacherRepository.delete(toDeleteTeacher);

		return ResponseEntity.noContent().build();
	}
}
