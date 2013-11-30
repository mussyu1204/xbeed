package com.balloonpi.xbeed;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.balloonpi.util.HexString;

public class XbeedRxServerThread implements Runnable {
	private static Logger logger = LogManager.getLogger();

	Socket socket = null;
	OutputStream os = null;
	boolean close_flag = false;

	HexString hs = new HexString();

	public XbeedRxServerThread(Socket socket) {
		this.socket = socket;
	}

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
						// ヘッダ
						os.write(0xFF);
						// データ長
						int length = rx_data.length;
						os.write(0xFF & (length >> 8));
						os.write(0xFF & (length));
						
						for (byte tmp : rx_data) {
							os.write(tmp);
						}
						// フッタ
						os.write(0xFF);
					}
				}
				// 相手からsocketをクローズされている場合、ループから抜け処理を終了する。
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

	public void stop() {
		close_flag = true;
	}
}
