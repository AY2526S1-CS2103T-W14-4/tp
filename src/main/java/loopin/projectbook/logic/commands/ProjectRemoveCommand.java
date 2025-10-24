package loopin.projectbook.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import loopin.projectbook.commons.core.index.Index;
import loopin.projectbook.logic.commands.exceptions.CommandException;
import loopin.projectbook.model.Model;
import loopin.projectbook.model.person.Person;
import loopin.projectbook.model.project.Project;
import loopin.projectbook.model.project.ProjectName;

/**
 * Removes a person (by index in the current person list) from an existing project.
 * Usage:
 * {@code project remove INDEX project/PROJECT_NAME}
 * {@code project remove n/NAME project/PROJECT_NAME}
 */
public final class ProjectRemoveCommand extends Command {
    public static final String COMMAND_WORD = "project";
    public static final String SUBCOMMAND = "remove";
    public static final String MESSAGE_USAGE =
            "project remove: Remove a person from a project.\n"
                    + "Formats:\n"
                    + "project remove INDEX project/PROJECT_NAME\n"
                    + "project remove n/NAME project/PROJECT_NAME\n"
                    + "Examples:\n"
                    + "project remove 1 project/MyProject\n"
                    + "project remove n/Alex Tan project/MyProject";

    public static final String MESSAGE_SUCCESS = "Removed %s from the project %s.";
    public static final String MESSAGE_NOT_IN = "%s is not in this project.";
    public static final String MESSAGE_NO_PROJECT = "Project '%s' does not exist.";
    public static final String MESSAGE_INVALID_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_NOT_FOUND_BY_NAME = "No person found with the name %s.";
    public static final String MESSAGE_AMBIGUOUS_NAME = "Multiple people share the name %s.\n"
            + "Please use index-based assignment instead.";

    private final Index index;
    private final String name;
    private final ProjectName projectName;

    /**
     * Constructs a command to remove the person at {@code index} from the project named {@code projectName}.
     *
     * @param index index of the person in the current filtered person list
     * @param projectName the validated project name value object
     */
    public ProjectRemoveCommand(Index index, ProjectName projectName) {
        this.index = index;
        this.name = null;
        this.projectName = projectName;
    }

    /**
     * Constructs a command to remove the person called {@code name} from the project named {@code projectName}.
     *
     * @param name name of the person
     * @param projectName the validated project name value object
     */
    public ProjectRemoveCommand(String name, ProjectName projectName) {
        requireNonNull(name);
        this.index = null;
        this.name = name;
        this.projectName = projectName;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        String lookup = projectName.toString();
        Project project = model.findProjectByName(lookup)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NO_PROJECT, lookup)));

        final Person target;
        if (name != null) {
            target = resolveByName(model, name);
        } else {
            List<Person> lastShown = model.getFilteredPersonList();
            if (index.getZeroBased() >= lastShown.size()) {
                throw new CommandException(MESSAGE_INVALID_INDEX);
            }
            target = lastShown.get(index.getZeroBased());
        }

        if (!project.hasMember(target)) {
            throw new CommandException(String.format(MESSAGE_NOT_IN, target.getName()));
        }

        project.removePerson(target);
        model.setProject(project);

        return new CommandResult(String.format(MESSAGE_SUCCESS, target.getName(), projectName));
    }

    /**
     * Returns a Person whose name matches the specified {@code name}, using a case-insensitive exact match.
     *
     * @param model the Model containing the list of persons to search through
     * @param name the name of the person to look up
     * @return the unique Person whose name matches the given {@code name}
     * @throws CommandException if no matching person is found or if multiple persons share the same name
     */
    private Person resolveByName(Model model, String name) throws CommandException {
        String trimmedLowerName = name.trim().toLowerCase(Locale.ROOT);

        List<Person> allPersons = model.getProjectBook().getPersonList();
        List<Person> matches = new ArrayList<>();
        System.out.println(allPersons);

        for (Person p : allPersons) {
            String candidate = p.getName().toString();
            if (candidate != null && candidate.trim().toLowerCase(Locale.ROOT).equals(trimmedLowerName)) {
                matches.add(p);
            }
        }

        if (matches.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NOT_FOUND_BY_NAME, trimmedLowerName));
        }

        if (matches.size() > 1) {
            throw new CommandException(String.format(MESSAGE_AMBIGUOUS_NAME, trimmedLowerName));
        }

        return matches.get(0);

    }
}