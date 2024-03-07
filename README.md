# globus-file-handler-cli

### How to build?
```
mvn clean package
```
### How to run?
Use appropriate jar version
#### Original file download
```
# Help option
java -jar globus-file-handler-cli-1.0.0.jar -h

# Execute download
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s {globus/file/path} -d {local/file/download/path} --file_size {file size in bytes}

# Example short option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s "ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" -d "/Users/ashutosh/downloaded-files" -l 278705850

# Example long option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev --globus_file_download_source_path "ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" --globus_file_download_destination_path "/Users/ashutosh/downloaded-files" --file_size 278705850
```
#### Crypt4gh - Decrypted file download [Decrypts file on the fly encrypted using 'crypt4gh']
```
# Help option
java -jar globus-file-handler-cli-1.0.0.jar -h

# Execute download
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s {globus/file/path} -d {local/file/download/path} --file_size {file size in bytes} --crypt4gh --sk {local/file/secret/key/path}

# Example short option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s "ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" -d "/Users/ashutosh/downloaded-files" -l 278705850 --crypt4gh --sk "/Users/ashutosh/downloaded-files/private.sec"

# Example long option
java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev --globus_file_download_source_path "ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" --globus_file_download_destination_path "/Users/ashutosh/downloaded-files" --file_size 278705850 --crypt4gh --sk "/Users/ashutosh/downloaded-files/private.sec"
```
