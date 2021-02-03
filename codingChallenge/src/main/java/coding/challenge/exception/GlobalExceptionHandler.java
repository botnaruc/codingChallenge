package coding.challenge.exception;

import java.util.Date;
import java.util.stream.Collectors;

import javax.validation.ValidationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<?> handleResourseNotFoundException(ResourceNotFoundException rnfe, WebRequest wr) {
		ErrorDetails ed = new ErrorDetails(new Date(), rnfe.getMessage(), wr.getDescription(false));
		
		return new ResponseEntity<>(ed, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<?> handleValidationLogicException(ValidationException ve, WebRequest wr) {
		ErrorDetails ed = new ErrorDetails(new Date(), ve.getMessage(), wr.getDescription(false));
		
		return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationLogicException(MethodArgumentNotValidException ve, WebRequest wr) {
		ErrorDetails ed = new ErrorDetails(new Date(), ve.getBindingResult().getFieldErrors().stream().map(err -> err.getDefaultMessage()).collect(Collectors.joining(", ")), wr.getDescription(false));
		
		return new ResponseEntity<>(ed, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(ResourceNotFoundException rnfe, WebRequest wr) {
		ErrorDetails ed = new ErrorDetails(new Date(), rnfe.getMessage(), wr.getDescription(false));
		
		return new ResponseEntity<>(ed, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
