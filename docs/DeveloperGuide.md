---
layout: default.md
title: "Developer Guide"
pageNav: 3
---

# Loopin Developer Guide

<page-nav-print />

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This project was adapted from the [AddressBook Level 3 (AB3)](https://se-education.org/addressbook-level3/) project
created by [SE-EDU initiative](https://se-education.org/)

* [JavaFX](https://wiki.openjdk.org/display/OpenJFX) is used to build the UI
* [JUnit](https://github.com/junit-team/junit5) is used for automated testing
* [PlantUML](https://plantuml.com/) is used to generate the UML diagrams in this document
* [Gradle](https://gradle.org/) is used to automate building

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

### Architecture

<puml src="diagrams/ArchitectureDiagram.puml" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

Given below is a quick overview of main components and how they interact with each other.

**Main components of the architecture**

**`Main`** (consisting of classes [
`Main`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/java/loopin/projectbook/Main.java) and [
`MainApp`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/java/loopin/projectbook/MainApp.java)) is
in charge of the app launch and shut down.

* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues
the command `delete 1`.

<puml src="diagrams/ArchitectureSequenceDiagram.puml" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* implements its functionality using a concrete `{Component Name}Manager` class (which follows the corresponding API
  `interface` mentioned in the previous point.

For example, the `Logic` component defines its API in the `Logic.java` interface and implements its functionality using
the `LogicManager.java` class which follows the `Logic` interface. Other components interact with a given component
through its interface rather than the concrete class (reason: to prevent outside component's being coupled to the
implementation of a component), as illustrated in the (partial) class diagram below.

<puml src="diagrams/ComponentManagers.puml" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [
`Ui.java`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/java/loopin/projectbook/ui/Ui.java)

<puml src="diagrams/UiClassDiagram.puml" alt="Structure of the UI Component"/>

The UI consists of a `MainWindow` that is made up of parts e.g.`CommandBox`, `ResultDisplay`, `SplitPane`,
`PersonListPanel`, `ProjectListPanel`, `StatusBarFooter` etc. All these, including the `MainWindow`, inherit from the
abstract `UiPart` class which captures the commonalities between classes that represent parts of the visible GUI.

The `UI` component uses the JavaFx UI framework. The layout of these UI parts are defined in matching `.fxml` files that
are in the `src/main/resources/view` folder. For example, the layout of the [
`MainWindow`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/java/loopin/projectbook/ui/MainWindow.java)
is specified in [
`MainWindow.fxml`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/resources/view/MainWindow.fxml)

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component, as it displays `Person` object residing in the `Model`.

### Logic component

**API** : [
`Logic.java`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/java/loopin/projectbook/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<puml src="diagrams/LogicClassDiagram.puml" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API
call as an example.

<puml src="diagrams/DeleteSequenceDiagram.puml" alt="Interactions Inside the Logic Component for the `delete 1` Command" />

<box type="info" seamless>

**Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X) but due to a limitation of
PlantUML, the lifeline continues till the end of diagram.
</box>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, it is passed to an `ProjectBookParser` object which in turn creates
   a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which
   is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above (for simplicity), in the code it can take
   several interactions (between the command object and the `Model`) to achieve.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned back from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<puml src="diagrams/ParserClasses.puml" width="600"/>

How the parsing works:

* When called upon to parse a user command, the `ProjectBookParser` class creates an `XYZCommandParser` (`XYZ` is a
  placeholder for the specific command name e.g., `AddCommandParser`) which uses the other classes shown above to parse
  the user command and create a `XYZCommand` object (e.g., `AddCommand`) which the `ProjectBookParser` returns back as a
  `Command` object.
* All `XYZCommandParser` classes (e.g., `AddCommandParser`, `DeleteCommandParser`, ...) inherit from the `Parser`
  interface so that they can be treated similarly where possible e.g, during testing.

### Model component

**API** : [
`Model.java`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/java/loopin/projectbook/model/Model.java)

<puml src="diagrams/ModelClassDiagram.puml" width="450" />


The **`Model`** component:

* stores all data of the application, i.e.,
    - `Person` objects (contained in a `UniquePersonList`), and
    - `Project` objects (contained in a `UniqueProjectList`).  
      These lists are contained within the `ProjectBook`.

* keeps track of the currently _selected_ or _filtered_ lists of both `Person` and `Project` objects.  
  These are exposed to other components as unmodifiable `ObservableList<Person>` and `ObservableList<Project>` objects,
  allowing the UI to automatically update when data changes.

* stores a `UserPrefs` object that represents user preferences (e.g., GUI settings, file paths).  
  These preferences are exposed externally as `ReadOnlyUserPrefs` to ensure immutability.

* ensures data integrity by preventing duplicate entries through `UniquePersonList` and `UniqueProjectList`.

* defines relationships between people and projects:
    - Each `Person` may be linked to multiple `Project` objects.
    - Each `Project` contains one or more `Membership` entries that connect it back to the people involved.

* distinguishes between different types of persons:
    - `Volunteer`: a volunteer with basic contact info and tags.
    - `TeamMember`: a team member belonging to a committee.
    - `OrgMember`: a person associated with an external organisation.

* does not depend on other major components (e.g., Logic, UI, or Storage).  
  This separation ensures that the `Model` represents the core domain data and logic, independent of how the application
  interacts with users or files.

[//]: # (<box type="info" seamless>)

[//]: # (**Note:** An alternative &#40;arguably, a more OOP&#41; model is given below. It has a `Tag` list in the `Project
Book`, which `Person` references. This allows `Project
Book` to only require one `Tag` object per unique tag, instead of each `Person` needing their own `Tag` objects.<br>)

[//]: # ()

[//]: # (<puml src="diagrams/BetterModelClassDiagram.puml" width="450" />)

[//]: # (</box>)

### Storage component

**API** : [
`Storage.java`](https://github.com/AY2526S1-CS2103T-W14-4/tp/tree/master/src/main/java/loopin/projectbook/storage/Storage.java)

<puml src="diagrams/StorageClassDiagram.puml" width="550" />

The `Storage` component,

* can save both project book data and user preference data in JSON format, and read them back into corresponding
  objects.
* inherits from both `ProjectBookStorage` and `UserPrefStorage`, which means it can be treated as either one (if only
  the functionality of only one is needed).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects
  that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.project.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation of future features**

This section describes some noteworthy details on how certain features are intended to be implemented in the future.

### \[To be implemented in future versions\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedProjectBook`. It extends `ProjectBook` with an undo/redo
history, stored internally as an `projectBookStateList` and `currentStatePointer`. Additionally, it implements the
following operations:

* `VersionedProjectBook#commit()`— Saves the current project book state in its history.
* `VersionedProjectBook#undo()`— Restores the previous project book state from its history.
* `VersionedProjectBook#redo()`— Restores a previously undone project book state from its history.

These operations are exposed in the `Model` interface as `Model#commitProjectBook()`, `Model#undoProjectBook()` and
`Model#redoProjectBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedProjectBook` will be initialized with the
initial project book state, and the `currentStatePointer` pointing to that single project book state.

<puml src="diagrams/UndoRedoState0.puml" alt="UndoRedoState0" />

Step 2. The user executes `delete 5` command to delete the 5th person in the project book. The `delete` command calls
`Model#commitProjectBook()`, causing the modified state of the project book after the `delete 5` command executes to be
saved in the `projectBookStateList`, and the `currentStatePointer` is shifted to the newly inserted project book state.

<puml src="diagrams/UndoRedoState1.puml" alt="UndoRedoState1" />

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls
`Model#commitProjectBook()`, causing another modified project book state to be saved into the `projectBookStateList`.

<puml src="diagrams/UndoRedoState2.puml" alt="UndoRedoState2" />

<box type="info" seamless>

**Note:** If a command fails its execution, it will not call `Model#commitProjectBook()`, so the project book state will
not be saved into the `projectBookStateList`.

</box>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the
`undo` command. The `undo` command will call `Model#undoProjectBook()`, which will shift the `currentStatePointer` once
to the left, pointing it to the previous project book state, and restores the project book to that state.

<puml src="diagrams/UndoRedoState3.puml" alt="UndoRedoState3" />


<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index 0, pointing to the initial ProjectBook state, then there are no
previous Project
Book states to restore. The `undo` command uses `Model#canUndoProjectBook()` to check if this is the case. If so, it
will return an error to the user rather
than attempting to perform the undo.

</box>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

<puml src="diagrams/UndoSequenceDiagram-Logic.puml" alt="UndoSequenceDiagram-Logic" />

<box type="info" seamless>

**Note:** The lifeline for `UndoCommand` should end at the destroy marker (X) but due to a limitation of PlantUML, the
lifeline reaches the end of diagram.

</box>

Similarly, how an undo operation goes through the `Model` component is shown below:

<puml src="diagrams/UndoSequenceDiagram-Model.puml" alt="UndoSequenceDiagram-Model" />

The `redo` command does the opposite — it calls `Model#redoProjectBook()`, which shifts the `currentStatePointer` once
to the right, pointing to the previously undone state, and restores the project book to that state.

<box type="info" seamless>

**Note:** If the `currentStatePointer` is at index `projectBookStateList.size() - 1`, pointing to the latest project
book state, then there are no undone ProjectBook states to restore. The `redo` command uses `Model#canRedoProjectBook()`
to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</box>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the project book, such as
`list`, will usually not call `Model#commitProjectBook()`, `Model#undoProjectBook()` or `Model#redoProjectBook()`. Thus,
the `projectBookStateList` remains unchanged.

<puml src="diagrams/UndoRedoState4.puml" alt="UndoRedoState4" />

Step 6. The user executes `clear`, which calls `Model#commitProjectBook()`. Since the `currentStatePointer` is not
pointing at the end of the `projectBookStateList`, all project book states after the `currentStatePointer` will be
purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern
desktop applications follow.

<puml src="diagrams/UndoRedoState5.puml" alt="UndoRedoState5" />

The following activity diagram summarizes what happens when a user executes a new command:

<puml src="diagrams/CommitActivityDiagram.puml" width="250" />

#### Design considerations:

**Aspect: How undo & redo executes:**

* **Alternative 1 (current choice):** Saves the entire project book.
    * Pros: Easy to implement.
    * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
    * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
    * Cons: We must ensure that the implementation of each individual command are correct.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, configuration, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [Configuration guide](Configuration.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* volunteer organisation project director
* has a need to manage a significant number of contacts from team members, organisations and volunteers
* has a need to keep track of liaisons
* prefer desktop apps over other types
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: provide fast access to contact details of project team members, volunteers and organisations,
track project updates

### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority                                        | As a …​                                         | I want to …​                          | So that I can…​                                                            |
|-------------------------------------------------|-------------------------------------------------|---------------------------------------|----------------------------------------------------------------------------|
| `* *`                                           | new user                                        | see usage instructions                | refer to instructions when I forget how to use the App                     |
| `* * *`                                         | user                                            | add a new team member                 | add contact details of a team member who is part of a committee            |
| `* * *`                                         | user                                            | add a new organisation member         | add contact details of an external organisation member                     |
| `* * *`                                         | user                                            | add a new volunteer                   | add contact details of a recruited volunteer                               |
| `* * *`                                         | user                                            | delete a person                       | remove contacts that I no longer need                                      |
| `* * *`                                         | user                                            | create a remark to update a person    | remember that I need to update that person                                 |
| `* * *`                                         | user                                            | resolve a remark for a person         | note that I have updated the person                                        |
| `* * *`                                         | user                                            | add a project                         | organise people who are involved in a project                              |
| `* * *`                                         | user                                            | assign a person to a project          | indicate that a person is involved in a project                            |
| `* * *`                                         | user                                            | remove a person from a project        | indicate that a person is no longer involved in a project                  |
| `* * *`                                         | user                                            | view a project                        | easily find the details of everyone involved in a project                  |
| `* *`                                           | user                                            | edit a person                         | edit inaccurate details about a person quickly                             |
| `* `                                            | new user who has finished learning the commands | remove all sample data at once        | quickly clear away sample data before adding in my own data                |
| `* * *`                                         | user                                            | delete a project                      | remove projects that I am not managing anymore                             |
| `* * *`                                         | user                                            | exit the app                          | exit the app when I am done using it                                       |
| `* *`                                           | user with many contacts added                   | search for a person                   | easily find contact details of a person without scrolling through the list |
| `* *`                                           | user with many contacts added                   | list all persons of a particular role | easily find all contacts of a particular role                              |
| `* *`                                           | user with many projects added                   | search for a project                  | easily find a project without scrolling through the list                   |
| `* *`                                           | user                                            | list out all persons                  | view a full list of all my contacts                                        |
| `* *`                                           | user                                            | list out all projects                 | view a full list of all my projects                                        |
| `* *` <br> (for future developers to implement) | user                                            | edit a project                        | edit inaccurate details about a project quickly                            |

### Use cases

(For all use cases below, the **System** is the `Loopin` and the **Actor** is the `user`, unless specified otherwise)

**Use Case: UC1 - Add Team Member**
(The use cases for add volunteer and organisation member are similar to this and will not be repeated)

**MSS**

1. User requests to add a team member and provides information about team member.
2. System stores the new team member's details.

   Use case ends.

**Extensions**

* 1a. Invalid command format (Missing, extra or invalid parameter(s)).

    * 1a1. System shows an error message indicating invalid command format.

      Use case ends.

* 1b. Duplicate person

    * 1b1. System checks the database and finds a person with identical email, phone or telegram.
    * 1b2. System shows an error message.

      Use case ends.

**Use case: UC2 - Delete Person**

**MSS**

1. User requests to delete a specific person in the list.
    - User specifies the index to delete.
2. System removes the person from the project book.

   Use case ends.

**Extensions**

* 1a. The given index is invalid.

  Use case ends.

**Use case: UC3 - Add Remark for Person Update**

**MSS**

1. User requests to create remark for a person.
    - User specifies the index to create remark.
2. System stores the new remark and sets its status to `pending`.

   Use case ends.

**Extensions**

* 1a. Missing or incorrectly formatted parameter(s).

    * 1a1. System shows an error message indicating parameter(s) are missing or incorrect.

      Use case ends.

* 1b. The given index is invalid.

    * 1b1. System shows an error message.

      Use case ends.

* 1c. The same person has duplicate remarks.

    * 1c1. System checks the database and finds that the specified person already has an identical remark pending or
      completed.
    * 1c2. System shows an error message indicating that person already has an identical remark.

      Use case ends.

**Use case: UC4 - Complete Person Update**

**MSS**

1. User requests to mark a person’s update as completed by providing the index of the person.
    - User specifies the index to create remark.
2. System updates the remark status to `completed`.

Use case ends.

**Extensions**

* 1a. Missing or incorrectly formatted parameter(s).

    * 1a1. System shows an error message.

      Use case ends.

* 1b. The given index is invalid.

    * 1b1. Project
      Book shows an error message.

      Use case ends.

* 1c. Remark not found for this person.

    * 1c1. System checks the person’s remarks and finds no matching pending or completed remark.

    * 1c2. System shows an error message indicating the remark was not found.

    * 1c3. User may choose to <u>UC5 - Add Remark for Person Update</u>.

      Use case resumes at step 1.

**Use case: UC5 - Add project**

**MSS**

1. User requests to create a new project.
2. System stores the new project.

Use case ends.

**Extensions**

* 1a. Missing or incorrectly formatted parameter(s).

    * 1a1. System shows an error message.

      Use case ends.

* 1b. Duplicate project.

    * 1b1. System checks the database and finds a project with the same name.
    * 1b2. System shows an error message.

      Use case ends.

**Use case: UC6 - Assign person to project**

**MSS**

1. User requests to assign a person to a project by specifying the person index and the project name.
2. System links the person to the project.

Use case ends.

**Extensions**

* 1a. Invalid person index.

    * 1a1. System shows an error message indicating that index is invalid.

      Use case ends.

* 1b. Project does not exist.

    * 1b1. System shows an error indicating that project does not exist.
    * 1b2. User may choose to <u>UC5: Add Project</u>.

      Use case resumes at step 1.

* 1c. Person already linked to project.

    * 1c1. System shows an error indicating that person already linked to project.

      Use case ends.

**Use case: UC7 - Remove Person from Project**

**MSS**

1. User requests to remove a person from a project by specifying the person index and the project name.
2. System removes the person from the project.

Use case ends.

**Extensions**

* 1a. Invalid person index.

    * 1a1. System shows an error message indicating that index is invalid.

      Use case ends.

* 1b. Project does not exist.

    * 1b1. System shows an error indicating that project does not exist.
    * 1b2. User may choose to <u>UC5: Add Project</u>.

      Use case resumes at step 1.

* 1c. Person not in project.

    * 1c1. System shows an error indicating that person not linked to project.

      Use case ends.

**Use case: UC10 - View Project**

**MSS**

1. User requests to view a project.
2. System retrieves the details of the project.
3. System displays the project to users.

Use case ends.

**Extensions**

* 1a. Missing or incorrectly formatted parameter(s).

    * 1a1. System shows an error message.

      Use case ends.

* 1b. Project does not exist.

    * 1b1. System shows an error indicating that project does not exist.
    * 1b2. User may choose to <u>UC5: Add Project</u>.
      Use case resumes at step 1.

### Non-Functional Requirements

1. Should work on any _mainstream OS_ as long as it has Java `17` or above installed.
2. Should process commands and display commands within 1 second.
3. Should function without an internet connection.
4. Should not use more than 1GB of RAM.
5. Should be able to hold up to 1000 persons and 30 projects without a noticeable sluggishness in performance for
   typical usage.
6. A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be
   able to accomplish most of the tasks faster using commands than using the mouse.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, MacOS
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **CLI command**: A text-based instruction entered by the user to perform an action in Loopin.
* **Parameter**: A piece of information provided by the user as part of a CLI command. Usually follows a strict format.
* **Index**: The position of a contact in the current list as displayed by the system. Uses 1-based indexing.
* **Remark**: A tag attached to a contact indicating a need to update them on the project. Each remark has its own
  status (pending or completed).
* **Volunteer**: A volunteer with basic contact info
* **Team Member**: A team member with basic contact info with a Committee.
* **Organisation Member**: A member of an external organisation with basic contact info and an Organisation.
* **Project**: A logical grouping of tasks, updates, and contacts in Loopin.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<box type="info" seamless>

**Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</box>

### Launch and shutdown

1. Initial launch

    1. Download the jar file and copy into an empty folder

    2. Double-click the jar file Expected: Shows the GUI with a set of sample contacts. The window size may not be
       optimum.

1. Saving window preferences

    1. Resize the window to an optimum size. Move the window to a different location. Close the window.

    2. Re-launch the app by double-clicking the jar file.<br>
       Expected: The most recent window size and location is retained.

### Adding a person

1. Adding a person while all persons are shown

    1. Test case: `addv n/John Wang e/johnwang@gmail.com t/johnwang`<br>
       Expected: A new Volunteer named John Wang with email johnwang@gmail.com and telegram @johnwang is added to the
       person list.

    2. Test case: `addo o/Good Org n/Mr Good e/goodorg@org.com`<br>
       Expected: A new representative Mr Good from Good Org has been added to the person list.

### Adding a project

1. Adding a project while all projects are being shown

    1. Test case: `project add n/Clean Beach d/Cleaning the beach`<br>
       Expected: A new project named Clean Beach is added to the list with the correct timestamp. No last update is
       available.

    2. Test case: `project add d/No name provided`<br>
       Expected: No project is added. Error message shown indicating that the name field is required. Status bar remains
       unchanged.

### Adding a remark to person

1. Adding a remark when person is being shown

    1. Test case: `remark 1 u/Call him`<br>
       Expected: A new remark tag labeled "Call him" is attached to the first person in the person list.

    2. Test case: `remark 999 u/Call him`<br>
       Expected: No remarks are added as index does not exist.

### Assigning a person to project

1. Assigning a person to project

    1. Prerequisites: At least one project (e.g. Clean Beach) exists and one person exists.

    1. Test case: `project assign 1 project/Clean Beach`<br>
       Expected: The first person in the persons list is added to the project Clean Beach, with the correct updated
       status bar

    2. Test case: `project assign 1 project/Clean Beach`<br>
       Expected: An error message appears indicating that person is already in the project.

### Deleting a person

1. Deleting a person while all persons are being shown

    1. Prerequisites: List all persons using the `list` command. Multiple persons in the list.

    2. Test case: `delete 1`<br>
       Expected: First contact is deleted from the list. Details of the deleted contact shown in the status message.
       Timestamp in the status bar is updated.

    3. Test case: `delete 0`<br>
       Expected: No person is deleted. Error details shown in the status message. Status bar remains the same.

    4. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
       Expected: Similar to previous.

### Saving data

1. Dealing with missing data files

    1. To simulate a missing data file, delete the file data/projectbook.json.

    2. Launch the app.
       Expected: The app will be populated with sample data, similar to first launch.

2. Dealing with corrupted data files

    1. To simulate a corrupted data file, edit the file data/projectbook.json such that the data is invalid.

    2. Launch the app.
       Expected: The app will delete all data and start from an empty projectbook.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Planned Enhancements**

Team size: 5

1. Handle long inputs in the UI (names, phones, emails, remarks).<br>
   Currently, long inputs are truncated with "..." in the UI. We plan to implement text wrapping so that the full text
   is displayed to the user.

2. Allow non exact matches for project/PROJECT_NAME inputs.<br>
   Currently, commands that require the input project/PROJECT_NAME require the user input to match the project name
   exactly. We plan to allow the user to use a substring of project name instead of the full project name, provided that
   there is only one project that matches the substring.

3. Make `find` search more fields.<br>
   Currently, `find` only searches the name and `findrole` only searches the role. We plan to make `find` more flexible
   and allow it to search other fields such as the phone, email and telegram.

4. Allow user to choose between long and short version of each project command word.<br>
   Currently, each project command follows the format `project COMMAND_WORD`. We plan to implement a second, shorter
   version of each command word (i.e. project add = addp, project delete = deletep, project assign = assignp)

5. Allow more flexibility in the validation for Phone.<br>
   Currently, phone numbers must be strictly numeric and more than 3 digits. We plan to allow for more flexible formats
   such as `1234 5678 (HP) 1111-3333 (Office)` and `(+65) 9876 4321`

6. Add deadline for remarks.<br>
   Currently, remarks act as a simple reminder without any prioritised order. We plan to give the user the option to add
   a deadline for when a remark should be resolved by, and prioritize the order of remarks by the urgency of the
   deadline.

7. Add warnings for `clear`, `delete` and `project delete`<br>
   Currently, if the user correctly inputs the command for `clear`, `delete`, and `project delete`, the app will simply
   perform the deletion without any additional checks. We plan to add an additional warning step, where the app will
   warn the user of the exact deletion that is about to happen and asks the user for confirmation before deleting.
8. Allow editing of projects via a `project edit` command <br>
   Currently, a project can only be deleted and remade, if an user wants to make a change to it. We plant to have a
   `project edit` command that allows users to directly make changes to an existing project.

--------------------------------------------------------------------------------------------------------------------
