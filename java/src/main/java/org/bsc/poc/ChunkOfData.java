package org.bsc.poc;

record ChunkOfData( int index, String value ) {
    public ChunkOfData( int index ) {
        this( index, "Item"+ index );
    }
}
