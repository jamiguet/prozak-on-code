package ch.blogspot.prozakcode.writeables;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.EOFException;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;


/**
 * Simple object that represents something being transported
 * It is a parcel hence a form of container.
 */
public class Parcel<K extends Writeable> implements Writeable{


    // an array indicating the dimensions od the parcel
    private int[] dimensions;

    Class<K> containee;

    // an collection of contents which only have to be writable to be stored
    private Collection<K> contents;

    public Parcel(Class<K> class,int[] dims){
	dimensions = Arrays.copyOf(dims,dims.length);
	contents = new LinkedList<K>();
	containee = class;
    }


    public void add(K thing){
	synchronized(contents){
	    contents.add(thing);
	    contents.notifyAll();
	}
	
    }

    public Collection<K> contents(){
	return contents;
    }


    public void readFields(DataInput input){
	synchronized(contents){
	    try {
		String[] sDims = input.readUTF().split(",");
		dimensions = new int[sDims.length];
		int pos =0;
		for(String cStr:sDims){
		    dimensions[pos++] = Integer.parseInt(cStr);
		}

		contents = new LinkedList<K>();
		Class<K> factory = Class.forName();
		while(true){
		    K cElem = containee.newInstance();
		    cElem.readFields(input);
		    contents.add(cElem);
		}
		
	    }catch(EOFException eofe){
	    }

	    contents.notifyAll();
	}
    }

    public void write(DataOutput output){
	synchronized(contents){
	    
	    output.writeUTF(Arrays.toString(dimensions).replace("[","").replace("]",""));
	    for(Writeable cw: contents){
		cw.write(output);
	    }
	    contents.notifyAll();
	}

    }

    @Override
    public boolean equals(Object other){
	boolean answer = false;
	synchronized(contents){
	    if( other instanceof Parcel ){
		Parcel o = (Parcel) other;
		answer = Arrays.equals(o.dimensions,this.dimensions) && o.contents.equals(this.contents);
	    }
	    contents.notifyAll();
	}
	return answer;
    }

}
