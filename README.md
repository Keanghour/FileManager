# FileManager
Monitors a specified directory for new files. When a file is created, it reads the content, backs it up with a timestamp, and optionally deletes the original. Includes retry mechanisms for reading and backing up files to ensure reliability, using WatchService for event handling and ExecutorService for background processing.
