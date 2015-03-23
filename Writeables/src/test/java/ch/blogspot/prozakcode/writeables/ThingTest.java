package ch.blogspot.prozakcode.writeables;

import org.junit.Test;
import org.junit.Ignore;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


import java.util.Arrays;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.EOFException;

import java.util.UUID;

public class ThingTest{


    @Test
    public void shouldConstructCorrectly(){

	int[] expected = {3,3};
	String expectedStr = "Something in "+Arrays.toString(expected);
	Thing test = new Thing("Something",expected);
	
	assertArrayEquals(expected,test.dimensions());
	assertEquals(expectedStr,test.toString());
    }

    @Test
    public void shouldWriteAndReadCorrectly() {
	
	int[] expected = {3,3};
	String expectedStr = "Something in "+Arrays.toString(expected);
	Thing expectedThing = new Thing("Something",expected);

	
	UUID fName = UUID.randomUUID();
	try( FileOutputStream fos = new FileOutputStream("/tmp/"+fName.toString());
	     DataOutputStream dos = new DataOutputStream(fos)){
		expectedThing.write(dos);
	    }catch(IOException ioe){
	    ioe.printStackTrace();
	}
	Thing test = new Thing();
	try( FileInputStream fis = new FileInputStream("/tmp/"+fName.toString());
	     DataInputStream dis = new DataInputStream(fis)){
		test.readFields(dis);
	    }
	catch(EOFException eofe) { }
	catch(IOException ioe){
	    ioe.printStackTrace();
	}


	assertEquals(expectedThing,test);
	
    }

}
