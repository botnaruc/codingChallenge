package coding.challenge.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import coding.challenge.model.Course;

public class CourseSerializer extends StdSerializer<Course> {

	private static final long serialVersionUID = 1L;
	
	public CourseSerializer() {
		this(null);
	}

	public CourseSerializer(Class<Course> t) {
		super(t);
	}
	
	@Override
	public void serialize(Course course, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField("id", course.getId());
		gen.writeEndObject();
	}
}
