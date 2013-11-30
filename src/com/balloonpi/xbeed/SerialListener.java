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
 * �V���A���f�[�^��M�p�̃C�x���g�胊�X�i�����N���X�B
 * ��M�f�[�^����͂��A�t���[���f�[�^���L���[�ɋl�߂�B
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
	 * �R���X�g���N�^�B
	 * 
	 * @param in InputStream
	 */
	public SerialListener(InputStream in) {
		this.in = in;
	}

	/**
	 * �V���A���|�[�g�̎�M�p���X�i�[�B
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
	 * xbee��M�f�[�^�̃X�^�[�g�t���O�A���b�Z�[�W���A�`�F�b�N�T���̉�͂��s���B
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
