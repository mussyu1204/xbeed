xbeed
=====

Xbeed is the daemon to communicate with Xbee from several applications.
 Xbee--(uart)--Xbeed--(TCP)--Xbeed Client AP

1. Configuration file
Xbeed reads property file on startup. You have to put "xbeed.properties" to Xbeed class path.

# port name of xbee
xbeed.com_port=COM5
# bit rate of xbee
xbeed.bit_rate=9600
# TCP port Xbeed opens for TX
xbeed.tx_server_port=1111
# TCP port Xbeed opens for RX
xbeed.rx_server_port=2222
# Stop file name. If this file exists, Xbeed stops.
xbeed.stop_file_name=F:/share/99_work/shintaro/workspace/xbeed/xbeedStop

2. Xbee settings
Xbee must be API mode. Xbeed adds header, datab length, checksum to a data from Xbeed Client AP.

3. Xbeed TCP I/F
Xbeed opens two TCP port.

3.1. TX port
If you connect to TX port, you can send a data to Xbeed. (You can connect several clients.)
When Xbeed receives a data from client, Xbeed send it to the Xbee.
The protocol between Xbeed and Xbeed client is below.
 1Byte      : start byte(0xFF)
 2-3Byte    : data length of Frame Data(n)
 4-(n+3)Byte: Frame Data(Xbee Frame Data of API mode)
 n+4Byte    : Stop byte(0xFF)
 
3.2. RX port
If you connect to RX port, you can receive a data from the Xbee when the Xbee send data. 
(You can connect several clients.)
The protocol between Xbeed and Xbeed client is same to TX port.
