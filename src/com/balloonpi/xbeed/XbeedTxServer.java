package com.balloonpi.xbeed;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * XbeedTxServer is TCP socket listener. When start connection, XbeedTxServer
 * makes new thread and delegate socket.
 * 
 * @author shintaro
 * @version 1.0
 */
public class XbeedTxServer implements Runnable {
	private static Logger logger = LogManager.getLogger();

	private ArrayList<XbeedTxServerThread> serverThreads = new ArrayList<XbeedTxServerThread>();

	boolean close_flag = false;
	int port;

	/**
	 * Constructor.
	 * 
	 * @param port
	 *            TCP port.
	 */
	public XbeedTxServer(int port) {
		this.port = port;
	}

	/**
	 * Start to listen socket. When new connection starts, make thread for new
	 * connection.
	 */
	public void run() {
		try {
			logger.debug("Start TX server listener.");
			ServerSocket serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(10000);
			while (close_flag == false) {
				try {
					Socket socket = serverSocket.accept();
					logger.debug(
							"Accept TX server socket and make thread(Port: {}).",
							port);
					XbeedTxServerThread txThread = new XbeedTxServerThread(
							socket);
					Thread thread = new Thread(txThread);
					thread.start();
					serverThreads.add(txThread);
				} catch (SocketTimeoutException e) {
					logger.debug("Server socket timeout happens.");
				}

			}
			logger.debug("Stop TX server listener.");
			serverSocket.close();

			// Stop every thread.
			Iterator<XbeedTxServerThread> it = serverThreads.iterator();
			while (it.hasNext()) {
				it.next().stop();
			}

		} catch (IOException e) {
			logger.error("", e);
		}
	}

	/**
	 * Top this thread.
	 */
	public void stop() {
		close_flag = true;
	}
}
