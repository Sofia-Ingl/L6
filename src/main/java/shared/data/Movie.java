package shared.data;

import com.google.gson.annotations.Expose;
import java.time.ZonedDateTime;

/**
 * Фильм.
 */
public class Movie implements Comparable<Movie> {

    @Expose
    private int id; //Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    @Expose
    private String name; //Поле не может быть null, Строка не может быть пустой
    @Expose
    private Coordinates coordinates; //Поле не может быть null

    private ZonedDateTime creationDate;//Поле не может быть null, Значение этого поля должно генерироваться автоматически
    @Expose
    private int oscarsCount; //Значение поля должно быть больше 0
    @Expose
    private long goldenPalmCount; //Значение поля должно быть больше 0
    @Expose
    private String tagline; //Поле не может быть null
    @Expose
    private MovieGenre genre; //Поле не может быть null
    @Expose
    private Person screenwriter;

    public Movie(String name, Coordinates coordinates, int oscars, long goldenPalmCount, String tags, MovieGenre genre, Person screenwriter) {
        this.name = name;
        this.coordinates = coordinates;
        this.oscarsCount = oscars;
        this.goldenPalmCount = goldenPalmCount;
        this.tagline = tags;
        this.genre = genre;
        this.screenwriter = screenwriter;
    }


    public int getId() {
        return id;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public Person getScreenwriter() {
        return screenwriter;
    }

    public long getGoldenPalmCount() {
        return goldenPalmCount;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public int getOscarsCount() {
        return oscarsCount;
    }

    public MovieGenre getGenre() {
        return genre;
    }

    public String getTagline() {
        return tagline;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public void setGenre(MovieGenre genre) {
        this.genre = genre;
    }

    public void setGoldenPalmCount(long goldenPalmCount) {
        this.goldenPalmCount = goldenPalmCount;
    }

    public void setOscarsCount(int oscarsCount) {
        this.oscarsCount = oscarsCount;
    }

    public void setScreenwriter(Person screenwriter) {
        this.screenwriter = screenwriter;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    @Override
    public int compareTo(Movie m) {
        int result = oscarsCount - m.oscarsCount;
        if (result == 0) {
            result = name.compareTo(m.name);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates={" + coordinates.getX() + ", " + coordinates.getY() + "}" +
                ", creationDate=" + creationDate +
                ", oscarsCount=" + oscarsCount +
                ", goldenPalmCount=" + goldenPalmCount +
                ", tagline='" + tagline + '\'' +
                ", genre=" + genre +
                ", screenwriter=" + screenwriter +
                '}';
    }
}
