package coding.challenge.serializer;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import coding.challenge.model.Student;

public class StudentSerializer extends StdSerializer<Student> {

	private static final long serialVersionUID = 1L;
	
	public StudentSerializer() {
		this(null);
	}

	public StudentSerializer(Class<Student> t) {
		super(t);
	}
	
	@Override
	public void serialize(Student student, JsonGenerator gen, SerializerProvider provider) throws IOException {
		gen.writeStartObject();
		gen.writeNumberField("id", student.getId());
		gen.writeEndObject();
	}
}
