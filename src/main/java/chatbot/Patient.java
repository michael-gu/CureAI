package chatbot;


public class Patient {

    private String name;
    private String age;
    private String symptoms;
    private String diagnosis;

    public Patient() {
        this.name = "NONE";
        this.age = "NONE";
        this.symptoms = "NONE";
        this.diagnosis = "N/A";
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String toString() {
        return "Name: " + name + "\n" + "Age: " + age + "\n" + "Symptoms: " + symptoms;
    }
}
