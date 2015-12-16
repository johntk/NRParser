package com.ibm.nrpreprocessor.model;

/** Get all the application names from the properties file */
public class Application {

    private String name;
    private String id;



    public Application() {



    }

    public String getName() {
        return name;
    }

    public void setName(String name) {this.name = name;}

    public String getId() {return id;}

    public void setId(String id) {this.id = id;}
}
