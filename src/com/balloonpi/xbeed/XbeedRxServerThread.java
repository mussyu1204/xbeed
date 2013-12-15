package com.balloonpi.xbeed;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.balloonpi.util.HexString;

/**
 * XbeedTxServerThread receive data from queue and send it to socket.
 * 
 * @author shintaro
 * @version 1.0
 */
public class XbeedRxServerThread implements Runnable {
	private static Logger logger = LogManager.getLogger();

	Socket socket = null;
	OutputStream os = null;
	boolean close_flag = false;

	HexString hs = new HexString();

	/**
	 * Constructor.
	 * 
	 * @param socket
	 */
	public XbeedRxServerThread(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Start to read queue data. When receive data, send it to socket.
	 */
	public void run() {
		UUID id = UUID.randomUUID();
		Byte[] rx_data;
		InputStream is;

		logger.debug("Start RX server thread.");
		logger.debug("Regist to RX queue. {}", id.toString());
		XbeedQueue.regist(id.toString());

		try {
			socket.setSoTimeout(100);
			os = socket.getOutputStream();
			is = socket.getInputStream();

			while (close_flag == false) {
				if (socket.isClosed()) {
					close_flag = true;
				} else {
					rx_data = XbeedQueue.getRx(id.toString());
					if (rx_data != null) {
						logger.debug("Get frame_data from RX queue. {}",
								hs.toHexString(rx_data));
						// header
						os.write(0xFF);
						// data length
						int length = rx_data.length;
						os.write(0xFF & (length >> 8));
						os.write(0xFF & (length));

						for (byte tmp : rx_data) {
							os.write(tmp);
						}
						// footer
						os.write(0xFF);
					}
				}
				// If closed socket by client, stop this thread.
				try {
					if (is.read() == -1) {
						break;
					}
				} catch (SocketTimeoutException e) {
					// Nothing to do
				}
			}
			os.close();
			socket.close();
		} catch (Exception e) {
			logger.error("", e);
		}
		logger.debug("delete from RX queue. {}", id.toString());
		XbeedQueue.delete(id.toString());
	}

	/**
	 * Stop this thread.
	 */
	public void stop() {
		close_flag = true;
	}
}
