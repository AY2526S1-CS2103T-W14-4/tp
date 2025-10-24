package loopin.projectbook.logic.parser;

import static loopin.projectbook.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static loopin.projectbook.logic.parser.CliSyntax.PREFIX_NAME;
import static loopin.projectbook.logic.parser.CliSyntax.PREFIX_PROJECT;

import java.util.Optional;

import loopin.projectbook.commons.core.index.Index;
import loopin.projectbook.logic.commands.ProjectAssignCommand;
import loopin.projectbook.logic.parser.exceptions.ParseException;
import loopin.projectbook.model.project.ProjectName;

/**
 * Parses input arguments and creates a new {@link ProjectAssignCommand} object.
 *
 * Supported formats:
 * {@code INDEX project/PROJECT_NAME}
 * {@code n/NAME project/PROJECT_NAME}
 */
public final class ProjectAssignCommandParser implements Parser<ProjectAssignCommand> {

    @Override
    public ProjectAssignCommand parse(String args) throws ParseException {
        final String normalized = " " + args.trim();
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalized, PREFIX_NAME, PREFIX_PROJECT);

        if (args.trim().startsWith(PREFIX_NAME.getPrefix())) {
            //name-based assignment
            String nameValue = argMultimap.getValue(PREFIX_NAME)
                    .orElseThrow(() -> new ParseException(ProjectAssignCommand.MESSAGE_USAGE));
            ProjectName projectName = argMultimap.getValue(PREFIX_PROJECT)
                    .map(n -> {
                        try {
                            return ParserUtil.parseProjectName(n);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new ParseException(ProjectAssignCommand.MESSAGE_USAGE));

            return new ProjectAssignCommand(nameValue.trim(), projectName);
        }

        String preamble = argMultimap.getPreamble().trim();
        ProjectName projectName = argMultimap.getValue(PREFIX_PROJECT)
                .map(n -> {
                    try {
                        return ParserUtil.parseProjectName(n);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseThrow(() -> new ParseException(ProjectAssignCommand.MESSAGE_USAGE));

        if (!preamble.isEmpty()) {
            // index-based assignment
            Index index = ParserUtil.parseIndex(preamble);
            return new ProjectAssignCommand(index, projectName);
        }

        Index index = ParserUtil.parseIndex(preamble);
        return new ProjectAssignCommand(index, projectName);
    }
}
