package smsserver.utils;

public class Utils {

	public static void dump(String str, byte [] data){
		//data = n.getData();
		System.out.println(str);
		System.out.println("Length: "+data.length);
		for (int i=0; i<data.length; i++){
			if (((i)%8)==0) System.out.print(i+"\t : ");
			if (data[i]<100) System.out.print(" ");
			System.out.print(data[i] + " ");
			if (((i+1)%8)==0) System.out.println();
		}
		System.out.println();
	}
	
	public static String numberFromUrl(String url) {
		String s = url.substring(6);
		return (s.split(":"))[0];
	}
	
	public static byte[] hexStringToBytes(String hex) {

		byte[] bts = new byte[hex.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
		}
		return bts;
	} 
	
	private static char[] hexChar = {
	        '0' , '1' , '2' , '3' ,
	        '4' , '5' , '6' , '7' ,
	        '8' , '9' , 'A' , 'B' ,
	        'C' , 'D' , 'E' , 'F' 
	};
 
	public static String bytesToHexString ( byte[] b ) {
	    StringBuffer sb = new StringBuffer( b.length * 2 );
	    for ( int i=0; i<b.length; i++ ) {
	        // look up high nibble char
	        sb.append( hexChar [( b[i] & 0xf0 ) >>> 4] ); // fill left with zero bits
 
	        // look up low nibble char
	        sb.append( hexChar [b[i] & 0x0f] );
	    }
	    return sb.toString();
   }
	
}

