package shared.data;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Человек.
 */
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    @Expose
    private String name; //Поле не может быть null, Строка не может быть пустой
    @Expose
    private Long height; //Поле не может быть null, Значение поля должно быть больше 0
    @Expose
    private Color eyeColor; //Поле может быть null
    @Expose
    private Country nationality; //Поле может быть null

    public Person(String name, Long height, Color eyeColor, Country nationality) {
        this.name = name;
        this.height = height;
        this.eyeColor = eyeColor;
        this.nationality = nationality;
    }

    public String getName() {
        return name;
    }

    public Long getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", height=" + height +
                ", eyeColor=" + eyeColor +
                ", nationality=" + nationality +
                '}';
    }
}
