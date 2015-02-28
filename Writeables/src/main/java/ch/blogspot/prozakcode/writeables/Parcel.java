package ch.blogspot.prozakcode.writeables;

import java.io.DataInput;
import java.io.DataOutput;

import java.util.Collection;

import java.util.Array;

/**
 * Simple object that represents something being transported
 */
public class Parcel implements Writeable{


    // an array indicating the dimensions od the parcel
    private int[] dimensions;

    // an collection of contents which only have to be writable to be stored.
    private Collection<Writeable> contents;

    public Parcel(int[] dims){
	dimensions = Arrays.copyOf(dims,dims.length);
	contents = new LinkedList<Writeables>();
    }


    public void add(Writable thing){
	synchronized(contents){
	    contents.add(thing);
	    contents.notifyAll();
	}
	
    }

    public void readFields(DataInput input){
	synchronised(contents){

	    contents.notifyAll();
	}
    }

    public void write(DataOutput output){
	synchronised(contents){
	    
	    contents.notifyAll();
	}

    }

    @Override
    public boolean equals(Object other){
	boolean answer = false;
	synchronised(contents){
	    if( other instanceof Parcel ){
		Parcel o = (Parcel) other;
		answer = Arrays.equals(o.dimensions,this.dimensions) && o.contents.equals(this.contents);
	    }
	    contents.notifyAll();
	}
	return answer;
    }

}
