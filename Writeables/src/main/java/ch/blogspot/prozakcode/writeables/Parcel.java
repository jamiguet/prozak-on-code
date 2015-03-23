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


    // Array indicating the dimensions od the parcel    
    private int[] dimensions;

    // Class instance enabling the crafting of containee instances
    private Class<K> containee;

    // Collection of contents which only have to be writable to be stored
    private Collection<K> contents;

    /**
     * Constructor
     * @param clazz Class<K>  class object for the contents of the parcel
     */
    public Parcel(Class<K>  clazz){
	containee = clazz;
    }


    /**
     * Constructor
     * @param clazz Class<K>  class object for the contents of the parcel
     * @param dims int[] Physical dimensions of the parcel
     */
    public Parcel(Class<K> clazz,int...dims){
	dimensions = Arrays.copyOf(dims,dims.length);
	contents = new LinkedList<K>();
	containee = clazz;
    }

    /**
     * Method adding an element to the parcel
     * @param thing represents the thing being added to the parcel
     */
    public void add(K thing){
	synchronized(this){
	    contents.add(thing);
	    this.notifyAll();
	}
    }

    /**
     * Method checking if the parcel will fit in the specified dimensions
     * @param containing int array indicating the dimensions of the containing object
     */
    public boolean willItFit(int[] containing){
	boolean res = dimensions.length == containing.length;
	if(res){
	    for(int i=0; (i< dimensions.length) && res; i++){
		res = res && (dimensions[i]<=containing[i]);
	    }
	}
	return res;
	
    }

    /**
     * Method returning the content of the pacel
     * @return A Collection of the contained objects.
     */
    public Collection<K> contents(){
	return contents;
    }


    /**
     * Method reading the fields of the object from a DataInput object
     * @param input DataInput object from which the fields are read.
     */
    public void readFields(DataInput input) throws IOException{
	synchronized(this){
	    try {
		String[] sDims = input.readUTF().split(",");
		dimensions = new int[sDims.length];
		int pos =0;
		for(String cStr:sDims){
		    dimensions[pos++] = Integer.parseInt(cStr);
		}

		contents = new LinkedList<K>();
		while(true){
		    K cElem = containee.newInstance();
		    cElem.readFields(input);
		    contents.add(cElem);
		    dimensions[pos++] = Integer.parseInt(cStr.trim());
		}
		
	    }catch(EOFException eofe){ // check here if we are contained and re-fire the exception }
	    catch(InstantiationException ie){ }
	    catch(IllegalAccessException iae) { }

	    finally{ this.notifyAll(); }

	}
    }

    /**
     * Method writing the contents of the object to a DataOutput object
     */
    public void write(DataOutput output) throws IOException{
	synchronized(this){
	    
	    output.writeUTF(Arrays.toString(dimensions).replace("[","").replace("]",""));
	    for(Writeable cw: contents){
		cw.write(output);
	    }
	    this.notifyAll();
	}

    }

    /**
     * Method checking the equality between two parcels
     * @param other Object against which we check for equality
     */
    @Override
    public boolean equals(Object other){
	boolean answer = false;
	synchronized(this){
	    if( other instanceof Parcel ){
		Parcel o = (Parcel) other;
		answer = Arrays.equals(o.dimensions,this.dimensions);
		answer = answer 
		    && Arrays.deepEquals(this.contents.toArray(),o.contents.toArray());

	    }
	    this.notifyAll();
	}
	return answer;
    }

}
