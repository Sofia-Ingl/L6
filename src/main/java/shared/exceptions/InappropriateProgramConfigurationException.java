package shared.exceptions;

/**
 * Исключение, которое бросается, если логика программы не соответсвует изначальной задумке (не соблюдаюся необходимые связи между компонентами программы).
 *
 */
public class InappropriateProgramConfigurationException extends RuntimeException {

    @Override
    public String getMessage() {
        return "Какие-то из составных частей программы не были должным образом связаны и/или инициализированы.\nРекомендуется проверить логику построения кода в main";
    }
}
