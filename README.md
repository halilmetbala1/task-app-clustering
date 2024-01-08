# eTutor Task-App: Binary Search

This application provides a REST-interface for following task type: binary_search.

Students have to guess a number. The student's input is compared with the number stored in the solution, and it is returned whether the number sought is _smaller_, _equal_ to or
_greater_ than the input.

This project **can** be used as a template for new task apps. Replace all occurrences of `binarysearch`/`binary search`/`binary-search`/`binary_search` with the name of your task
type.

Task-App repositories should start with `task-app-` and end with the task type (e.g. `task-app-binary-search`).

## Development

See [CONTRIBUTING.md](https://github.com/eTutor-plus-plus/task-app-starter/blob/main/CONTRIBUTING.md) for details.

## Docker

Start a new instance of the application using Docker:

```bash
docker run -p 8090:8081 \ 
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://postgres:5432/etutor_binary_search" \
  -e SPRING_DATASOURCE_USERNAME=etutor_binary_search \
  -e SPRING_DATASOURCE_PASSWORD=myPwd \
  -e SPRING_FLYWAY_USER=etutor_binary_search_admin \
  -e SPRING_FLYWAY_PASSWORD=adPwd \
  -e CLIENTS_API_KEYS_0_NAME=task-administration \
  -e CLIENTS_API_KEYS_0_KEY=4fsda6f465sad4f6sfd \
  -e CLIENTS_API_KEYS_0_ROLES_0=CRUD \
  -e CLIENTS_API_KEYS_0_ROLES_1=SUBMIT \
  -e CLIENTS_API_KEYS_1_NAME=moodle \
  -e CLIENTS_API_KEYS_1_KEY=as89df47s98ad7f98s7d \
  -e CLIENTS_API_KEYS_1_ROLES_0=SUBMIT \
  -e CLIENTS_API_KEYS_2_NAME=plagiarism-checker \
  -e CLIENTS_API_KEYS_2_KEY=adf455jfil45646 \
  -e CLIENTS_API_KEYS_2_ROLES_0=READ_SUBMISSION \
  etutorplusplus/task-app-binary-search
```

or with Docker Compose:

```yaml
version: '3.8'

services:
    task-app-binary-search:
        image: etutorplusplus/task-app-binary-search
        restart: unless-stopped
        ports:
            -   target: 8081
                published: 8090
        environment:
            SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/etutor_binary_search
            SPRING_DATASOURCE_USERNAME: etutor_binary_search
            SPRING_DATASOURCE_PASSWORD: myPwd
            SPRING_FLYWAY_USER: etutor_binary_search_admin
            SPRING_FLYWAY_PASSWORD: adPwd
            CLIENTS_API_KEYS_0_NAME: task-administration
            CLIENTS_API_KEYS_0_KEY: 4fsda6f465sad4f6sfd
            CLIENTS_API_KEYS_0_ROLES_0: CRUD
            CLIENTS_API_KEYS_0_ROLES_1: SUBMIT
            CLIENTS_API_KEYS_1_NAME: moodle
            CLIENTS_API_KEYS_1_KEY: as89df47s98ad7f98s7d
            CLIENTS_API_KEYS_1_ROLES_0: SUBMIT
            CLIENTS_API_KEYS_2_NAME: plagiarism-checker
            CLIENTS_API_KEYS_2_KEY: adf455jfil45646
            CLIENTS_API_KEYS_2_ROLES_0: READ_SUBMISSION
```

### Environment Variables

In production environment, the application requires two database users:

* A database administrator user which has the permission to create the tables.
* A JPA user which has read/write access (`SELECT, INSERT, UPDATE, DELETE, TRUNCATE`) to the database tables (see `./docker/create_user.sh` for example user creation statement).

> In development environment, one user will be used for both.

The users must be configured via environment variables. The clients have to be configured via environment variables as well (`X`/`Y` stands for a 0-based index).

| Variable                     | Description                                      |
|------------------------------|--------------------------------------------------|
| `SERVER_PORT`                | The server port.                                 |
| `SPRING_DATASOURCE_URL`      | JDBC-URL to the database                         |
| `SPRING_DATASOURCE_USERNAME` | The username of the JPA user.                    |
| `SPRING_DATASOURCE_PASSWORD` | The password of the JPA user.                    |
| `SPRING_FLYWAY_USER`         | The username of the database administrator user. |
| `SPRING_FLYWAY_PASSWORD`     | The password of the database administrator user. |
| `CLIENTS_API_KEYS_X_NAME`    | The name of the client.                          |
| `CLIENTS_API_KEYS_X_KEY`     | The API key of the client.                       |
| `CLIENTS_API_KEYS_X_ROLES_Y` | The role of the client.                          |
