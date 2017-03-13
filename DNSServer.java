import java.io.*;
import java.net.*;
import java.util.*;
// import java.Array.*;
 
// 1. 本程式必須與 UdpClient.java 程式搭配執行，先執行本程式再執行 UdpClient。
// 2. 執行方法 : java UdpServer
 
public class DNSServer extends Thread {
    int mPort;    // 連接埠
    String mInterfaceName ;
    
    
    String RealDNSServer = "8.8.8.8" ;
    
    
 
    public static void main(String args[]) throws Exception {
        
        Enumeration<NetworkInterface> enumeration= NetworkInterface.getNetworkInterfaces(); 
        int i = 0 ; 
        while(enumeration.hasMoreElements())
        { 
          
          
          NetworkInterface networkInterface=enumeration.nextElement(); 
          if ( networkInterface.getHardwareAddress() != null )
          {
            System.out.println( networkInterface); 
            
            Enumeration <InetAddress> enu1 = networkInterface.getInetAddresses();
            
            while ( enu1.hasMoreElements() ) {
              InetAddress inetAddr = enu1.nextElement();
              System.out.println( inetAddr ); 
            } // while 
            
            
            System.out.println( "" ); 
          } // if
          
          
          i ++ ;
        } // while 
        
        
        
        
        System.out.println("Enter the interface address");
        Scanner scanner = new Scanner(System.in);
        String interfaceName = scanner.next();
        
        // String interfaceName = "192.168.1.102" ;
        
        DNSServer server = new DNSServer(53 , interfaceName ); // 建立 UdpServer 伺服器物件。
        
        System.out.println( "Start DNSserver..." ); 
        
        
        
        
            
        server.run();                           // 執行該伺服器。
    }
 
    public DNSServer(int pPort,String name ) {
        
        mPort = pPort;                            // 設定連接埠。
        
        mInterfaceName = name ;
        
        // run();
    }
 
    public void run() {
        final int SIZE = 1024;                    
        byte buffer[] = new byte[SIZE];            // 設定訊息暫存區
        
        
        for (int count = 0; ; count++) {
            try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            DatagramSocket socket = new DatagramSocket(mPort,InetAddress.getByName(mInterfaceName) );         // 設定接收的 UDP Socket.
            socket.receive(packet);                                    // 接收封包。
           
            
            try {
              // String msg = new String(buffer, 0, packet.getLength());    // 將接收訊息轉換為字串。
            
              // System.out.println( packet.getLength() +"" );                    // 印出接收到的訊息。
           
              HandleRequest( socket, packet ,buffer , packet.getLength() ) ;
            } finally {
                socket.close(); } // finally                                           // 關閉 UDP Socket.
            } catch ( Exception e ) {
            } // catch
        } // for
    } // run
    
    
    void HandleRequest( DatagramSocket ds,DatagramPacket packet,byte[] buffer , int length ) throws Exception {
        try {
                byte[] xid = GetDNSid( buffer ) ;
                
                

                
                if ( IsDNSQuerry ( buffer ) ) {
        
                        System.out.println( "   Querry!!!!" ); 
                        DealQuery( ds,packet,buffer,length ) ;
                } // DNS Querry 
                else if ( !IsDNSQuerry ( buffer ) ) { // it must a response
                        System.out.println( "   Response!!!!" ); 
                } // DNS response
                else { 
                        System.out.println( "I don't care!" ); 
                } ; // else 
                
                
                // System.out.println("") ;
                
                
                
                // System.out.println( "receive = "+ new String( buffer ,0 , length ) );
        } catch ( Exception e ) {
               System.out.println( " HandleRequest ERROR!!!!!!" );
        }
    } // HandleRequest
    
    
    byte[] GetDNSid( byte[] buffer ) {
        
        byte[] xid = new byte[2] ;
        
        xid[0] = buffer[0] ;
        xid[1] = buffer[1] ;
        
        return xid ;
    } // GetDHCPXid()
    
    
    
    
   boolean IsDNSQuerry( byte[] buffer ) {

        int num = 0 ;
        
        num = buffer[3] & 0xFF ;
        
        if ( num < 127 && num >= 0 ) return true ;
        
        return false ;
        
    } // GetDHCPXid()
    
    void DealQuery ( DatagramSocket ds,DatagramPacket packet,byte[] buffer , int length ) {
        
        
        try {
             
            
            InetAddress userIP = packet.getAddress();
            int userPort = packet.getPort();
            System.out.println("User的IP地址是："+userIP.getHostAddress());
            System.out.println("User的port是："+userPort);
              
            
            // System.out.println( "?????" );
            
            
            DatagramSocket DNSServerSocket = new DatagramSocket(); 
            
            DatagramPacket DNSServerPacket = 
                       new DatagramPacket( 
                                            buffer,
                                            buffer.length,
                                            InetAddress.getByName(RealDNSServer),
                                            53
                                          );
            
            DNSServerSocket.send( DNSServerPacket ) ;                
            DNSServerSocket.setSoTimeout(500);
            // System.out.println( " is send" );
            
            byte[] DNSServerBuf = new byte[1024];
      
            DatagramPacket getPacket = new DatagramPacket(DNSServerBuf,DNSServerBuf.length);
      
            DNSServerSocket.receive(getPacket);
            
            // System.out.println( "?????" );
            
            // String backMes = new String(DNSServerBuf,0,getPacket.getLength());
            System.out.println("接受方返回的消息："+ bytesToHexString(DNSServerBuf,getPacket.getLength()));
      

            // DNSServerSocket.close();
            
            
            
            DatagramPacket UserPacket = 
                       new DatagramPacket( 
                                            DNSServerBuf,
                                            getPacket.getLength(),
                                            userIP,
                                            userPort
                                          );
            
            
            ds.send( UserPacket ) ;
            DNSServerSocket.close();
      
            // ds.close();
        } catch ( Exception e ) {
           System.out.println( " Querry ERROR!!!!!!" );
        } // catch 
    } // DealQuery()
    
   
    
    
    public static byte[] ByteReplace( byte[] a , byte[] b, int index ) {
        
        
        
        for( int i = 0 ; i < b.length ; i ++ ) {
          a[ index+i ] = b[i] ;
        } // for 
        
        return a ;
        
    } // public static byte[] ByteReplace()
    
    
    
    
    
    
    
    public static int bytesToInt(byte[] bytes) throws Exception {
    
    
    int num = 0;
    
    try{
        
    num = bytes[0] & 0xFF;

    num |= ((bytes[1] << 8) & 0xFF00);

    num |= ((bytes[2] << 16) & 0xFF0000);

    num |= ((bytes[3] << 24) & 0xFF000000);
    }catch( Exception e) {
    }
    

    return num;

  }
  
  public static String bytesToHexString(byte[] bytes) throws Exception {
    
        
  int length = bytes.length;
  String hexString = "" ;      
        
  // Integer.toHexString( bytesToInt(xid) )      
  
  
  int num = 0 ;
  
  for( int i = 0 ; i < length ; i++ ) {
        num = bytes[i] & 0xFF;
        if ( (num/16) == 10 ) hexString += "A" ;
        else if ( (num/16) == 11 ) hexString += "B" ;
        else if ( (num/16) == 12 ) hexString += "C" ;
        else if ( (num/16) == 13 ) hexString += "D" ;
        else if ( (num/16) == 14 ) hexString += "E" ;
        else if ( (num/16) == 15 ) hexString += "F" ;
        else hexString += (num/16) ;
        
        if ( (num%16) == 10 ) hexString += "A" ;
        else if ( (num%16) == 11 ) hexString += "B" ;
        else if ( (num%16) == 12 ) hexString += "C" ;
        else if ( (num%16) == 13 ) hexString += "D" ;
        else if ( (num%16) == 14 ) hexString += "E" ;
        else if ( (num%16) == 15 ) hexString += "F" ;
        else hexString += (num%16) ;
        hexString += " " ;
        
        if ( i%8 == 0 ) hexString += " " ;
        if ( i%16 == 0 ) hexString += "\n" ;
        
        num = 0 ; 
  }
  
        
    
    return hexString;

  }
  
  
  public static String bytesToHexString(byte[] bytes, int length) throws Exception {
    
        
  
  String hexString = "\n" ;      
        
  // Integer.toHexString( bytesToInt(xid) )      
  
  
  int num = 0 ;
  
  for( int i = 0 ; i < length ; i++ ) {
        num = bytes[i] & 0xFF;
        if ( (num/16) == 10 ) hexString += "A" ;
        else if ( (num/16) == 11 ) hexString += "B" ;
        else if ( (num/16) == 12 ) hexString += "C" ;
        else if ( (num/16) == 13 ) hexString += "D" ;
        else if ( (num/16) == 14 ) hexString += "E" ;
        else if ( (num/16) == 15 ) hexString += "F" ;
        else hexString += (num/16) ;
        
        if ( (num%16) == 10 ) hexString += "A" ;
        else if ( (num%16) == 11 ) hexString += "B" ;
        else if ( (num%16) == 12 ) hexString += "C" ;
        else if ( (num%16) == 13 ) hexString += "D" ;
        else if ( (num%16) == 14 ) hexString += "E" ;
        else if ( (num%16) == 15 ) hexString += "F" ;
        else hexString += (num%16) ;
        
        hexString += " " ;
        
        if ( (i+1)%8 == 0 ) hexString += " " ;
        if ( (i+1)%16 == 0 ) hexString += "\n" ;
        
        num = 0 ; 
  }
  
        
    
    return hexString;

  }
    
    public static byte[] ByteAdd(byte[] a , byte[] b ) {

      byte[] bt = new byte[ (a.length +  b.length) ];

      for( int i = 0 ; i < a.length ; i++ ) {
        bt[i] = a[i] ;
      } // for 

      for( int i = 0 ; i < b.length ; i++ ) {
        bt[ (i + a.length) ] = b[i] ;
      } // for 

      return bt;

  }
  
  public static byte[] BytePadding( byte[]a , int size ) {
        
        byte[] bt = new byte[ a.length + size ] ;
        
        for( int i = 0 ; i < a.length ; i++ ) {
          bt[i] = a[i] ;
        } // for
        
        for( int i = a.length ; i < size ; i++ ) {
          bt[i] = 0x00 ;
        } // for
        
        return bt;
  } 
  
  public static byte[] intToByte(int i) {
        

      byte[] bt = new byte[1];
      
      bt[0] = (byte) (0xff & i);


      return bt;
  }
    
}




