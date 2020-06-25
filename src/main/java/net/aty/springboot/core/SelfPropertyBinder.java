package net.aty.springboot.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePropertySource;

public class SelfPropertyBinder {

    public static class Address {
        private String country;
        private String province;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }
    }

    public static class Person {
        private int id;
        private String name;
        private Address address;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }

    public static void main(String[] args) throws Exception {
        Resource resource = new ClassPathResource("bind.properties");
        ResourcePropertySource resourcePropertySource = new ResourcePropertySource(resource);

        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addLast(resourcePropertySource);

        Binder binder = Binder.get(environment);
        Person person = binder.bind("aty.person", Bindable.of(Person.class)).get();
        // jackson
        System.out.println(new ObjectMapper().writeValueAsString(person));
    }
}
