package ch.blogspot.prozakcode.coalesce;

import java.util.UUID;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import java.lang.reflect.InvocationTargetException;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.DataOutputStream;
import java.io.DataOutput;
import java.io.ByteArrayOutputStream;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;


import java.util.zip.InflaterOutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


public class CoalescingTools {


    static public class UnCompressedIterator<T extends Number> implements Iterator<T>, Iterable<T> {

	boolean has = true;
	DataInputStream dis = null;
	FileInputStream fis = null;
	long total = 0;
	Class<T> containee = null;


	public enum SupportedType{ 
	    LONG(Long.class),
	    DOUBLE(Double.class),
	    INTEGER(Integer.class),
	    SHORT(Short.class),
	    BYTE(Byte.class),
	    FLOAT(Float.class);
	    
	    private final Class type;

	    <K extends Number>SupportedType(Class<K> t){
		type = t;
	    } 

	    static public  <K extends Number> SupportedType of(Class<K> cont){
		for(SupportedType curr: SupportedType.values()){
		    if (curr.type == cont){
			return curr;
		    }
		}
		return null;
	    }

	   
	    public <K extends Number> K getValue(String in){
		K res = null;
		try{
		    res = (K)type.getConstructor(String.class).newInstance(in);
		}catch(InstantiationException ie) { ie.printStackTrace(); }
		catch(IllegalAccessException iae) { iae.printStackTrace(); }
		catch(NoSuchMethodException nsme) { nsme.printStackTrace(); }
		catch(InvocationTargetException ite) { ite.printStackTrace(); }
		return res;
	    }
 
	};


	public UnCompressedIterator(UUID store,Class<T> containee){
	    this.containee = containee;
	    try	{
		fis = new FileInputStream(store.toString());
		dis = new DataInputStream(fis);
	    }catch(IOException ioe){
		has = false;
		ioe.printStackTrace();
	    }
	}

	public boolean hasNext(){
	    boolean res = false;
	    synchronized(this){
		try{
		    res = dis.available()!=0;
		} catch(IOException ioe) { res =false; }
		has = res;
		this.notifyAll();
	    }
	    return res;
	}
	


	public T next(){
	    
	    T res = null;
	    synchronized(this){
		try{
		    
		    SupportedType st = SupportedType.of(containee);
		    switch(st){
		    case LONG:{
			res =(T) st.getValue(Long.toString(dis.readLong()));

			break;
		    }	
		    case INTEGER:{
			res =(T) st.getValue(Integer.toString(dis.readInt()));
			break;
		    }
		    case DOUBLE:{
			res =(T) st.getValue(Double.toString(dis.readDouble()));
			break;
		    }
		    case SHORT:{
			res =(T) st.getValue(Short.toString(dis.readShort()));
			break;
		    }

		    case BYTE:{
			res =(T) st.getValue(Byte.toString(dis.readByte()));
			break;
		    }
		    case FLOAT:{
			res =(T) st.getValue(Float.toString(dis.readFloat()));
			break;
		    }
			
						
		    }
		   
		
		}
		catch(EOFException eof) { has =false; }
		catch(IOException ioe){
		    ioe.printStackTrace();
		    has = false;
		}
		this.notifyAll();
	    }
	    if(res==null)
		throw new RuntimeException();
	    return res;
	}

	public void done(){
	    try{
		dis.close();
		fis.close();
	    }catch(IOException ioe){
		ioe.printStackTrace();
	    }
	}

	public void remove(){
	    // THINK OF DOING SOMTHING NASTY HERE
	}
	
	public Iterator<T> iterator(){
	    return this;
	}

    }


    static public class CompressedIterator implements Iterator<String>, Iterable<String>{

	boolean has = true;
	Deflater defl = null;
	DeflaterInputStream dis = null;
	FileInputStream fis = null;
	long total = 0;

	public CompressedIterator(UUID store){
	    defl = new Deflater(Deflater.BEST_COMPRESSION); 
	    try
		{
		    fis = new FileInputStream(store.toString());
		    dis = new DeflaterInputStream(fis,defl);
		}catch(IOException ioe){
		has = false;
		ioe.printStackTrace();
	    }
	}

	public boolean hasNext(){
	    boolean res = false;
	    synchronized(this){
		try{
		    res = has && (dis.available()!=0);
		    this.notifyAll();
		}catch(IOException ioe){
		    ioe.printStackTrace();
		    has = false;
		}
	    }
	    return res;
	}
	
	public String next(){
	    // 2^15
	    final byte[] buff = new byte[32768];
	    String res = null;
	    synchronized(this){
		try{
		    if(dis.available()!=0){
			dis.read(buff,0,buff.length);
			long bytesIn = defl.getBytesWritten();
			int buffLen = (int) (bytesIn-total);
			res = printBase64Binary(Arrays.copyOf(buff,buffLen));
			total = bytesIn;
		    }
		}catch(IOException ioe){
		    ioe.printStackTrace();
		    has = false;
		}
		this.notifyAll();
	    }
	    return res;
	}

	public void done(){
	    try{
		dis.close();
		fis.close();
	    }catch(IOException ioe){
		ioe.printStackTrace();
	    }
	}

	public void remove(){
	    // THINK OF DOING SOMTHING NASTY HERE
	}
	
	public Iterator<String> iterator(){
	    return this;
	}
    }

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
		long total = 0;
		while(dfis.available()!=0){
		    dfis.read(buff,0,buff.length);
		    long bytesIn = defl.getBytesWritten();
		    int buffLen =(int) (bytesIn - total);
		    // System.out.println("bytesIn: "+bytesIn+", total: "+ total );
		    sbout.append(printBase64Binary(Arrays.copyOf(buff,buffLen)));
		    total=bytesIn;
		}

	    }
	catch(IOException ioe) {System.out.println("read: "+ioe.toString());}

	return sbout.toString();
    }


    /**
     *  Crafts a Data Input stream for uncompressed data and hands it out to the caller
     * caller is free to do what it pelases with it but should copy the data
     * as it may clog its runtinme.
     * @param store UUID for the store to read.
     */
    static public DataInputStream readData(UUID store) throws FileNotFoundException, IOException{

	FileInputStream fis = new FileInputStream(store.toString());
	//TODO consider putting a buffer here
	DataInputStream res = new DataInputStream(fis);

	return res;
    }

    static public UUID[] makeStore(int dims){
	UUID[] res = new UUID[dims];

	for(int i=0; i<dims; i++){
	    res[i] = UUID.randomUUID();
	}

	return res;
    }
}
