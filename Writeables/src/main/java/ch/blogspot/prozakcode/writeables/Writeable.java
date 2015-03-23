package ch.blogspot.prozakcode.writeables;

import java.io.DataOutput;
import java.io.DataInput;

import java.io.IOException;


/**
 * Copy of the hadoop Writable interface.
 * That prepares objects for transport and delivers them.
 */
public interface Writeable{

    /**
     * Un-pack the object on arrival inside this object
     * The data inside the DataInut object will replace the one
     * in the object.
     */
    public void readFields(DataInput input) throws IOException;

    /**
     * Prepare the object for transport
     * The transport ready version will be inside the
     * DataOuput object passed as a paremeter
     */

    public void write(DataOutput output) throws IOException;

}
