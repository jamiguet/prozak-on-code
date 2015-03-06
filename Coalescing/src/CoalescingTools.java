package ch.blogspot.prozakcode.coalesce;

import java.util.UUID;
import java.util.Arrays;
import java.util.Collection;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.DataOutputStream;
import java.io.DataOutput;
import java.io.ByteArrayOutputStream;

import java.io.DataInputStream;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.FileNotFoundException;


import java.util.zip.InflaterOutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


public class CoalescingTools {


    static public void coalesce(UUID store,Integer... data ){
	try(
	    ByteArrayOutputStream baos =new ByteArrayOutputStream();
	    ){
		DataOutput output = new DataOutputStream(baos);
		for(Integer cInt:data){
		    output.writeInt(cInt);
		}
		baos.flush();
		CoalescingTools.coalesce(store,baos.toString(),false);
	    }catch(IOException ioe) {System.out.println(ioe.toString()); }

    }

    static public void coalesce(UUID store,Long...data){
	try(
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ){
		DataOutput output = new DataOutputStream(baos);

		for(Long cLong:data){
		    output.writeLong(cLong);
		}
		baos.flush();
		CoalescingTools.coalesce(store,baos.toString(),false);
	    }catch(IOException ioe) {System.out.println(ioe.toString()); }

    } 


    /**
     * Method storing the supplied String in the specifiec store
     * @param data String containing the data
     * @param compressed boolean indicating if the string contains compresed BASE64 Encoded data
     * @param store UUID for the associated store
     */
    static public void coalesce(UUID store, String data, boolean compressed){
	
	FileOutputStream fos = null;
	InflaterOutputStream infOut = null;
	OutputStreamWriter output =  null;

	try{
	    fos = new FileOutputStream(store.toString(),true);

	    if(compressed){
		infOut =  new InflaterOutputStream(fos, new Inflater());
		
		byte[] buffer = parseBase64Binary(data);
		infOut.write(buffer,0,buffer.length);
	

		infOut.flush();
		infOut.finish();
	    }else{
		output = new OutputStreamWriter(fos); 
		output.write(data,0,data.length());
		output.flush();
	    }
	    
	    fos.flush();
	}
	catch(IOException ioe){
	    System.out.println("coalesce: "+ioe.toString());
	}finally{
	    try{
		fos.close();
	    }catch(IOException ioe) { }
	}
		    

    }

    /**
     * Returns the compressed contents of the store as a BASE64 encoded string
     * @param store UUID for the store
     * @return String which is the BASE64 encoded version of the compressed data.
     */
    static public String read(UUID store){


	StringBuffer sbout = new StringBuffer();
	Deflater defl = new Deflater(Deflater.BEST_COMPRESSION);
	try(
	    FileInputStream fis =new FileInputStream(store.toString());
	    DeflaterInputStream dfis = new DeflaterInputStream(fis,defl);
	    ){

		byte[] buff= new byte[1024];
		int total = 0;
		while(dfis.available()!=0){
		    dfis.read(buff,0,buff.length);
		    int bytesIn = defl.getTotalOut();
		    sbout.append(printBase64Binary(Arrays.copyOf(buff,bytesIn-total)));
		    total+=bytesIn;
		}

	    }
	catch(IOException ioe) {System.out.println("read: "+ioe.toString());}

	return sbout.toString();
    }


    // crafts a Data Input stream and hands it out to the caller
    // caller is free to do what it pelases with it but should not handle the
    // dat whole as that nay clog things up.
    static public DataInputStream readData(UUID store) throws FileNotFoundException, IOException{

	FileInputStream fis = new FileInputStream(store.toString());
	//TODO consider putting a buffer here
	DataInputStream res = new DataInputStream(fis);

	return res;
    }
}
