# File-Share
### Introduction ###
File-Share is a terminal based file sharing system developed in JAVA 8. It is based on *Peer-2-Peer(P2P)* network architecture in which all client nodes are at equal level and there is no central point of control within the network. The project makes use of Multi-threading and Socket Programming concepts.

### Features ###
* Manage list of online users.
* Search files over the network.
* Download a specific file from other peer.

### System Components ###
##### Main Server #####
Every client is connected to the main server. Main server maintains the list of all connected clients and shares this list with every newly connected client.
##### Client #####
Since, it is a P2P network architecture, every node can act as either Server or client. Hence, every Client can take up two roles:
* Mini-Server: It handles/responds to search and download requests from other peers.
* Mini-Client: It initiates the search requests over the network and consume services from other peers.
