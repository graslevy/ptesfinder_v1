/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.entities;

/**
 *
 * @author osagie izuogu - 05/2013
 */
public class ExonConstructs {

    private String id;
    private int junction;
    private String sequence;
    private int[] sizes;

    public ExonConstructs(String id, String exon1, String exon2) {
        this.id = id;
        int x = 0;
        int y = 0;
        sizes = new int[2];
      
        x = exon1.length();
        y = exon2.length();
        sequence = exon1.replace("\r", "") + exon2.replace("\r", "");
        //junction = sequence.length() - x;
        junction = x-1;
        sizes[0] = x;
        sizes[1] = y;
    }

    public ExonConstructs(String s, String exon1, String exon2, int segment_size) { //used to generate new references for junction models
        
        int x = 0;
        int y = 0;
        sizes = new int[2];
       
        x = exon1.length();
        int x1 = x > segment_size ? segment_size : x; //use exon length if segment size > than exon length
        y = exon2.length();
        int y1 = y > segment_size ? segment_size : y;
        String seq1 = exon1.replace("\r", "").substring(x - x1);
        String seq2 = exon2.replace("\r", "").substring(0, y1);
        sequence = seq1 + seq2;
        this.id = s + "." + x1;
       
        junction = x1;
        sizes[0] = x;
        sizes[1] = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getJunction() {
        return junction;
    }

    public void setJunction(int junction) {
        this.junction = junction;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public int[] getSizes() {
        return sizes;
    }

    public void setSizes(int[] sizes) {
        this.sizes = sizes;
    }
}
