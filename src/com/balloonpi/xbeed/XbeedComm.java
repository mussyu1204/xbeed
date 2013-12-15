package com.balloonpi.xbeed;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.balloonpi.util.HexString;

/**
 * XbeedComm is class for serial communication.
 * @author shintaro
 * @version 1.0
 */
public class XbeedComm {
	private static Logger logger = LogManager.getLogger();

	HexString hs = new HexString();

	InputStream in;
	OutputStream out;

	SerialWriter sw;
	SerialReader sr;
	Thread sw_thread;
	Thread sr_thread;

	SerialPort port;

	String com_port;
	int bit_rate;

	/**
	 * Constructor.
	 * 
	 * @param com_port
	 *            Serial port name.
	 * @param bit_rate
	 */
	public XbeedComm(String com_port, int bit_rate) {

		this.com_port = com_port;
		this.bit_rate = bit_rate;

	}

	/**
	 * Open serial port and start up TX/RX threads.
	 */
	public void start() {
		try {
			// Configure serial port and connect.
			logger.debug("Open serial port(COM:{})", com_port);
			CommPortIdentifier portId = CommPortIdentifier
					.getPortIdentifier(com_port);
			port = (SerialPort) portId.open("Xbeed", 1000);

			logger.debug("Set bit rate to {}", bit_rate);
			port.setSerialPortParams(bit_rate, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

			in = port.getInputStream();
			out = port.getOutputStream();

			// Set timeout to 1 second
			port.enableReceiveTimeout(1000);

			// Startup TX thread.
			sw = new SerialWriter(out);
			sw_thread = new Thread(sw);
			sw_thread.start();

			// Startup RX thread.
			sr = new SerialReader(in);
			sr_thread = new Thread(sr);
			sr_thread.start();

			port.notifyOnDataAvailable(true);

		} catch (NoSuchPortException e) {
			logger.error("", e);
		} catch (PortInUseException e) {
			logger.error("", e);
		} catch (UnsupportedCommOperationException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("", e);
		}
	}

	/**
	 * Stop all threads for serial communication.
	 */
	public void stop() {
		// Stop TX thread.
		logger.debug("Stop Serial Writer.");
		if (sw != null) {
			sw.stop();
			while (sw_thread.isAlive() == true) {
				try {
					logger.debug("Serial Writer Thread is still alive. Wait 1000 msec.");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
		}

		// Stop RX thread
		logger.debug("Stop Serial Reader.");
		if (sr != null) {
			sr.stop();
			while (sr_thread.isAlive() == true) {
				try {
					logger.debug("Serial Reader Thread is still alive. Wait 1000 msec.");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("", e);
				}
			}
		}

		// Close serial port.
		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		} catch (IOException e) {
			logger.error("", e);
		}
		if (port != null) {
			port.close();
		}
	}

}
