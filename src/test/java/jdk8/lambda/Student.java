package jdk8.lambda;

public class Student {
    
    private int id;
    
    private int age;
    
    private String name;
    
    private String phone;
    
    public Student(int id, int age, String name, String phone) {
        this.id = id;
        this.age = age;
        this.name = name;
        this.phone = phone;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getAge() {
        return age;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    @Override
    public String toString() {
        return "{id=" + id + ", age=" + age + ", name=" + name + ",phone=" + phone + "}";
    }
    
    @Override
    public int hashCode() {
        return id;
    }
    
    @Override
    public boolean equals(Object obj) {
        return id == ((Student) obj).id;
    }
    
}
