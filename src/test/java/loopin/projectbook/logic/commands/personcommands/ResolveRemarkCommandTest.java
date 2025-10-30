package loopin.projectbook.logic.commands.personcommands;

import static loopin.projectbook.logic.commands.CommandTestUtil.assertCommandFailure;
import static loopin.projectbook.logic.commands.CommandTestUtil.assertCommandSuccess;
import static loopin.projectbook.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static loopin.projectbook.testutil.TypicalPersons.getTypicalProjectBook;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import loopin.projectbook.commons.core.index.Index;
import loopin.projectbook.logic.Messages;
import loopin.projectbook.model.Model;
import loopin.projectbook.model.ModelManager;
import loopin.projectbook.model.UserPrefs;
import loopin.projectbook.model.person.Person;
import loopin.projectbook.model.person.Remark;
import loopin.projectbook.model.person.Remark.Status;

public class ResolveRemarkCommandTest {

    private Model model;
    private Person personWithRemark;
    private Index remarkIndex;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalProjectBook(), new UserPrefs());

        // Find the first person in the filtered list
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        // Ensure the person has a pending remark for testing
        Remark pendingRemark = new Remark("Sample pending task");
        personWithRemark = personToEdit.withNewRemark(pendingRemark);

        model.setPerson(personToEdit, personWithRemark);

        // Get the index of the newly added remark (assumed to be 0 for simplicity)
        remarkIndex = Index.fromZeroBased(0);
    }

    @Test
    public void execute_validIndexes_success() {
        ResolveRemarkCommand resolveCommand = new ResolveRemarkCommand(INDEX_FIRST_PERSON, remarkIndex);

        String expectedMessage = String.format(ResolveRemarkCommand.MESSAGE_RESOLVE_REMARK_SUCCESS,
                personWithRemark.getName().fullName,
                personWithRemark.getRemarks().iterator().next().content);

        Model expectedModel = new ModelManager(model.getProjectBook(), new UserPrefs());

        // Manually apply the change to the expected model for assertion
        Person expectedPerson = expectedModel.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Remark oldRemark = expectedPerson.getRemarks().iterator().next(); // Get the original remark
        Remark resolvedRemark = oldRemark.resolve();
        Person resolvedExpectedPerson = expectedPerson.withResolvedRemark(oldRemark, resolvedRemark);
        expectedModel.setPerson(expectedPerson, resolvedExpectedPerson);

        assertCommandSuccess(resolveCommand, model, expectedMessage, expectedModel);

        // Final check: the remark status in the actual model must be COMPLETED
        Person actualResolvedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        assertTrue(actualResolvedPerson.getRemarks().stream()
                .anyMatch(r -> r.status.equals(Status.COMPLETED)));
    }

    @Test
    public void execute_invalidPersonIndex_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        ResolveRemarkCommand resolveCommand = new ResolveRemarkCommand(outOfBoundIndex, remarkIndex);

        assertCommandFailure(resolveCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidRemarkIndex_failure() {
        Index invalidRemarkIndex = Index.fromOneBased(personWithRemark.getRemarks().size() + 1);
        ResolveRemarkCommand resolveCommand = new ResolveRemarkCommand(INDEX_FIRST_PERSON, invalidRemarkIndex);

        assertCommandFailure(resolveCommand, model, ResolveRemarkCommand.MESSAGE_INVALID_REMARK_DISPLAYED_INDEX);
    }

    @Test
    public void execute_alreadyResolvedRemark_failure() {
        // Resolve the remark once
        Remark initialRemark = personWithRemark.getRemarks().iterator().next();
        Remark resolvedRemark = initialRemark.resolve();
        Person alreadyResolvedPerson = personWithRemark.withResolvedRemark(initialRemark, resolvedRemark);
        model.setPerson(personWithRemark, alreadyResolvedPerson);

        // Try to resolve again
        ResolveRemarkCommand resolveCommand = new ResolveRemarkCommand(INDEX_FIRST_PERSON, remarkIndex);
        assertCommandFailure(resolveCommand, model, ResolveRemarkCommand.MESSAGE_REMARK_ALREADY_RESOLVED);
    }
}
