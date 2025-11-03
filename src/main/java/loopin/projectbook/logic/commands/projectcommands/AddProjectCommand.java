package loopin.projectbook.logic.commands.projectcommands;

import static java.util.Objects.requireNonNull;
import static loopin.projectbook.logic.parser.CliSyntax.PREFIX_DESCRIPTION;
import static loopin.projectbook.logic.parser.CliSyntax.PREFIX_NAME;

import loopin.projectbook.commons.util.ToStringBuilder;
import loopin.projectbook.logic.Messages;
import loopin.projectbook.logic.commands.Command;
import loopin.projectbook.logic.commands.CommandResult;
import loopin.projectbook.logic.commands.exceptions.CommandException;
import loopin.projectbook.model.Model;
import loopin.projectbook.model.project.Project;
/**
 * Adds a {@code Project} to the project book.
 * The {@code AddProjectCommand} encapsulates the logic for creating and adding
 * a new project with the specified details (name and description) into the project book.
 * If a project with the same identity already exists, a {@code CommandException} is thrown.
 */
public class AddProjectCommand extends Command {
    public static final String COMMAND_WORD = "project";
    public static final String SUBCOMMAND = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " "
            + SUBCOMMAND + ": Adds a project to the project book. \n"
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_DESCRIPTION + "DESCRIPTION "
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "Beach Cleanup "
            + PREFIX_DESCRIPTION + "Annual cleanup at East Coast ";

    public static final String MESSAGE_SUCCESS = "New project added: %1$s";
    public static final String MESSAGE_DUPLICATE_PROJECT = "This project already exists in the project book";

    private final Project toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Project}
     */
    public AddProjectCommand(Project project) {
        requireNonNull(project);
        toAdd = project;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasProject(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PROJECT);
        }

        model.addProject(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatProject(toAdd)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddProjectCommand)) {
            return false;
        }

        AddProjectCommand otherAddProjectCommand = (AddProjectCommand) other;
        return toAdd.equals(otherAddProjectCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
