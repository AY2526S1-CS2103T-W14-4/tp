package loopin.projectbook.storage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import loopin.projectbook.commons.exceptions.IllegalValueException;
import loopin.projectbook.model.person.Person;
import loopin.projectbook.model.project.Description;
import loopin.projectbook.model.project.Project;
import loopin.projectbook.model.project.ProjectName;

/**
 * Jackson-friendly version of {@link Project}.
 */
class JsonAdaptedProject {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Project's %s field is missing!";

    private final String name;
    private final String description;
    private final List<String> members = new ArrayList<>();
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Constructs a {@code JsonAdaptedProject} with the given project details.
     */
    @JsonCreator
    public JsonAdaptedProject(@JsonProperty("name") String name,
                              @JsonProperty("description") String description,
                              @JsonProperty("members") List<String> members,
                              @JsonProperty("createdAt") LocalDateTime createdAt,
                              @JsonProperty("updatedAt") LocalDateTime updatedAt) {
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

        if (members != null) {
            this.members.addAll(members);
        }
    }

    /**
     * Converts a given {@code Project} into this class for Jackson use.
     */
    public JsonAdaptedProject(Project source) {
        name = source.getName().toString();
        description = source.getDescription().toString();
        createdAt = source.getCreatedAt();
        updatedAt = source.getUpdatedAt();

        for (Person p : source.getAllPeople()) {
            this.members.add(p.getEmail().value);
        }
    }

    /**
     * Converts this Jackson-friendly adapted project object into the model's {@code Project} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted project.
     */
    public Project toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ProjectName.class.getSimpleName()));
        }
        if (!ProjectName.isValidProjectName(name)) {
            throw new IllegalValueException(ProjectName.MESSAGE_CONSTRAINTS);
        }
        final ProjectName modelName = new ProjectName(name);

        if (description == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Description.class.getSimpleName()));
        }
        if (!Description.isValidDescription(description)) {
            throw new IllegalValueException(Description.MESSAGE_CONSTRAINTS);
        }
        final Description modelDescription = new Description(description);

        return new Project(modelName, modelDescription);
    }

    public String getName() { return name; }
    public List<String> getMembers() { return members; }
}
