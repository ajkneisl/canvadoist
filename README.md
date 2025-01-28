# canvadoist

Sync your Canvas assignments into Todoist!

## How to Build using Gradle
1. Clone repository.
2. Run `sh gradlew shadowJar`.
3. An executable jar will be created at `./build/libs`.

## How to Run
1. Running `java -jar canvadoist.jar` will create an empty `config.json` next to the jar. 
2. In this config, fill in your Canvas API token, your Todoist API token, and your Canvas's URL (such as "https://canvas.umn.edu").
3. Now, find your course ID's, run `java -jar canvadoist.jar view-courses`.
4. From that result, copy the course ID's that you'd like to track. 
5. To select them, run `java -jar canvadoist.jar select-courses [course id] [course id] etc..`
6. Now, run `java -jar canvadoist.jar` periodically to transfer Canvas assignments into Todoist!