package loopin.projectbook.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import loopin.projectbook.commons.exceptions.IllegalValueException;
import loopin.projectbook.model.ProjectBook;
import loopin.projectbook.model.ReadOnlyProjectBook;
import loopin.projectbook.model.person.Person;
import loopin.projectbook.model.project.Project;

/**
 * An Immutable ProjectBook that is serializable to JSON format.
 */
@JsonRootName(value = "projectbook")
class JsonSerializableProjectBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_PROJECT = "Projects list contains duplicate project(s).";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedProject> projects = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableProjectBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableProjectBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
                                       @JsonProperty("projects") List<JsonAdaptedProject> projects) {
        this.persons.addAll(persons != null ? persons : new ArrayList<>());
        this.projects.addAll(projects != null ? projects : new ArrayList<>());

    }

    /**
     * Converts a given {@code ReadOnlyProjectBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableProjectBook}.
     */
    public JsonSerializableProjectBook(ReadOnlyProjectBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        projects.addAll(source.getProjectList().stream().map(JsonAdaptedProject::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code ProjectBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public ProjectBook toModelType() throws IllegalValueException {
        ProjectBook projectBook = new ProjectBook();
        java.util.Map<String, Person> personsByEmail = new java.util.HashMap<>();
        java.util.Map<String, Project> projectsByName = new java.util.HashMap<>();

        // Add persons
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (projectBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            projectBook.addPerson(person);
            personsByEmail.put(person.getEmail().value, person);
        }

        // Add projects
        for (JsonAdaptedProject jsonAdaptedProject : projects) {
            Project project = jsonAdaptedProject.toModelType();
            if (projectBook.hasProject(project)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PROJECT);
            }
            projectBook.addProject(project);
            projectsByName.put(project.getName().toString(), project);
        }

        // Attach memberships
        for (JsonAdaptedProject jsonAdaptedProject : projects) {
            Project project = projectsByName.get(jsonAdaptedProject.getName());
            if (project == null) {
                continue;
            }
            for (String email : jsonAdaptedProject.getMembers()) {
                Person p = personsByEmail.get(email);
                if (p != null) {
                    if (!project.hasMember(p)) {
                        project.assignPerson(p);
                    }
                }
            }
        }

        return projectBook;
    }

}
