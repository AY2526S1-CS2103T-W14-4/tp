package loopin.projectbook.model.person;

import java.util.Set;

import loopin.projectbook.model.tag.Tag;

/**
 * Represents a Volunteeer in the project book.
 */
public class Volunteer extends Person {

    /**
     * Name, email and tags must be present and non null but phone can be null.
     */
    public Volunteer(Name name, Phone phone, Email email, Set<Tag> tags) {
        super(name, phone, email, null, tags);
    }

    /**
     * Returns true if both persons are volunteers and have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        } else if (!(otherPerson instanceof Volunteer)) {
            return false;
        }

        return super.isSamePerson(otherPerson);
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        // instanceof handles nulls
        if (!(other instanceof Volunteer)) {
            return false;
        }

        Person otherPerson = (Person) other;
        return super.equals(otherPerson);
    }
}
