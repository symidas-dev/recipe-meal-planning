package hr.algebra.recipemealplanning.domain.exception;

public class CircularDependencyException extends RuntimeException {

    public CircularDependencyException(String message) {
        super(message);
    }
}
