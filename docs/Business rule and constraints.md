User:

- UBR-001: The system shall allow users to create an account using their email address and a password.
    - **UBR-001.1** The email address must be unique and not already associated with an existing account.
    - **UBR-001.2** The password must be at least 8 characters long
    - **UBR-001.3** The password must contain at least one uppercase letter, one lowercase letter, and one number.

- UBR-002: The system shall allow users to log in using their email address and password.
    - **UBR-002.1** The system shall validate the email address and password against the stored credentials.
    - **UBR-002.2** If the credentials are valid, the user shall be granted access to their account.
    - **UBR-002.3** If the credentials are invalid, the system shall display an error message and prompt the user to try again.

- UBR-003: The system shall allow users to reset their password if they forget it.
    - **UBR-003.1** The user shall be able to request a password reset by providing their email address.
    - **UBR-003.2** The system shall send a password reset link to the user's email address.
    - **UBR-003.3** The password reset link shall be valid for 15 minutes.

- UBR-004: The system shall allow users to update their account information, including their email address and password.
    - **UBR-004.1** The user shall be required to enter their current password to update their account information.
    - **UBR-004.2** The system shall validate the new email address and password against the same criteria as account creation.
    - **UBR-004.3** If the new email address is already associated with another account, the system shall display an error message and prompt the user to choose a different email address.

Project Administrator:
- PRA-001: The system shall allow project administrators to manage user accounts and permissions.
    - **PRA-001.1** Project administrators shall be able to view a list of all user accounts associated with the project.
    - **PRA-001.2** Project administrators shall be able to assign roles and permissions to users based on their responsibilities within the project.
    - **PRA-001.3** Project administrators shall be able to deactivate or remove user accounts from the project.

Project Member:
- PRM-001: The system shall allow project members to view and update their assigned tasks.
    - **PRM-001.1** Project members shall be able to update the status, description, and other attributes of their assigned tasks.
    - **PRM-001.2** Project members shall be able to view the history of changes made to their assigned tasks.
    - **PRM-001.3** Project members shall receive notifications when their assigned tasks
- PRM-002: The system shall allow project members to communicate and collaborate with other members of the project.
    - **PRM-002.1** Project members shall be able to add comments to tasks and view comments from other members.
    