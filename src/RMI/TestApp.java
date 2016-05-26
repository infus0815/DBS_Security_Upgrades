package RMI;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class TestApp {
	public static void main(String[] args) {	// java TestApp <remote_object_name> <sub_protocol> <opnd_1> <opnd_2>
		//String host;
		String str, cmd, resp;
		
		//host = (args[0] == "null") ? null : args[0];
		
		try{
			Registry registry = LocateRegistry.getRegistry(null, 1099);	// host = null
			RMInterface stub = (RMInterface) registry.lookup(args[0]);	// args[0]=<remote_object_name>=<peer_ap>
			
			str = new String("");
			cmd = args[1];
			if (cmd.equals("BACKUP") && args.length == 4){
				str= cmd+" "+args[2]+" "+args[3];
				
			}
			else if (cmd.equals("RESTORE") && args.length == 3){	// doesnt need replication degree (?)
				str= cmd+" "+args[2];
			}
			else if (cmd.equals("DELETE") && args.length == 3){		// doesnt need replication degree (?)
				str= cmd+" "+args[2];
			}
			else if (cmd.equals("RECLAIM") && args.length == 3){		// doesnt need replication degree (?)
				str= cmd+" "+args[2];
			}
			else{
				System.out.println("USAGE: java TestApp <remote_object_name> <sub_protocol> <opnd_1> <opnd_2>");
				System.exit(-1);
			}
			System.out.println("ENVIEI: "+str);
			resp= stub.tarefa(str);
			System.out.println("RECEBI: "+ resp);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}