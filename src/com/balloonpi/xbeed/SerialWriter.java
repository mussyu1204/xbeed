package com.balloonpi.xbeed;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.balloonpi.util.HexString;

public class SerialWriter implements Runnable {
	private static Logger logger = LogManager.getLogger();
	private byte XBEE_START_DELIMITER = 0x7E;

	private OutputStream out;
	private boolean stop_flag = false;

	HexString hs = new HexString();

	public SerialWriter(OutputStream out) {
		this.out = out;
	}

	public void run() {
		logger.debug("Start Serial Writer.");
		ArrayList<Byte> tx_data = new ArrayList<Byte>();
		int length = 0;
		byte check_sum = 0;
		Byte[] frame_data;
		try {
			while (stop_flag == false) {
				frame_data = XbeedQueue.getTx();
				if (frame_data != null) {
					logger.debug("Get TX frame data from queue. {}",
							hs.toHexString(frame_data));

					// set start byte
					tx_data.add(XBEE_START_DELIMITER);

					// set length
					length = frame_data.length;
					tx_data.add((byte) (0xFF & (length >> 8)));
					tx_data.add((byte) (0xFF & length));

					// set frame data
					for (byte tmp : frame_data) {
						tx_data.add(tmp);
						check_sum += tmp;
					}

					check_sum = (byte) (0xFF - check_sum);
					tx_data.add(check_sum);

					// send data
					logger.debug("Send message. {}",
							hs.toHexString(tx_data.toArray(new Byte[0])));
					Iterator<Byte> it = tx_data.iterator();
					while (it.hasNext()) {
						this.out.write(it.next());
					}
					// 後処理
					tx_data.clear();
					check_sum = 0;
				}
				Thread.sleep(100);
			}
			logger.debug("Stop Serial Writer.");
		} catch (IOException e) {
			logger.error("", e);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	/**
	 * スレッドを停止させる。
	 */
	public void stop() {
		stop_flag = true;
	}
}
