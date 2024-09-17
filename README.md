## globus-file-handler-cli

This [java package](https://github.com/orgs/ebi-gdp/packages?repo_name=globus-file-handler-cli) provides a CLI to download files from a Globus collection over HTTPS, with optional crypt4gh decryption on the fly.

## Set up runtime configuration

You'll need to configure the CLI to be able to access private Globus collections.

The CLI application can be configured by creating an [`application.properties`](https://docs.spring.io/spring-boot/docs/1.1.0.M1/reference/html/boot-features-external-config.html#boot-features-external-config-application-property-files) file.

### Globus configuration

| Key                              | Value                                         | Description                                                                                     |
|----------------------------------|-----------------------------------------------|-------------------------------------------------------------------------------------------------|
| `globus.guest-collection.domain` | `https://url.data.globus.org`                 | https://docs.globus.org/globus-connect-server/v5.4/use-client-credentials/#register-application |
| `globus.aai.client-id`           | `uuid`                                        | https://docs.globus.org/globus-connect-server/v5.4/use-client-credentials/                      |
| `globus.aai.client-secret`       | `secret-token`                                | https://docs.globus.org/globus-connect-server/v5.4/use-client-credentials/#obtain_access_tokens |
| `globus.aai.scopes`              | `https://author.globus.org/scopes/uuid/https` | https://docs.globus.org/guides/overviews/clients-scopes-and-consents/                                                                                              

### (Optional) crypt4gh configuration

| Key                              | Value                                         | Description                                                                                     |
|----------------------------------|-----------------------------------------------|-------------------------------------------------------------------------------------------------|
| `crypt4gh.shell-path`            | `/bin/bash -c`                                | Path to the local shell                                                                         |
| `crypt4gh.binary-path`           | `/opt/bin/crypt4gh`                           | Path to the crypt4gh binary                                                                     |

### (Optional) Key handler service configuration

| Key                                       | Value                                         | Description                                         |                                   
|-------------------------------------------|-----------------------------------------------|-----------------------------------------------------|
| `intervene.key-handler.base-url`          | `https://example.comkey-handler               | Base path to a key handler instance                 |
| `intervene.key-handler.keys.uri`          | `key/{secretId}/version/{secretIdVersion}`    |                                                     |
| `intervene.key-handler.basic-auth`        | token                                         | Token to authenticate with the key handler service  |
| intervene.key-handler.secret-key.password | password                                      | Password used to decrypt the fetched private key    |

## Example configuration

Depending on your deployment the application properties file might look something like this:

```
globus.guest-collection.domain=https://url.data.globus.org
globus.aai.client-id=uuid
globus.aai.client-secret=secret-token
globus.aai.scopes=https://author.globus.org/scopes/uuid/https
crypt4gh.shell-path=/bin/bash -c
crypt4gh.binary-path=/opt/bin/crypt4gh
```

(`uuid` / `secret-token` are placeholders - replace with your own values)

## How to download files from a Globus collection over HTTPS

> [!TIP]
> Remember to wrap all paths in the CLI arguments in quotes and always specify a prefix (e.g. `"file:///..."`)

```
# Help option
$ java -jar globus-file-handler-cli-1.0.0.jar -h

# Execute download
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s {globus/file/path} -d {local/file/download/path} --file_size {file size in bytes}

# Example short option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s "golbus:///ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" -d "file:///Users/ashutosh/downloaded-files" -l 278705850

# Example long option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev --globus_file_transfer_source_path "golbus:///ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" --globus_file_transfer_destination_path "file:///Users/ashutosh/downloaded-files" --file_size 278705850
```

## How to download files and decrypt on the fly with crypt4gh (local keys)

> [!TIP]
> This process assumes your crypt4gh key pairs are present on your local machine and secret keys are unencrypted 

```
# Execute download
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s {globus/file/path} -d {local/file/download/path} --file_size {file size in bytes} --crypt4gh --sk {local/file/secret/key/path}

# Example short option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s "golbus:///ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" -d "file:///Users/ashutosh/downloaded-files" -l 278705850 --crypt4gh --sk "/Users/ashutosh/downloaded-files/private.sec"

# Example long option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev --globus_file_transfer_source_path "golbus:///ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" --globus_file_transfer_destination_path "file:///Users/ashutosh/downloaded-files" --file_size 278705850 --crypt4gh --sk "/Users/ashutosh/downloaded-files/private.sec"
```

## How to download files and decrypt on the fly with crypt4gh (secret manager)

> [!TIP]
> This process assumes your encrypted crypt4gh secret key is in google secret manager

```
# Example secret config option (e.g. stored on secret manager)
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=dev -s "globus:///ashutosh@ebi.ac.uk/Friday-10-05-09-46-Test/hapnest.pvar.c4gh" --globus_file_transfer_destination_path "file:///Users/ashutosh/Desktop/Intervene-Files/downloaded-files/hapnest.pvar" -l 215004174 --crypt4gh --sk "file:///Users/ashutosh/crypt4gh/globus-cli-test/096149F5-8566-437A-80A1-16BAFB65F74B-secret-config.json"
```

## Build instructions

* Build with maven:

```
$ mvn clean package
```

* Or build a docker image (which builds with maven):

```
$ docker build --secret id=MAVEN_SETTINGS,src=$HOME/.m2/settings.xml  -t test -f docker/crypt4gh/Dockerfile .
```

> [!TIP]
> You'll need to set up a a token in the maven settings file (`settings.xml`) and mount it during build
