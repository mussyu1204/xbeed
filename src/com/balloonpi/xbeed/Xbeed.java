package com.balloonpi.xbeed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Xbeed main process. This process loads the property file(xbeed.properties)
 * ,connects to serial port and start up some threads for TCP connection.
 * 
 * @author shintaro
 * @version 1.0
 * 
 */
public class Xbeed {
	private static Logger logger = LogManager.getLogger();

	public static void main(String argv[]) {
		Properties prop = new Properties();

		try {
			// open property file.
			InputStream inputStream = XbeedComm.class
					.getResourceAsStream("/xbeed.properties");
			prop.load(inputStream);

			// get property values.
			String com_port = prop.getProperty("xbeed.com_port");
			int bit_rate = Integer.parseInt(prop.getProperty("xbeed.bit_rate"));
			int tx_server_port = Integer.parseInt(prop
					.getProperty("xbeed.tx_server_port"));
			int rx_server_port = Integer.parseInt(prop
					.getProperty("xbeed.rx_server_port"));
			String stop_file_name = prop.getProperty("xbeed.stop_file_name");

			// start serial communiction thread.
			logger.debug("Start XbeedComm.");
			XbeedComm comm = new XbeedComm(com_port, bit_rate);
			comm.start();

			// start socket server thread for TX.
			logger.debug("Start TX Server.");
			XbeedTxServer tx_server = new XbeedTxServer(tx_server_port);
			Thread txThread = new Thread(tx_server);
			txThread.start();

			// start socket server thread for RX.
			logger.debug("Start RX Server.");
			XbeedRxServer rx_server = new XbeedRxServer(rx_server_port);
			Thread rxThread = new Thread(rx_server);
			rxThread.start();

			// wait when stop file exists.
			File file = new File(stop_file_name);
			while (file.exists() == false) {
				Thread.sleep(1000);
			}

			// exit process.
			logger.debug("Stoping process.");
			comm.stop();
			tx_server.stop();
			rx_server.stop();

			logger.debug("Finish!");

		} catch (IOException e) {
			logger.error("Can't open properties file.", e);
		} catch (InterruptedException e) {
			logger.error("Can't open properties file.", e);
		}
	}
}
