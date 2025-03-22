# FileManager
Monitors a specified directory for new files. When a file is created, it reads the content, backs it up with a timestamp, and optionally deletes the original. Includes retry mechanisms for reading and backing up files to ensure reliability, using WatchService for event handling and ExecutorService for background processing.

---

# File Backup and Monitoring System

## Description

This Java program monitors a specified directory for newly created files. When a file is created, it reads the content of the file and attempts to back it up to another directory. The program handles retries in case of failures when reading or backing up the file. If a backup is successful and a deletion flag (`deleteFile`) is set to "Y", it will delete the original file after backing it up.

## Features

- **Directory Monitoring**: Monitors a specified directory for file creation events.
- **File Reading with Retries**: Attempts to read the content of the created file with retry logic in case of failures.
- **File Backup**: Backs up the file to a backup directory with a timestamp to avoid overwriting files.
- **File Deletion**: Optionally deletes the original file after successfully backing it up (based on `deleteFile` flag).
- **Retry Mechanism**: Both reading and backing up the file have retry mechanisms in case of failure.

## Requirements

- Java 8 or higher
- Basic understanding of Java file I/O operations and thread management

## Configuration

### Parameters

The following parameters are configured in the code:

1. **IP Address** (`ip`):  
   Example: `"***.***.***.***"`  
   This is a placeholder and doesn't seem to be used in the code but might be used for future extensions.

2. **Drop File Directory** (`dropFilePath`):  
   Example: `"*****:/*****/*****/*****/DropFile"`  
   Directory where files will be watched for creation.

3. **Backup File Directory** (`backupFilePath`):  
   Example: `"*****:/*****/*****/*****/BackupFile"`  
   Directory where the backed-up files will be stored.

4. **Delete File Flag** (`deleteFile`):  
   Values: `"Y" | "N"`  
   If set to `"Y"`, the original file will be deleted after being backed up. If set to `"N"`, the file will remain in the original directory after the backup.

5. **Retry Parameters for Backup**:
    - **Retry Count** (`retry`):  
      Number of attempts to retry the backup operation. Default is `3`.
    - **Retry Delay** (`retryDelayMillis`):  
      Time in milliseconds to wait before retrying the backup operation. Default is `1000` milliseconds (1 second).

6. **Retry Parameters for Reading Files**:
    - **Read Retry Count** (`readRetryCount`):  
      Number of attempts to retry reading the file. Default is `3`.
    - **Read Retry Delay** (`readRetryDelayMillis`):  
      Time in milliseconds to wait before retrying the file reading operation. Default is `1000` milliseconds (1 second).

### WatchService

The program uses Java's `WatchService` to monitor a specific directory (`dropFilePath`) for file creation events. When a file is created, the program processes it by reading the content and backing it up.

### File Backup

When backing up the file, the program appends a timestamp to the file name to avoid overwriting existing backups.

### File Deletion

If the `deleteFile` flag is set to `"Y"`, the original file will be deleted after it is successfully backed up.

## How It Works

1. **Watch Directory**:  
   The program continuously watches the specified `dropFilePath` for new files using the `WatchService`.

2. **Handle File Creation Event**:  
   When a new file is detected, the program attempts to read the file's contents. If the file is locked or cannot be read, it retries based on the configured retry count and delay.

3. **Backup the File**:  
   Once the file is read successfully, it is backed up to the `backupFilePath`. The program generates a unique backup file name by appending the current timestamp to avoid overwriting any existing files.

4. **Retry Mechanism**:  
   Both file reading and backup operations include retry mechanisms in case of failures. These retries are limited to the number of configured retries, and each retry attempt waits for a specified delay before retrying.

5. **Delete File (Optional)**:  
   If the backup operation is successful and the `deleteFile` flag is set to `"Y"`, the original file is deleted from the `dropFilePath`.

## Example

### Configuration Example

```java
String ip = "***.***.***.***";
String dropFilePath = "*****:/*****/*****/*****/DropFile";
String backupFilePath = "*****:/*****/*****/*****/BackupFile";
String deleteFile = "Y";  // Delete original file after backup

int retry = 3;  // Retry 3 times for backup
long retryDelayMillis = 1000;  // Retry every 1 second
int readRetryCount = 3;  // Retry 3 times for reading the file
long readRetryDelayMillis = 1000;  // Retry every 1 second
```

In this example:
- Files created in the directory `"*****:/*****/*****/*****/DropFile"` will be backed up to `"*****:/*****/*****/*****/BackupFile"`.
- If the backup is successful, the original file will be deleted.

### Console Output

- **When a file is created**:  
  `Watching directory: *****:/*****/*****/*****/DropFile`  
  `Contents of the file: example.txt`  

- **When file is backed up**:  
  `File backed up successfully.`

- **If file is deleted**:  
  `DeleteFile successfully.`

- **Error Handling**:  
  If reading or backing up a file fails after the retry limit, the program will print an error message to the console.

## Running the Program

To run the program:

1. Ensure that you have Java 8 or higher installed.
2. Compile the code:
   ```sh
   javac Main.java
   ```
3. Run the program:
   ```sh
   java Main
   ```

The program will continuously monitor the specified `dropFilePath` directory for newly created files and process them as described.

## Troubleshooting

- **"Unable to read file after multiple retries"**:  
  This error can occur if the file is locked by another process or if there is an I/O issue. Ensure the file is accessible or increase the retry count/delay.
  
- **"Error during backup"**:  
  This can occur if there is an issue with file permissions or if the backup directory does not exist. Make sure the backup directory is valid and has the necessary permissions.

## License

This project is open-source and available for use under the MIT License.

---

## Contact Me

Feel free to reach out if you have any questions or need further assistance:

- **Email**: [phokeanghour12@gmail.com](mailto:phokeanghour12@gmail.com)
- **Telegram**: [@phokeanghour](https://t.me/phokeanghour)

[![Telegram](https://www.vectorlogo.zone/logos/telegram/telegram-ar21.svg)](https://t.me/phokeanghour)
[![LinkedIn](https://www.vectorlogo.zone/logos/linkedin/linkedin-ar21.svg)](https://www.linkedin.com/in/pho-keanghour-27133b21b/)

---

**Credit**: This project was created by **Pho Keanghour**.

---

Let me know if this works or if you'd like any more adjustments!
