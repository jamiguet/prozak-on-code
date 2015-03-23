package ch.blogspot.prozakcode.coalesce;

import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


import java.util.UUID;
import java.util.LinkedList;

import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.io.IOException;
import java.io.EOFException;



public class CoalescingTest{

    // Kindly lifted from
    // http://www.mkyong.com/java/how-to-execute-shell-command-from-java/
    private String executeCommand(String command) {
 
	StringBuffer output = new StringBuffer();
	
	Process p;
	try {
	    p = Runtime.getRuntime().exec(command);
	    p.waitFor();
	    BufferedReader reader = 
		new BufferedReader(new InputStreamReader(p.getInputStream()));
	    
	    String line = "";			
	    while ((line = reader.readLine())!= null) {
		output.append(line + "\n");
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return output.toString();
 
    }


    @Test
    public void singleStringCoalesce(){
	String expected =new String("lkfhdsaöflvjhdsöaj fhdsögjhadsölvh ");

	UUID store = UUID.randomUUID();

	CoalescingTools.coalesce(store,expected,false);	
	
	String[] result = executeCommand("wc -m "+store.toString()+" | awk '{ print $1 }' ").split(" ");
	assertEquals(expected.length(),Integer.parseInt(result[0].trim()));

    }



    @Test
    public void multiStringCoalesce(){
	String expected =new String("lkfhdsaöflvjhdsöaj fhdsögjhadsölvh ");
	int n =3;

	UUID store = UUID.randomUUID();

	for(int i=0; i<n; i++){
	    CoalescingTools.coalesce(store,expected,false);	
	}
	
	String[] result = executeCommand("wc -m "+store.toString()+" | awk '{ print $1 }' ").split(" ");
	assertEquals(n*expected.length(),Integer.parseInt(result[0].trim()));

    }

    @Test
    public void loadAfterStoreSingle(){
	String expected =new String("lkfhdsaöflvjhdsöaj fhdsögjhadsölvh ");

	UUID store = UUID.randomUUID();

	CoalescingTools.coalesce(store,expected,false);	
	
	String[] result = executeCommand("wc -m "+store.toString()+" | awk '{ print $1 }' ").split(" ");
	assertEquals(expected.length(),Integer.parseInt(result[0].trim()));

	String compressed = CoalescingTools.read(store);

	assertNotNull(compressed);
	assertNotEquals(0,compressed.length());

    }

    @Test
    public void storeCompressedSingle(){
	String expected =new String("lkfhdsaöflvjhdsöaj fhdsögjhadsölvh ");

	UUID store = UUID.randomUUID();

	CoalescingTools.coalesce(store,expected,false);	
	
	String[] result = executeCommand("wc -m "+store.toString()+" | awk '{ print $1 }' ").split(" ");
	assertEquals(expected.length(),Integer.parseInt(result[0].trim()));

	String compressed = CoalescingTools.read(store);

	//	System.out.println("compressed: "+compressed);
	assertNotNull(compressed);
	assertNotEquals(0,compressed.length());


	UUID store2 = UUID.randomUUID();

	CoalescingTools.coalesce(store2,compressed,true);

	String[] result2 = executeCommand("wc -m "+store2.toString()+" | awk '{ print $1 }' ").split(" ");	
	assertEquals(expected.length(),Integer.parseInt(result2[0].trim()));

    }

    @Test
    public void storingCompressedAndNot(){

	String expected =new String("lkfhdsaöflvjhdsöaj fhdsögjhadsölvh ");
	UUID store = UUID.randomUUID();

	CoalescingTools.coalesce(store,expected,false);	

	String compressed = CoalescingTools.read(store);

		System.out.println("compressed: "+compressed);
	assertNotNull(compressed);
	assertNotEquals(0,compressed.length());

	UUID store2 = UUID.randomUUID();

	CoalescingTools.coalesce(store2,expected,false);
	CoalescingTools.coalesce(store2,expected,false);
	CoalescingTools.coalesce(store2,compressed,true);
	CoalescingTools.coalesce(store2,compressed,true);


	System.out.println(store2.toString());
	String[] result2 = executeCommand("wc -m "+store2.toString()+" | awk '{ print $1 }' ").split(" ");	
	assertEquals(4*expected.length(),Integer.parseInt(result2[0].trim()));

    }

    @Test
    public void testCoalesceSingleInt() throws IOException{
	UUID intStore = UUID.randomUUID();

	int expected = 3;

	CoalescingTools.coalesce(intStore,expected);

	int result = CoalescingTools.readData(intStore).readInt();

	assertEquals(expected,result);
    }


    @Test 
    public void testCoalesceMuiltiInt() throws IOException{

	UUID intStore = UUID.randomUUID();

	int expected = 3;
	int count=10;

	int read=0;
	int total = 0;
	
	for(int i=0; i<count; i++){
	    CoalescingTools.coalesce(intStore,expected);
	}


	try(
	    DataInputStream dis = CoalescingTools.readData(intStore)
	    ) {

		while(true){
		    total += dis.readInt();
		    read++;
		}
	    }
	catch(EOFException eofe) { }
	catch(IOException ioe){ System.out.println(ioe.toString()); }
	

	assertEquals(count,read);
	assertEquals(expected*count,total);

	

    }

    
    @Test
    public void testCoalesceCollection() throws IOException{

	UUID longStore = UUID.randomUUID();
	LinkedList<Long> lll = new LinkedList<Long>();
	int count =20;
	long expected = 3;
	long total=0;
	int read =0;

	for(int i=0; i<count;i++){
	    lll.add(expected);
	}

	CoalescingTools.coalesce(longStore,lll.toArray(new Long[0]));

	try(
	    DataInputStream dis = CoalescingTools.readData(longStore)
	    ) {

		while(true){
		    total += dis.readLong();
		    read++;
		}
	    }
	catch(EOFException eofe) { }
	catch(IOException ioe){ System.out.println(ioe.toString()); }
	

	assertEquals(count,read);
	assertEquals(expected*count,total);
	
    }

    @Test
    public void testRecoallesceCollection() throws IOException{
	// In this test we take a collection that is compressed and
	// then coallesce it with the next one.

	UUID store = UUID.randomUUID();

	LinkedList<Long> lll = new LinkedList<Long>();

	int count =10;
	long expected = 3;
	long total = 0;
	int read = 0;

	for(int i=0 ; i<count ; i++){
	    lll.add(expected);
	}

	CoalescingTools.coalesce(store,lll.toArray(new Long[0]));

	// compressed has 10*3
	String compressed = CoalescingTools.read(store);

	// we now make it 5*10*3 with a mix of compressed or not

	store = UUID.randomUUID();
	
	CoalescingTools.coalesce(store,lll.toArray(new Long[0]));
	CoalescingTools.coalesce(store,compressed,true);
	CoalescingTools.coalesce(store,lll.toArray(new Long[0]));
	CoalescingTools.coalesce(store,compressed,true);
	CoalescingTools.coalesce(store,compressed,true);

	try(
	    DataInputStream dis = CoalescingTools.readData(store)
	    ){
		while(true){
		    total += dis.readLong();
		    read++;
		}
	    }
	catch(EOFException eofe) { }
	catch(IOException ioe){ System.out.println(ioe.toString()); }
	
	assertEquals(5*10*3,total);
	assertEquals(50,read);
	    	
    }



    @Test
    public void compressedIteratorTest(){

	// First we create some data

	UUID store = UUID.randomUUID(); 
	UUID store2 = UUID.randomUUID(); 	
	

	LinkedList<Long> lll = new LinkedList<Long>();

	int count =1000;
	long expected = 3;
	long total = 0;
	int read = 0;

	for(int i=0 ; i<count ; i++){
	    lll.add(expected);
	}

	CoalescingTools.coalesce(store,lll.toArray(new Long[0]));

	CoalescingTools.CompressedIterator iterator = new CoalescingTools.CompressedIterator(store);

	LinkedList<String> state = new LinkedList<String>();
	for(String cStr:iterator){
	    state.add(cStr);
	}

	// then we re-coallesce 
	for(String cstr:state){
	    CoalescingTools.coalesce(store2,cstr,true);
	}
	
	// to finally consume
	try(
	    DataInputStream dis = CoalescingTools.readData(store2)
	    ){
		while(true){
		    total += dis.readLong();
		    read++;
		}
	    }
	catch(EOFException eofe) { }
	catch(IOException ioe){ System.out.println(ioe.toString()); }
	
	assertEquals(count*expected,total);
	assertEquals(count,read);


    }
    
    

    @Test
    public void testUnCompressedIterator() throws IOException{
	// In this test we take a collection that is compressed and
	// then coallesce it with the next one.

	UUID store = UUID.randomUUID();

	LinkedList<Long> lll = new LinkedList<Long>();

	int count =10;
	long expected = 3;
	long total = 0;
	int read = 0;

	for(int i=0 ; i<count ; i++){
	    lll.add(expected);
	}

	CoalescingTools.coalesce(store,lll.toArray(new Long[0]));

	// compressed has 10*3
	String compressed = CoalescingTools.read(store);

	// we now make it 5*10*3 with a mix of compressed or not

	store = UUID.randomUUID();
	
	CoalescingTools.coalesce(store,lll.toArray(new Long[0]));
	CoalescingTools.coalesce(store,compressed,true);
	CoalescingTools.coalesce(store,lll.toArray(new Long[0]));
	CoalescingTools.coalesce(store,compressed,true);
	CoalescingTools.coalesce(store,compressed,true);

	CoalescingTools.UnCompressedIterator<Long> iter = new CoalescingTools.UnCompressedIterator<Long>(store,Long.class);

	for(Long cLong:iter){
	    total+= cLong;
	    read++;
	}
	
	
	assertEquals(5*count*expected,total);
	assertEquals(5*count,read);
	    	
    }




}
