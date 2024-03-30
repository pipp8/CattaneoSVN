package client;

import java.rmi.Remote;



	public interface Echo extends Remote {
        String echo(String e);
}

