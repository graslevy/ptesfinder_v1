/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.entities;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Osagie
 */
public class PTES {

    Map<String, Reads> reads = new HashMap();
    String id;
    String locus;
    int prime5;
    int prime3;
    int count;
    int adj5;
    int adj3;
    int totalCanonicalCount;
    String line;
    boolean confirmed;
    boolean spanned;
    boolean lastExon;
    
    public PTES(){
        
    }
    public PTES(String id, int count) {
        setId(id);
        setLocus(id.split("\\.")[0]);
        setPrime5(Integer.parseInt(id.split("\\.")[1]));
        setPrime3(Integer.parseInt(id.split("\\.")[2]));
    }

    /**
     * Expects input from combined.sam
     * @param line
     */
    public PTES(String line) {
        this.line = line;
        setAttributes(line);
    }
    /*
     * Example data:
     * NM_005245	3	2	1	4	0	1	9
     */
    private void setAttributes(String line) {
        String[] attributes = line.split("\t");
        setId(attributes[0] + "." + attributes[1] + "." + attributes[2]);
        setLocus(attributes[0]);
        setPrime5(Integer.parseInt(attributes[1]));
        setPrime3(Integer.parseInt(attributes[2]));
        setAdj5(Integer.parseInt(attributes[4]));
        setAdj3(Integer.parseInt(attributes[6]));
        setTotalCanonicalCount(Integer.parseInt(attributes[5]) + Integer.parseInt(attributes[7]));
    }

    /**
     * Add supporting reads to PTES
     * @param read
     */
    public void addRead(Reads read) {
        if (read.getTarget().equalsIgnoreCase(this.id)) {
            this.reads.put(read.getId(), read);
            this.count += 1;
        }
    }

    /**
     *
     * @return 3' adjacent exon
     */
    public int getAdj3() {
        return this.adj3;
    }

    /**
     *
     * @param adj3
     */
    public void setAdj3(int adj3) {
        this.adj3 = adj3;
    }

    /**
     *
     * @return 5' adjacent exon
     */
    public int getAdj5() {
        return this.adj5;
    }

    /**
     *
     * @param adj5
     */
    public void setAdj5(int adj5) {
        this.adj5 = adj5;
    }

    /**
     *
     * @return count
     */
    public int getCount() {
        return this.count;
    }

    /**
     *
     * @param count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     */
    public String getLine() {
        return this.line;
    }

    /**
     *
     * @param line
     */
    public void setLine(String line) {
        this.line = line;
    }

    /**
     *
     * @return
     */
    public String getLocus() {
        return this.locus;
    }

    /**
     *
     * @param locus
     */
    public void setLocus(String locus) {
        this.locus = locus;
    }

    /**
     *
     * @return
     */
    public int getPrime3() {
        return this.prime3;
    }

    /**
     *
     * @param prime3
     */
    public void setPrime3(int prime3) {
        this.prime3 = prime3;
    }

    /**
     *
     * @return
     */
    public int getPrime5() {
        return this.prime5;
    }

    /**
     *
     * @param prime5
     */
    public void setPrime5(int prime5) {
        this.prime5 = prime5;
    }

    /**
     *
     * @return
     */
    public Map<String, Reads> getReads() {
        return this.reads;
    }

    /**
     *
     * @param reads
     */
    public void setReads(Map<String, Reads> reads) {
        this.reads = reads;
    }

    /**
     *
     * @return
     */
    public int getTotalCanonicalCount() {
        return this.totalCanonicalCount;
    }

    /**
     *
     * @param totalCanonicalCount
     */
    public void setTotalCanonicalCount(int totalCanonicalCount) {
        this.totalCanonicalCount = totalCanonicalCount;
    }

    /**
     *  Returns true if PTES structure passes threshold 
     * specified for NM
     * @return confirmed 
     */
    public boolean isConfirmed() {
        return this.confirmed;
    }

    /**
     *
     * @param confirmed
     */
    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    /**
     * Returns true if PTES structure is supported
     * by reads spanning the Junction
     * @return spanned
     */
    public boolean isSpanned() {
        return this.spanned;
    }

    /**
     *
     * @param spanned
     */
    public void setSpanned(boolean spanned) {
        this.spanned = spanned;
    }

    /**
     *
     * @return lastExon
     */
    public boolean isLastExon() {
        return this.lastExon;
    }

    /**
     *
     * @param lastExon
     */
    public void setLastExon(boolean lastExon) {
        this.lastExon = lastExon;
    }
}
