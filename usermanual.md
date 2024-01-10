# VSFyClient User Manual

## How to Launch the Project

1. **Prerequisites:**
   - Ensure Java is installed on your system.
   - Maven should be installed for building the project.

2. **Building the Project:**
   - Navigate to the project directory in the terminal.
   - Run `mvn clean package` to build the project. This will create a JAR file in the `target` directory.

3. **Running the Client:**
   - After building, navigate to the `target` directory.
   - Run the client using the command `java -jar VSFyClient-1.0-SNAPSHOT-jar-with-dependencies.jar`.

## How to Use the Project

After starting the client, you will need to give a name to your client
Then you will be asked to give the path to your music folder

And then you will be able to use the following commands:
- **help**: Displays the help menu with all available commands.
- **list**: Lists all available songs from connected clients.
- **request**: Requests a specific song from another client.
- **info**: Retrieves information about a specific client.
- **exit**: Quits the application.

## How the Project Works

- The `UnifiedClient` class manages the client's interaction with the server and other clients.
- The `Command` interface and its implementations (`ListCommand`, `RequestCommand`, `InfoCommand`, etc.) handle specific user commands.
- The client runs a P2P server to facilitate direct file transfers.

## How to Generate a JAR if Not Generated

If the JAR file is not generated:

- Ensure you are in the project's root directory.
- Make sure Maven is correctly installed and configured.
- Run `mvn clean package` again and check for errors.

## Access the JavaDoc for More Information

JavaDoc provides detailed documentation on the project's classes and methods.

1. **Generating JavaDoc:**
   - Run `mvn javadoc:javadoc` from the project's root directory.
   - This generates documentation in the `target/site/javadoc` directory.

2. **Viewing JavaDoc:**
   - Open the `index.html` file in the generated JavaDoc directory in a web browser to view the documentation.

## How to Generate the JavaDoc

- From the root directory of the project, run `mvn javadoc:javadoc`.
- The generated JavaDoc will be in the `target/site/javadoc` directory.
