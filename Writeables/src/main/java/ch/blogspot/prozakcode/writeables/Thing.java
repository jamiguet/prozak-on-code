package ch.blogspot.prozakcode.writeables;

import java.util.Arrays;
import java.util.LinkedList;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.EOFException;

/**
 * A thing that ocupies space
 */
public class Thing implements Writeable{


    private String description = null;
    private int[] dimensions = null;

    public Thing(){ }

    public Thing (String descr,int...dims){
	description = descr;
	dimensions = Arrays.copyOf(dims,dims.length);
    }

    public int[] dimensions(){
	return Arrays.copyOf(dimensions,dimensions.length);
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
	for(int cInt:dimensions){
	    out.writeInt(cInt);
	}
    }

    public void readFields(DataInputStream in) throws IOException{
	LinkedList<Integer> lli = new LinkedList<Integer>();	
	try{
	    description = in.readUTF();

	    while(in.available()!=0){
		lli.add(in.readInt());
	    }
	} catch(EOFException eofe) {  }
	finally{
	    dimensions = new int[lli.size()];
	    int pos =0;
	    for(int cInt:lli){
		dimensions[pos++] = cInt;
	    }
	}
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
