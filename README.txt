
COMO COMPILAR:

	(usar java sdk 1.8+)
	javac -d bin src/Chunk/*.java src/DB/*.java src/Encryption/*.java src/Filesystem/*.java src/Initiators/*.java src/Listeners/*.java src/Message/*.java src/Peer/*.java src/RMI/*.java src/Server/*.java src/Utils/*.java


COMO EXECUTAR:
	Este projeto usa RMI como “protocolo de transporte”.
	Exemplo de utilização:

	1)LANÇAR RMI - TERMINAL 1:
		cd bin
		rmiregistry & (UNIX)
		start rmiregistry (Windows)
		
	2)TERMINAL 2:
		cd bin
		java Server.Server 2220 224.0.0.233 2201 224.0.0.234 2202 224.0.0.235 2203
		(java Server.Server <Server Port> <mcaddress> <mcport> <mdbaddress> <mdbport> <mdraddress> <mdrport>)

	3)TERMINAL 3:
		cd bin
		java -Djava.net.preferIPv4Stack=true Peer.Peer 1 192.168.1.1 2220 74
		(java (-Djava.net.preferIPv4Stack=true) Peer.Peer <id> <Identification Server Address> <Identification Server Port> <size>)
		A aplicação irá pedir a password para se identificar no servidor, se o id do peer não existir no servidor irá ser criado com a password introduzida.

	4) TERMINAL 4:
		cd bin
		java -Djava.net.preferIPv4Stack=true Peer.Peer 2 192.168.1.1 2220 74
		(java (-Djava.net.preferIPv4Stack=true) Peer.Peer <id> <Identification Server Address> <Identification Server Port> <size>)
		A aplicação irá pedir a password para se identificar no servidor, se o id do peer não existir no servidor irá ser criado com a password introduzida.
		
	[desta forma, temos 2 peers; para aumentar o numero de peers, devemos repetir o passo 2 tendo em atenção de não repetir os ids]

	5) TERMINAL 5:
		cd bin
		PARA BACKUP:
			java RMI.TestApp 1 BACKUP Tulips.jpg 1
			(java RMI.TestApp <id> BACKUP <file> <replication_degree>)
		PARA RESTORE:
			java RMI.TestApp 1 RESTORE Tulips.jpg
			(java RMI.TestApp <id> RESTORE <file>)
		PARA DELETE:
			java RMI.TestApp 1 DELETE Tulips.jpg
			(java RMI.TestApp 1 DELETE <file>)
		PARA RECLAIM:
			java RMI.TestApp 2 RECLAIM 3
			(java RMI.TestApp <peer_id> RECLAIM <new_space_available_in_chunks>