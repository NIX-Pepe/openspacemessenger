package de.pepe4u.space.messenger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class DirectMessageServerWorkerThread extends Thread {

	private Socket s;
	private MessageManager mm;
	
	public DirectMessageServerWorkerThread(MessageManager mm, Socket s) {
		this.s = s;
		this.mm = mm;
	}
	
	@Override
	public void run() {
		super.run();
		try{
			if(s!=null)
			{
				/**
				 * Pretty simple... set a timeout, read a line and process the line.
				 * After that, connection is closed.
				 */
				s.setSoTimeout(5000);
				BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
				String line = br.readLine();
				if(line != null)
				{
					mm.processDirectTelegramm(line, s.getInetAddress().getHostAddress());
				}
				s.close();
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
