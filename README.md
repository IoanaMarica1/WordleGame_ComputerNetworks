 Wordle Game: Networked Client-Server Application
This project is an advanced implementation of the classic Wordle game, designed as a distributed application. It demonstrates competency in network programming, concurrency, and cross-platform development. Technical Architecture
ComponentTechnologyKey Principles and RoleServerPython (Multi-threaded)Hosts the core game logic, manages sessions, and uses multithreading to handle concurrent client connections. The setup is Docker and Linux-ready.ClientJavaFX provides a responsive and cross-platform Graphical User Interface (GUI) for users. Network TCP and UDP implement bidirectional communication. TCP is used for reliable transmission of critical data (e.g., game state, win/loss status), and UDP can be utilized for time-sensitive status updates (checking if the word exists in the dictionary). Core Features Demonstrated
Concurrency: The multi-threaded server ensures stable performance when handling multiple simultaneous players.
Message Routing: Server-side logic correctly directs user input and game status updates to the appropriate game session.
Real-time Logic: Game state processing and user feedback are delivered instantly.
Cross-Platform Design: Separation of client (JavaFX) and server (Python - used in Docker container for Linux) allows for flexible deployment. 
