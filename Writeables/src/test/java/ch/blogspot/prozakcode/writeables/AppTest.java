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

import java.util.UUID;



/**
 * Unit test for simple App.
 */
public class AppTest
{


    @Test
    public void shouldCheckFitCorrectly(){
	int[] dims = {1,1,1};
	int[] spot = {2,2,2};
	Parcel tested = new Parcel(Parcel.class,dims);
	assertTrue(tested.willItFit(spot));
    }

    @Test
    public void shouldAddThingsCorrectly(){

	Thing thing = new Thing("Thing1",1,1,1);
	Parcel tested = new Parcel(Thing.class,2,2);

	tested.add(thing);
	assertEquals(thing,tested.contents().iterator().next());
    }

     @Test
    public void shouldWriteAndReadCorrectly(){

	Parcel expectedParcel = new Parcel(Thing.class,3,3,3);
	Thing thing = new Thing("One Thing",1,1);

	 for(int i=0; i<3; i++)
	     expectedParcel.add(thing);


	UUID fName = UUID.randomUUID();
	try( FileOutputStream fos = new FileOutputStream("/tmp/"+fName.toString());
	     DataOutputStream dos = new DataOutputStream(fos)){
		expectedParcel.write(dos);
		dos.close();
	    }catch(IOException ioe){
	    ioe.printStackTrace();
	}

	Parcel test = new Parcel(Thing.class);
	try( FileInputStream fis = new FileInputStream("/tmp/"+fName.toString());
	     DataInputStream dis = new DataInputStream(fis)){
		test.readFields(dis);
	    }catch(IOException ioe){
	    ioe.printStackTrace();
	}

	 assertEquals(expectedParcel,test);

    }

}
