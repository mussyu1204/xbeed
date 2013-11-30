package com.balloonpi.xbeed;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.balloonpi.util.HexString;

/**
 * シリアルデータ受信用のイベントりリスナ実装クラス。
 * 受信データを解析し、フレームデータをキューに詰める。
 * @author shintaro
 *
 */
public class SerialListener implements SerialPortEventListener {
	private static Logger logger = LogManager.getLogger();
	
	private byte XBEE_START_DELIMITER = 0x7E;
	
	private InputStream in;

	int cur_position = 0;
	int length = 0;
	ArrayList<Byte> frame_data = new ArrayList<Byte>();
	byte check_sum = 0;

	HexString hs = new HexString();
	
	/**
	 * コンストラクタ。
	 * 
	 * @param in InputStream
	 */
	public SerialListener(InputStream in) {
		this.in = in;
	}

	/**
	 * シリアルポートの受信用リスナー。
	 */
	@Override
	public void serialEvent(SerialPortEvent event) {
		logger.debug("Serial Event happens.");
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			int received_data = 0;
			while (true) {
				try {
					received_data = in.read();
					if (received_data == -1)
						break;
					else {
						parseXbeeData((byte)received_data);
					}
				} catch (IOException e) {
					logger.error("",e);
				}
			}

		}
	}

	/**
	 * xbee受信データのスタートフラグ、メッセージ長、チェックサムの解析を行う。
	 * 
	 * @param rcv_data
	 */
	private void parseXbeeData(byte rcv_data) {
		logger.debug("Receive data. 0x{}", Integer.toHexString(0xFF & rcv_data));
		if (cur_position == 0 && rcv_data == XBEE_START_DELIMITER) {
			cur_position = 1;
		}
		if (cur_position == 1) {
			length = rcv_data << 8;
			cur_position = 2;
		}
		if (cur_position == 2) {
			length |= rcv_data;
			cur_position = 3;
			logger.debug("Length is 0x{}",Integer.toHexString(length & 0xFFFF));
		}
		if (cur_position >= 3 && cur_position < (length + 3)) {
			frame_data.add(rcv_data);
			check_sum += rcv_data;
		}
		if (cur_position == (length + 3)) {
			check_sum = (byte) (0xFF - rcv_data);
			if (check_sum == rcv_data) {
				logger.debug("Set frame data to RX queue. {}",hs.toHexString(frame_data.toArray(new Byte[0])));
				XbeedQueue.setRx(frame_data.toArray(new Byte[0]));
			}
			else{
				logger.warn("Check sum does't match. rcv:{}, cul:{}",rcv_data, check_sum);
			}
			cur_position = 0;
			check_sum = 0;
			frame_data.clear();
		}
	}
}
