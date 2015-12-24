package de.pepe4u.space.messenger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DirectMessageServerThread extends Thread {
	
	private ServerSocket server;
	private MessageManager mm;
	private boolean keepRunning;
	
	public DirectMessageServerThread(MessageManager mm , int port) throws IOException {
		server = new ServerSocket(port);
		keepRunning = true;
		//server.setSoTimeout(5000);
		this.mm = mm;
	}

	@Override
	public void run() {
		while(keepRunning)
		{
			try {
				Socket socket;
				socket = server.accept();
				if(socket != null && socket.isConnected())
				{
					new DirectMessageServerWorkerThread(mm, socket).start();
				}
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}

	public void shutdownThread()
	{
		try {
			keepRunning = false;
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
