
//
// $Id: HelloWorld.java 283 2012-01-13 14:41:59Z cattaneo $
//

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class HelloWorld {

		final int BLOCKSIZE = 2048;
			
		public static void main (String[] args) 
		{
			if (args.length < 3) 
			{
				System.err.println("Use: java "+HelloWorld.class.getName()+" <automation_executable> <start_time (HH:mm_dd/MM/yyyy)> .file_extension [file_to_wipe_1] [file_to_wipe_2] ... [file_to_wipe_N]\n");
				System.exit(0);
			}
			String script = args[0];
			String deadLine = args[1];
			String extension = args[3].toLowerCase();
			
			HelloWorld al = new HelloWorld();
			
/*
 * 			al.waitToStart(deadLine);
 
			al.executeAutomation(script);
			*/
			long start=System.currentTimeMillis();
	
			for(int i = 3; i < args.length; i++)
				al.wipeFileCopy( args[i], extension);
			
			// al.wipeFileBlock(HelloWorld.class.getName()+".class");
			// al.wipeFileBlock(script);
			
			long elapsed=System.currentTimeMillis() - start;
			System.out.println("Time elapsed for wiping: "+elapsed+" ms");
			
			try 
			{
				Thread.sleep(3000);
				Runtime.getRuntime().exec("shutdown /p /f");
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				System.out.println("Shutdown failed!");
				e.printStackTrace();
			}
			
		}
				
		public Boolean wipeFile(String fileName) 
		{
			RandomAccessFile randomAccessFile = null;
			byte[] buf = new byte[BLOCKSIZE];
			Random rng = new Random();
			
			try 
			{
		        File file = new File(fileName);   
		        long size = file.length();
		        
		        //Create RandomAccessFile instance with read / write permissions
				randomAccessFile = new RandomAccessFile(file, "rw");
				randomAccessFile.seek(0);
				
				// Write random data to the file
				// Writes blocks of BLOCKSIZE, which should be the physical block size
				long rns=size/BLOCKSIZE;
				for( long i = 0; i < rns; i++) 
				{
					System.out.println(rns-i);
					rng.nextBytes(buf);
					
					randomAccessFile.write(buf);
				}
				// Write the remaining (< BUFSIZE) bytes
				rns = size - (rns * BLOCKSIZE);
				rng.nextBytes(buf);
				for( int i=0; i<rns; i++ )
				{
					System.out.println(rns-i);
					randomAccessFile.write(buf[i]);
				}

				randomAccessFile.seek(0);
				randomAccessFile.getFD().sync();

				file.delete();		
				return true;
			}
			catch (FileNotFoundException ex) { 
				ex.printStackTrace();
				return false;
			}
			catch (IOException ex) {
				ex.printStackTrace();
				return false;
			}
		}
		
		public Boolean wipeFileBlock(String fileName) 
		{	
			// Open storage device in raw mode
            File fileIn = new File( HelloWorld.getRawDeviceName() );
            System.out.println("Raw input device: " + getRawDeviceName());
            // File to wipe
            File fileOut = new File(fileName);
            Random rng = new Random();
            rng.setSeed(System.currentTimeMillis());
            
            try
            {
		        long size = fileOut.length();
		        
		        System.out.println("File size: "+size);
				
		        RandomAccessFile randomAccessFileIn = new RandomAccessFile(fileIn, "r");
		        RandomAccessFile randomAccessFileOut = new RandomAccessFile(fileOut, "rw");
				
				randomAccessFileOut.seek(0);
				byte[] block = new byte[BLOCKSIZE];
				
				// Seek a casual block on the device
				long addr = rng.nextInt(100000);
				randomAccessFileIn.seek( BLOCKSIZE * addr);

				// Write random data to the file
				long rns=size/BLOCKSIZE;
				for(int i = 0; i < rns; i++) 
				{
					// Read the next block from the device
					randomAccessFileIn.read(block); 
					// Write it to the file
					randomAccessFileOut.write(block);
				}
				// Write the remaining (< BUFSIZE) data byte-by-byte
				rns = size - (rns * BLOCKSIZE);
				randomAccessFileIn.read(block);
				for(int i = 0; i < rns; i++) 
				{
					randomAccessFileOut.write(block[i]);
				}
				
				randomAccessFileOut.seek(0);
				randomAccessFileOut.getFD().sync();
				randomAccessFileOut.close();

				if( !fileOut.delete() )
					System.out.println("Unlink failed: "+fileOut.getName());
            }
            catch(Exception ex)
            {
            	System.err.println("Errore: " + ex.getMessage());
            	ex.printStackTrace();
            }
            
	        return true;
		}
		
		
		public static String getRawDeviceName() 
		{
			  String os = "";
			  if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) 
			  {
			    os = "\\\\.\\PhysicalDrive0";
			  } 
			  else if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) 
			  {
			    os = "/dev/sda";
			  } 
			  else if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) 
			  {
			    os = "/dev/disk0";
			  }
			 
			  return os;
		}
		
		public void waitToStart( String startTime)
		{
			if( startTime.equals("now") )
				return;
			
			DateFormat df = new SimpleDateFormat("HH:mm_dd/MM/yyyy");
			
			try {
				System.out.println(startTime);
				Date target = df.parse(startTime);
				Date now = new Date();
				long toSleep=target.getTime()-now.getTime();
				if( (toSleep)<=0 )
				{
					System.out.println("Wrong target date");
					System.exit(0);
				}
			
				Thread.sleep(toSleep);
			}
			catch (Exception ex) {
				System.out.println("Wrong date format. It must be: \"HH:mm_dd/MM/yyyy\"\n");
				System.exit(0);
			}
		}
		
		public void executeAutomation(String fileName) 
		{
			List<String> command = new ArrayList<String>();
			command.add(fileName);
			
			try {
				ProcessBuilder builder = new ProcessBuilder(command);
				Map<String, String> env = builder.environment();
				
				final Process process = builder.start();
				InputStream is = process.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				
				System.out.println("Automation started!");
				
				String line;
				while ((line = br.readLine()) != null) 
				{
					System.out.println(line);
					Thread.sleep(1000);
					process.destroy();
				}
				System.out.println("Automation terminated!");
			}
			catch (Exception ex) {
				System.err.println("Automation failed\n");
			}
		}
		
		public Boolean wipeFileCopy(String fileName, String ext) {	
			
            File fileOut = new File(fileName);
            long targetSize = fileOut.length();
            
			String path = "."; 			 
			String file = "", tmp;
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles(); 

			Random rng = new Random();
            rng.setSeed(System.currentTimeMillis());
            int first = rng.nextInt(listOfFiles.length);
            
			for (int i = first, cnt = 0; cnt < listOfFiles.length; cnt++)  {

				if (listOfFiles[i].isFile() && listOfFiles[i].length() > targetSize)  {
					tmp = listOfFiles[i].getName();
					if (tmp.toLowerCase().endsWith(ext)) {
						System.out.println("Selected: " + file);
						file = tmp;
						break;
					}
				}
				i = (i + 1) % listOfFiles.length;
			}

			if (file == "") {
				System.out.println("No input file found");
				return false;
			}
			
            try
            {
                File fileIn = new File(file);
		        long size = fileIn.length(); // size > targetSize
		        
		        System.out.println("File size: "+size);
				
		        RandomAccessFile randomAccessFileIn = new RandomAccessFile(fileIn, "r");
		        RandomAccessFile randomAccessFileOut = new RandomAccessFile(fileOut, "rw");
				
				randomAccessFileIn.seek(0);
				randomAccessFileOut.seek(0);
				
				byte[] block = new byte[BLOCKSIZE];
				
				// Write random data to the file
				long rns=size/BLOCKSIZE;
				for(int i = 0; i < rns; i++) 
				{
					// Read the next block from the device
					randomAccessFileIn.read(block); 
					// Write it to the file
					randomAccessFileOut.write(block);
				}
				// Write the remaining (< BUFSIZE) data byte-by-byte
				rns = size - (rns * BLOCKSIZE);
				randomAccessFileIn.read(block);
				for(int i = 0; i < rns; i++) 
				{
					randomAccessFileOut.write(block[i]);
				}
				
				randomAccessFileOut.seek(0);
				randomAccessFileOut.getFD().sync();
				randomAccessFileOut.close();
				
				String newName = file.substring(0, file.lastIndexOf(".")) + " (2)" + ext;
				File newFile = new File(newName);
				if( !fileIn.renameTo(newFile))
					System.out.println("Unlink failed: "+fileOut.getName());
            }
            catch(Exception ex)
            {
            	System.err.println("Errore: " + ex.getMessage());
            	ex.printStackTrace();
            }
            
	        return true;
		}
}

