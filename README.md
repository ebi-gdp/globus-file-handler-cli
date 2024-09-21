## globus-file-handler-cli

This [Java package](https://github.com/orgs/ebi-gdp/packages?repo_name=globus-file-handler-cli) provides a CLI to download files from a Globus collection over HTTPS to local storage. This CLI application peovides 2 main types of downloads

## Overview
* Original file download (downloads exact copy of uploaded file).
* Download & decrypt crypt4gh encrypted file on the fly. Downloaded file is decrypted version of orginal encrypted file. This feature has 2 variations, private key is stored on local storage Or Secret Manager e.g. GCP. Private keys are being handled by Key Handler Service deployed part of IGS4EU Genetice Scoring Platform. Encryption/Decryption feature is based on Asymmetric Cryptography (Public/Private key pair). For more information about crypt4gh, refer to [crypt4gh](https://crypt4gh.readthedocs.io/en/latest/encryption.html).

## Requirements
For running the application as jar you need:
* JRE 21+ (You can use either OpenJDK or Oracle distributions).

## Configure properties
In order to run application successfully you need to configure application properties file with appropriate values.
* `application.properties` => This file is important, make sure you make Globus config changes in order to connect to glous. This is required for all types of download.
* `application-crypt4gh.properties` => Make changes in this file to download decrypted version of file from globus provided secret key/ private key is stored locally.
* `application-crypt4gh-secret-manager.properties` => Make changes in this file to download decrypted version of file from globus provided secret key/ private key is stored on Secret Manager.

#### Application config
| Property                          | Value                                     | Description |
|-----------------------------------|-------------------------------------------|-------------|
| `data.copy.buffer-size` | `8192` | The bufferSize used to copy from the input to the output. |

#### Apache HttpClient connection config
| Property                          | Value                                     | Description |
|-----------------------------------|-------------------------------------------|-------------|
| `webclient.connection.pipe-size` | `${data.copy.buffer-size}` | Preferred to keep as defined in buffer size. |
| `webclient.connection.connection-timeout` | `5` | Connection timeout in seconds. |
| `webclient.connection.socket-timeout` | `0` | Socket timeout in seconds. |
| `webclient.connection.read-write-timeout` | `30000` | Read-write timeout in Milliseconds. |

#### Apache HttpClient connection config
| Property                          | Value                                     | Description |
|-----------------------------------|-------------------------------------------|-------------|
| `file.download.retry.strategy` | `FIXED` OR `EXPONENTIAL` | Retry strategy in case failure occures (e.g. Network issue). Choose according to your need. Refer to Spring framework retry template. |
| `file.download.retry.attempts.max` | `3` | Number of retry attempts |
| **Fixed Strategy properties** || Define properties if `FIXED` strategy selected.|
| `file.download.retry.attempts.back-off-period` | `2000` | None |
| **Exponential Strategy properties** || Define properties if `EXPONENTIAL` strategy selected.|
| `file.download.retry.attempts.delay` | `1000` | Delay between retry attempts |
| `file.download.retry.attempts.maxDelay` | `30000` | Retry attempts max delay in milliseconds |
| `file.download.retry.attempts.multiplier` | `2` | Retry attempts multipler |
For more details explore **Spring Retry Template** documentation.

#### Globus config
| Property                          | Value                                     | Description |
|-----------------------------------|-------------------------------------------|-------------|
| `globus.guest-collection.domain` | `https://{generated-domain-prefix}.data.globus.org` | Create Guest collection programatically. You can find your domain under overview section of Globus at https://app.globus.org/file-manager/collections. Repace `{generated-domain-prefix}` with actual value.|
| `globus.aai.access-token.uri` | `https://auth.globus.org/v2/oauth2/token` | This is standard auth URL. Use the same unless changed by Globus |
| `globus.aai.client-id` | `7v3vg66f-4g78-6586-a10a-e4567a4e3a34` | Client Id - UUID string, you will get this once you register your client on globus. Required for Authentication. Replace with actual value.|
| `globus.aai.client-secret` | `Vh8cVcVnpp5Z9K67LLXc8Xuc6TOPk3T4CqUNMhBnYOU=`| Client Secret - A string of charaters. You will get this once you register your client on globus. Required for Authentication. Replace with actual value.|
| `globus.aai.scopes` | `https://auth.globus.org/scopes/{f1t4567c-34f4-4e3f-1111-433565d05v6r}/https` | UUID of Guest Collection. Replace `{f1t4567c-34f4-4e3f-1111-433565d05v6r}` with actual value.|
[Globus client registration & its usage](https://docs.globus.org/globus-connect-server/v5.4/use-client-credentials/#register-application).

#### Crypt4gh config
This config is required for `crypt4gh` profile.

| Property                          | Value                                     | Description |
|-----------------------------------|-------------------------------------------|-------------|
| `crypt4gh.shell-path` | `/bin/bash -c` | Path to local shell inside environment where CLI application is running. Replace as per local environment.|
| `crypt4gh.binary-path` | `/opt/bin/crypt4gh` | Path to local crypt4gh binary executable inside environment where CLI application is running. Replace as per local environment.|

#### Intervene key handler service config
This config is required for `crypt4gh-secret-manager` profile.

| Property                          | Value                                     | Description |
|-----------------------------------|-------------------------------------------|-------------|
| `intervene.key-handler.base-url` | `https://{intervene.platform.domain}/key-handler` | This URL refers to key handler service. Replace `{intervene.platform.domain}` with actual value.|
| `intervene.key-handler.keys.uri` | `/key/{secretId}/version/{secretIdVersion}` | Key handler service URI. |
| `intervene.key-handler.basic-auth` | `Basic {secret-string}` | Basic authentication secret details. replace `{secret-string}` with actual value. Required for key-handler service authentication. |
| `intervene.key-handler.secret-key.password` | `{password}` | Secret/private key password. Password used to decrypt the fetched private key. Replace with actual value.|

## How to download files from a Globus collection over HTTPS

> [!TIP]
> Remember to wrap all paths in the CLI arguments in quotes and always specify a prefix (e.g. `"globus:///","file:///..."`).

#### Help option
```
# Help option
$ java -jar globus-file-handler-cli-1.0.0.jar -h
```
#### Example 1: Original file download (default behaviour)
```
# Execute download
$ java -jar globus-file-handler-cli-1.0.0.jar -s|--globus_file_transfer_source_path {globus/file/path} -d|--globus_file_transfer_destination_path {local/file/download/path} -l|--file_size {file size in bytes}

# Example short option
$ java -jar globus-file-handler-cli-1.0.0.jar -s "golbus:///ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" -d "file:///Users/ashutosh/downloaded-files" -l 278705850

# Example long option
$ java -jar globus-file-handler-cli-1.0.0.jar --globus_file_transfer_source_path "golbus:///ashutosh@ebi.ac.uk/INTP00000000360/hapnest.pgen" --globus_file_transfer_destination_path "file:///Users/ashutosh/downloaded-files" --file_size 278705850
```
You can still use  `--spring.profiles.active={profile-name}` if you are using multi environment. Make sure you still supply existing profiles as mentioned in this document.

#### Example 2: Crypt4gh - Local Secret Key. Downloads encrypted file as decrypted (Decryption happens on the fly).

> [!TIP]
> Make sure you make config changes in `application-crypt4gh.properties` with correct values. Profile required to execute is `crypt4gh`.

This process works like:
* Transfer a file in a Globus collection over HTTPS.
* Decrypts on the fly using a local unencrypted secret key. So a cleartext file is output.

```
# Execute download
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s|--globus_file_transfer_source_path {globus:///globus/file/path} -d|--globus_file_transfer_destination_path {file:///local/file/download/path} -l|--file_size {file size in bytes} --crypt4gh -sk|-private-key {file:///local/file/secret/key/path}

# Example short option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=crypt4gh -s "golbus:///ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" -d "file:///Users/ashutosh/downloaded-files" -l 278705850 --crypt4gh --sk "/Users/ashutosh/downloaded-files/private.sec"

# Example long option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=crypt4gh --globus_file_transfer_source_path "golbus:///ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" --globus_file_transfer_destination_path "file:///Users/ashutosh/downloaded-files" --file_size 278705850 --crypt4gh -private_key "/Users/ashutosh/downloaded-files/private.sec"
```

#### Example 3: Crypt4gh - Secret Key is provisioned by Key Handler Service. Downloads encrypted file as decrypted (Decryption happens place on the fly).

> [!TIP]
> Make sure you make config changes in `application-crypt4gh-secret-manager.properties` with correct values. Profile required to execute is `crypt4gh-secret-manager`.

E.g. `secret-config.json` file. Properties refer to GCP Secret manger.
```
{
    "secretId": "NNCA6E59-1234-5678-4D01-5F2CV83C23E9",
    "secretIdVersion": "1"
}
```
This process works like:
* Fetches an encrypted crypt4gh secret key from the key handler service.
* Decrypts the secret key.
* Transfer a file in a Globus collection over HTTPS.
* Decrypts on the fly using a local secret key. So a cleartext file is output.

```
# Execute download
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active={profile} -s|--globus_file_transfer_source_path {globus:///globus/file/path} -d|--globus_file_transfer_destination_path {file:///local/file/download/path} -l|--file_size {file size in bytes} --crypt4gh -sk|-private-key {file:///local/file/secret/key/path}

# Example short option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=crypt4gh-secret-manager -s "globus:///ashutosh@ebi.ac.uk/Friday-10-05-09-46-Test/hapnest.pvar.c4gh" -d "file:///Users/ashutosh/Desktop/Intervene-Files/downloaded-files/hapnest.pvar" -l 215004174 --crypt4gh --sk "file:///Users/ashutosh/crypt4gh/globus-cli-test/096149F5-8566-437A-80A1-16BAFB65F74B-secret-config.json"

# Example long option
$ java -jar globus-file-handler-cli-1.0.0.jar --spring.profiles.active=crypt4gh-secret-manager --globus_file_transfer_source_path "golbus:///ashutosh@ebi.ac.uk/Sample_Set_Friday_02_Feb_14_11/hapnest.pvar.c4gh" --globus_file_transfer_destination_path "file:///Users/ashutosh/downloaded-files" --file_size 278705850 --crypt4gh -private_key "/Users/ashutosh/downloaded-files/private.sec"
```

## Build instructions (build your own jar/image)
Build jar with maven: [Make sure you have installed JDK 21+ & Maven 3.9+]
```
$ mvn clean package
```
Build a docker image: [Make sure you have istalled docker service]
```
$ docker build --secret id=MAVEN_SETTINGS,src=$HOME/.m2/settings.xml --build-arg VERSION={jar version} -f docker/crypt4gh/Dockerfile -t test .
```

> [!TIP]
> You'll need to set up a a token in the maven settings file (`settings.xml`) and mount it during build
