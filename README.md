# Common FHIR Java Library

This Java library allows you to dynamically convert Java objects into FHIR (Fast Healthcare Interoperability Resources) format. The conversion is based on a configurable JSON mapping, enabling users to convert their own Java objects into FHIR resources with ease.

The library provides the necessary utilities and code to:
1. Convert any Java object into a FHIR resource using a configurable JSON file.
2. Allow users to use this functionality in their own projects by building the library into a JAR.

## Overview

This project enables dynamic FHIR object creation from Java objects. Users can map their Java class fields to FHIR resource fields via a JSON configuration. The library is designed to be simple to integrate into your project by including the JAR file and providing the required configurations.

## Features

- **Dynamic Object Conversion**: Convert Java objects to FHIR resources based on customizable field mappings defined in a JSON configuration file.
- **Easy Integration**: Once compiled into a JAR, users can integrate the library into any Java project.
- **Custom Field Mappings**: Users can customize the conversion behavior by editing the JSON configuration.
- **Support for Nested Fields**: Maps nested fields from Java objects to FHIR resources recursively.
- **Extensibility**: Extend the library with custom field converters if required for specific object types or complex mappings.

## Prerequisites

To use this library, ensure you have the following:

- Java 8 or higher.
- A working Java project (Maven/Gradle).
- **HAPI FHIR** library (provided as a dependency in this project).  (artifactId: **hapi-fhir-structures-r5**)
- jackson-databind library.
- A valid JSON configuration file that defines how to map your Java fields to FHIR fields.

## Getting Started

### 1. Clone the repository

Clone this repository to your local machine:

```bash```
git clone https://github.com/Youngsoft-India/common-fhir-java-library.git

### 2. Build the project into a JAR

After cloning the repository, navigate to the project directory and build the JAR file:

#### Using Maven:

```bash```
mvn clean install

### 3. Use the JAR in your own project

Once you have the JAR file, add it to your project's dependencies.

### 4. Prepare Your JSON Configuration

Create a JSON configuration file to map your Java object fields to the corresponding FHIR fields. Below is an example of a configuration file (config.json):

```json example : ```

```json
{
    "clientClass": "org.ysifhir.MyPatient4",
    "fhirMapperClass": "Patient",
    "fieldMapping": [
        {
            "from": "fullName",
            "to": {
                "dataType": "HumanName",
                "fieldName": "name",
                "fields": [
                    {
                        "from": "fullName",
                        "to": "text"
                    },
                    {
                        "from": "lastName",
                        "to": "family"
                    }
                ]
            }
        },
        {
            "from": "dateOfBirth",
            "to": {
                "dataType": "DateType",
                "fieldName": "birthDate"
            }
        },
        {
            "from": "gender",
            "to": {
                "dataType": "AdministrativeGender",
                "fieldName": "gender"
            }
        }
    ]
}
```

### 5. Example Java Object

Here’s an example Java class (MyPatient4) that can be converted into a FHIR resource:

```java
public class MyPatient4 {
    private String fullName;
    private String lastName;
    private String dateOfBirth;
    private String gender;
    private String address;
    private String phone;
    private String email;

    // Getters and Setters
}
```

### 6. Use the library in your project

Once the JAR is included in your project, you can use the FHIRConverter class to convert your Java objects into FHIR resources.

```example```

```java
import org.ysifhir.FHIRConverter;
import org.ysifhir.MyPatient4;

import java.nio.file.Files;
import java.nio.file.Paths;

public class MainConverter {
    public static void main(String[] args) throws Exception {

        // Read the JSON configuration from a file
        String jsonConfig = new String(Files.readAllBytes(Paths.get("path_to_your_config_file.json")));

        // Example source object (your Java object)
        MyPatient sourceObj = new MyPatient();
        sourceObj.setFullName("John Doe");
        sourceObj.setLastName("Doe");
        sourceObj.setDateOfBirth("2001-11-26");
        sourceObj.setGender("Male");
        sourceObj.setAddress("123 Main St, Springfield, IL");
        sourceObj.setPhone("123-456-7890");
        sourceObj.setEmail("john.doe@example.com");

        // Convert the Java object to FHIR object
        FHIRConverter converter = new FHIRConverter(jsonConfig);
        String fhirObjJson = converter.toFHIR(sourceObj);

        // Output the resulting FHIR JSON
        System.out.println(fhirObjJson);
    }
}
```

### 7. Customize the Configuration

Edit the JSON configuration file to fit your own Java class fields. Each from field in the configuration corresponds to a field in your Java object, and to specify the FHIR field. Nested fields can be mapped by using the fields array in the configuration.

### 8. Converting Java Object to FHIR

Once the setup is done, you can convert your Java object into a FHIR resource by calling the toFHIR method of the FHIRConverter. The method will return the FHIR resource in JSON format.

### 9. Documentation link : 
[Documentation](https://docs.google.com/document/d/1gygV8kkMBjwDhr1UVIjw7xUrdSlVKpJuYLZAbNQ_Guo/edit?usp=sharing)
