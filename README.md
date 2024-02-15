# globus-file-handler-cli

### How to build?
```
mvn clean package
```
### How to run?
Use appropriate jar version
```
# help option
java -jar globus-file-handler-cli-1.0.0.jar -h

# Execute download
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s {globus/file/path} -d {local/file/download/path} --file_size {file size in bytes}

# Example
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen -d . --file_size 278705850
```
