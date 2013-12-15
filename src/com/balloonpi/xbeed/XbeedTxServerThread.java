package com.balloonpi.xbeed;

import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.balloonpi.util.HexString;

/**
 * XbeedTxServerThread receive data from socket, parse and put it to queue.
 * 
 * @author shintaro
 * @version 1.0
 */
public class XbeedTxServerThread implements Runnable {
	private static Logger logger = LogManager.getLogger();

	Socket socket = null;
	InputStream is = null;
	boolean close_flag = false;

	HexString hs = new HexString();

	ArrayList<Byte> rcv_data_array = new ArrayList<Byte>();

	int length = 0;
	int position = 0;

	/**
	 * Constructor.
	 * 
	 * @param socket
	 */
	public XbeedTxServerThread(Socket socket) {
		this.socket = socket;
	}

	/**
	 * Start to read socket data and parse. When receive one meaningful data,
	 * put it to queue.
	 */
	public void run() {
		logger.debug("Start TX server thread.");

		try {
			socket.setSoTimeout(10000);
			is = socket.getInputStream();
			int rcv_data;
			byte rcv_data_b;

			while (close_flag == false) {
				try {
					if (socket.isClosed()) {
						close_flag = true;
					} else {
						rcv_data = is.read();
						rcv_data_b = (byte) (0xFF & rcv_data);
						if (rcv_data == -1) {
							break;
						} else if (parse(rcv_data_b) == true) {
							logger.debug("Set TX queue. {}", hs
									.toHexString(rcv_data_array
											.toArray(new Byte[0])));
							XbeedQueue.setTx(rcv_data_array
									.toArray(new Byte[0]));
							rcv_data_array.clear();
						}
					}
				} catch (SocketTimeoutException e) {
					logger.debug("Socket timeout happens.");
				} finally {

				}
			}
			logger.debug("Exit TX Server thread.");
			is.close();
			socket.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	/**
	 * parse receive data from TCP socket.
	 * 
	 * @param rcv_data_tmp
	 * @return if parse succeed and be able to get data, return true.
	 */
	private boolean parse(byte rcv_data_tmp) {

		logger.debug("Receive from client. 0x{}",
				Integer.toHexString((0xFF & rcv_data_tmp)));
		if (position == 0 && (0xFF & rcv_data_tmp) == 0xFF) {
			position = 1;
			length = 0;
			rcv_data_array.clear();
			return false;
		} else if (position == 1) {
			length = (int) (rcv_data_tmp) << 8;
			position = 2;
			return false;
		} else if (position == 2) {
			length += rcv_data_tmp;
			position = 3;
			return false;
		} else if (position > 2 && position <= 2 + length) {
			rcv_data_array.add(rcv_data_tmp);
			position++;
			return false;
		} else if (position == 3 + length && (0xFF & rcv_data_tmp) == 0xFF) {
			position = 0;
			return true;
		}
		return false;
	}

	/**
	 * Stop this thread.
	 */
	public void stop() {
		close_flag = true;
	}
}
