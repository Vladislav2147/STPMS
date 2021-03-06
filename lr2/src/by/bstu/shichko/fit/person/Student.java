package by.bstu.shichko.fit.person;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class Student extends Person {

    private University university;
    private Integer mark;

    public Student(String firstName, String secondName, int age, University university, Integer mark) {
        super(firstName, secondName, age);
        this.university = university;
        this.mark = mark;
    }

}
