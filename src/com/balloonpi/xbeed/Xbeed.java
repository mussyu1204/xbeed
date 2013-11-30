package com.balloonpi.xbeed;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Xbeed {
	private static Logger logger = LogManager.getLogger();

	public static void main(String argv[]) {
		Properties prop = new Properties();

		try {
			// プロパティファイルを開く
			InputStream inputStream = XbeedComm.class
					.getResourceAsStream("/xbeed.properties");
			prop.load(inputStream);

			// 設定値の取得
			String com_port = prop.getProperty("xbeed.com_port");
			int bit_rate = Integer.parseInt(prop.getProperty("xbeed.bit_rate"));
			int tx_server_port = Integer.parseInt(prop
					.getProperty("xbeed.tx_server_port"));
			int rx_server_port = Integer.parseInt(prop
					.getProperty("xbeed.rx_server_port"));
			String stop_file_name = prop.getProperty("xbeed.stop_file_name");

			// シリアル通信用
			logger.debug("Start XbeedComm.");
			XbeedComm comm = new XbeedComm(com_port, bit_rate);
			comm.start();

			// ソケット用送信サーバの起動
			logger.debug("Start TX Server.");
			XbeedTxServer tx_server = new XbeedTxServer(tx_server_port);
			Thread txThread = new Thread(tx_server);
			txThread.start();

			// ソケット用受信サーバの起動
			logger.debug("Start RX Server.");
			XbeedRxServer rx_server = new XbeedRxServer(rx_server_port);
			Thread rxThread = new Thread(rx_server);
			rxThread.start();

			// 停止ファイルが置かれるまで待機
			File file = new File(stop_file_name);
			while (file.exists() == false) {
				Thread.sleep(1000);
			}

			// 終了処理
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
