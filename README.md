# Quiz-Application
Simple Backend Application for saving Questions and related Answers, with minimal dependencies utilizing JDBC.


## Setting up the Project:
Clone the repository and open the project in your IDE of choice:
```
git clone https://github.com/markm001/quiz-application-assignment.git
```

Desired configurations can be set within the
`\src\resources\ `
Folder before running the tests.
```lombok.config
URL=jdbc:mysql://localhost:port/db
USERNAME=user
PASSWORD=pass
```
Database creation script can be found within
`\src\resources\db\migration `

###
> Run the tests from `\test\model\repository\DaoQuestionTest.class `

## Possible Improvements:
- runs several SELECT queries to check if a Response already exists in the Database and retrieves the Primary-Key.
- replacing DriverManager with Connection Pooling (for several Threads)
  - replacing static DatabaseConnector for Instanced
- unused responses remain in table (may be used for several other questions)