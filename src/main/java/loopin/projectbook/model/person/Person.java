package loopin.projectbook.model.person;

import static loopin.projectbook.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import loopin.projectbook.commons.core.LogsCenter;
import loopin.projectbook.commons.util.ToStringBuilder;
import loopin.projectbook.model.project.Project;
import loopin.projectbook.model.tag.Tag;

/**
 * Represents a Person in the project book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    /**
     * Logger for Person class
     */
    private static final Logger logger = LogsCenter.getLogger(Person.class);

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;
    private final Telegram telegram;

    // Data fields
    private final Set<Tag> tags = new HashSet<>();
    private final List<Remark> remarks = new ArrayList<>(); // empty by default. TODO: change implementation
    private List<Project> projects = new ArrayList<>(); //list of projects the person is part of, empty by default

    /**
     * Name, email and tags must be present and non null but phone and telegram can be null.
     */
    public Person(Name name, Phone phone, Email email, Telegram telegram, Set<Tag> tags) {
        requireAllNonNull(name, email, tags);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.telegram = telegram;
        this.tags.addAll(tags);
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Telegram getTelegram() {
        return telegram;
    }

    public String getRole() {
        return "Unknown Role";
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same phone, email or telegram.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        } else if (otherPerson == null) {
            return false;
        }

        boolean isSamePhone = otherPerson.getPhone().equals(getPhone());
        boolean isSameEmail = otherPerson.getEmail().equals(getEmail());
        boolean isSameTelegram = otherPerson.getTelegram().equals(getTelegram());

        return isSamePhone || isSameEmail || isSameTelegram;
    }

    // Add this method for duplicate checking in RemarkCommand
    public boolean hasRemark(Remark remark) {
        return remarks.contains(remark); // leverages UpdateRemark's equals()
    }

    // Add this method for adding a remark (returns a new immutable Person)
    /**
     * Returns a new immutable Person with a remark.
     */
    public Person withNewRemark(Remark newRemark) {
        // Implementation must create a copy of the existing person and add the new remark.
        Person updatedPerson = new Person(name, phone, email, telegram, tags);
        updatedPerson.remarks.addAll(this.remarks);
        updatedPerson.remarks.add(newRemark);
        return updatedPerson;
    }

    /**
     * Adds a project to the list of projects a person is part of
     * @param p project to be added
     */
    public void addProject(Project p) {
        if (this.projects.contains(p)) {
            throw new IllegalStateException("Person is already in that project");
        }
        this.projects.add(p);
        logger.fine("Project added to person.");
    }

    public int getNumberOfProjects() {
        return this.projects.size();
    }

    // Add a getter for the UI
    public List<Remark> getRemarks() {
        return Collections.unmodifiableList(remarks);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person)) {
            return false;
        }

        Person otherPerson = (Person) other;
        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && telegram.equals(otherPerson.telegram)
                && tags.equals(otherPerson.tags);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, telegram, tags);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("telegram", telegram)
                .add("tags", tags)
                .toString();
    }

}
