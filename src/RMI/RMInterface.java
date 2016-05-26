package RMI;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMInterface extends Remote {
	
	String tarefa(String message) throws RemoteException;
	
}