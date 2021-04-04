package shared.exceptions;

import java.io.OutputStream;
import java.util.Scanner;

/**
 * Исключение, которое бросается, если в скрипте есть рекурсия.
 *
 */
public class ScriptRecursionException extends RuntimeException {

    private final Scanner lostScanner;
    private final OutputStream lostOut;

    public ScriptRecursionException(Scanner scanner, OutputStream outputStream) {
        lostOut = outputStream;
        lostScanner = scanner;
    }
    @Override
    public String getMessage() {
        return "В скрипте присутствует рекурсия!";
    }

    public OutputStream getLostOut() {
        return lostOut;
    }

    public Scanner getLostScanner() {
        return lostScanner;
    }
}
