package ch.blogspot.prozakcode.writeables;

import java.io.DataInput;
import java.io.DataOutput;

import java.util.Collection;

/**
 * Simple object that represents something being transported
 */
public class Parcel implements Writeable{


    private int[] dimensions;
    private String manifest;
    private Collection<Writeable> contents;

    public Parcel(int[] dims){

    }


    public void readFields(DataInput input){

    }

    public void write(DataOutput output){

    }

    @Override
    public boolean equals(Object other){
	if( other instanceof Parcel ){
	    return true;
	}
	return false;
    }

}
