## globus-file-handler-cli

This java package provides a CLI to download files from a Globus collection over HTTPS, with optional crypt4gh decryption on the fly.

### How to build?

```
$ mvn clean package
```

#### Building docker image

You'll need to set up a a token in the maven settings file and mount it during build:

```
$ docker build --secret id=MAVEN_SETTINGS,src=$HOME/.m2/settings.xml  -t test -f docker/crypt4gh/Dockerfile .
```

### Pre-built packages

Check out the [package registry](https://github.com/orgs/ebi-gdp/packages?repo_name=globus-file-handler-cli) to find pre-built packages and container images.

The docker images are built in amd64 and arm64 architectures.

### How to run?

Use appropriate jar version

#### Original file download
```
# Help option
java -jar globus-file-handler-cli-1.0.0.jar -h

# Execute download
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s {globus/file/path} -d {local/file/download/path} --file_size {file size in bytes}

# Example short option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s "golbus:///ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" -d "file:///Users/ashutosh/downloaded-files" -l 278705850

# Example long option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev --globus_file_transfer_source_path "golbus:///ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" --globus_file_transfer_destination_path "file:///Users/ashutosh/downloaded-files" --file_size 278705850
```
#### Crypt4gh - Decrypted file download [Decrypts file on the fly encrypted using 'crypt4gh']
```
# Help option
java -jar globus-file-handler-cli-1.0.0.jar -h

# Execute download
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s {globus/file/path} -d {local/file/download/path} --file_size {file size in bytes} --crypt4gh --sk {local/file/secret/key/path}

# Example short option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s "golbus:///ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" -d "file:///Users/ashutosh/downloaded-files" -l 278705850 --crypt4gh --sk "/Users/ashutosh/downloaded-files/private.sec"

# Example long option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev --globus_file_transfer_source_path "golbus:///ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" --globus_file_transfer_destination_path "file:///Users/ashutosh/downloaded-files" --file_size 278705850 --crypt4gh --sk "/Users/ashutosh/downloaded-files/private.sec"
```
