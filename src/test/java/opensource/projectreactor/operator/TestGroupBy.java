package opensource.projectreactor.operator;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;


public class TestGroupBy {

    private static class Person {
        private String name;

        private int gender;

        public Person(String name, int gender) {
            this.name = name;
            this.gender = gender;
        }

        public String getName() {
            return name;
        }

        public int getGender() {
            return gender;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", gender=" + gender +
                    '}';
        }
    }

    @Test
    public void groupBy() {
        Flux<Person> personFlux = Flux.just(
                new Person("A", 0),
                new Person("B", 0),
                new Person("c", 1),
                new Person("d", 1),
                new Person("X", 2));

        Flux<GroupedFlux<Integer, Person>> groups = personFlux.groupBy(Person::getGender);
        groups.subscribe(groupedFlux -> {
            Integer gender = groupedFlux.key();
            groupedFlux.subscribe(e -> System.out.println("key=" + gender + ",value=" + e));
        });
    }
}
