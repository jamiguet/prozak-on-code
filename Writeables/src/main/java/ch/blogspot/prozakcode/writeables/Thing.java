package ch.blogspot.prozakcode.writeables;

import java.util.Arrays;
import java.util.LinkedList;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.EOFException;

/**
 * A thing that ocupies space
 */
public class Thing implements Writeable{


    private String description = null;
    private int[] dimensions = null;
    private boolean contained = false;

    public Thing(){ }

    public Thing (String descr,int...dims){
	description = descr;
	dimensions = Arrays.copyOf(dims,dims.length);
    }

    public int[] dimensions(){
	return Arrays.copyOf(dimensions,dimensions.length);
    }


    public void setContained(boolean cont){
	contained = cont;
    }
    @Override
    public String toString(){
	StringBuffer res = new StringBuffer();
	res.append(description);
	res.append(" in ");
	res.append(Arrays.toString(dimensions));
	return res.toString();
    }

    public void write(DataOutput out) throws IOException{
	out.writeUTF(description);
	out.writeInt(dimensions.length);
	for(int cInt:dimensions){
	    out.writeInt(cInt);
	}
    }

    public void readFields(DataInput in) throws IOException{
	
	try{
	    description = in.readUTF();
	    int dims = in.readInt();
	    dimensions = new int[dims];
	    for(int i =0; i<dimensions.length; i++){
		dimensions[i]=in.readInt();
	    }
	} catch(EOFException eofe) { if (contained) throw eofe; }
	
    }

    @Override
    public boolean equals(Object o){
	boolean res = false;
	
	if(o instanceof Thing){
	    Thing other = (Thing) o;
	    res = other.description.equals(this.description);
	    res = res &&  Arrays.equals(other.dimensions,this.dimensions) ;
	}
	
	return res;
    }
}
