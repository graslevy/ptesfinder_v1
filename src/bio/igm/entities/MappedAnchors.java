/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.entities;

/**
 *
 * @author osagie izuogu - 05/2013
 */
public class MappedAnchors {

    boolean junctionSpan = false;
    Exons[] exons = new Exons[2];
    Exons[] adjacentExons = new Exons[2];
    String to_print = "";
    Transcript transcript;
    String id = "";

    public MappedAnchors(int leftstart, int rightstart, Transcript t) {
        this.transcript = t;
        try {
            this.exons[0] = resolveExons(leftstart);
            this.exons[1] = resolveExons(rightstart);

           // determine_adjacent_exons();
            determine_canonical_junctions();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    public MappedAnchors(Reads left, Reads right) {
    }

    public MappedAnchors(Reads left, Reads right, Transcript transcript) {
        this.transcript = transcript;
        try {
            this.exons[0] = resolveExons(left.getStart());
            this.exons[1] = resolveExons(right.getStart());

        } catch (Exception e) {
            e.getMessage();
        }
    }

    private Exons resolveExons(int start) {
        Exons temp = null;

        //int x = 0;
        for (Exons e : this.transcript.getExons().values()) {
            if (e.getRange().contains(Integer.valueOf(start))) {

                // x = ((Integer) e.getRange().getMaximum()).intValue();
                temp = e;

                break;
            }
        }

        return temp;
    }

    public boolean checkJunction() {
        if ((this.exons[0] != null) && (this.exons[1] != null)
                && (this.exons[0].getOrder() >= this.exons[1].getOrder())) {

            id = transcript.getRefseq() + "." + this.exons[0].getOrder() + "." + this.exons[1].getOrder();
            this.junctionSpan = true;
            to_print = transcript.getRefseq() + "\t" + transcript.getRefseq() + "\t" + id + "\t" + this.exons[0].getStart() + "\t"
                    + this.exons[0].getStop() + "\t" + this.exons[1].getStart() + "\t" + this.exons[1].getStop() + "\t" + to_print;

        }

        return this.junctionSpan;
    }

    private void determine_adjacent_exons() {
        int prime5exon = getExons()[0].getOrder() > this.transcript.getNumOfExons() - 1 ? getExons()[0].getOrder() : getExons()[0].getOrder() + 1;
        int prime3exon = getExons()[1].getOrder() < 1 ? getExons()[1].getOrder() : getExons()[1].getOrder() - 1;;

        Exons t5 = this.transcript.getExons().get(prime5exon);
        Exons t3 = this.transcript.getExons().get(prime3exon);
        adjacentExons[0] = t5;
        adjacentExons[1] = t3;
        String right_can = transcript.getRefseq() + "." + getExons()[0].getOrder() + "." + t5.getOrder() + "\t" + getExons()[0].getStart()
                + "\t" + getExons()[0].getStop() + "\t" + t5.getStart() + "\t" + t5.getStop();

        String left_can = transcript.getRefseq() + "." + t3.getOrder() + "." + getExons()[1].getOrder() + "\t" + t3.getStart() + "\t" + t3.getStop()
                + "\t" + getExons()[1].getStart() + "\t" + getExons()[1].getStop();

        to_print = left_can + "\t" + right_can;
    }

    private void determine_canonical_junctions() {
        
        String temp = "";
        for(Exons e: this.transcript.getExons().values()){
            int x = e.getOrder();
            int y = x - 1;
            
            
            if((x <= this.transcript.getNumOfExons()) && (y > 0)){
                Exons e1 = this.transcript.getExons().get(y);
                temp += this.transcript.getRefseq() + "." + e1.getOrder() + "." + e.getOrder() + "\t" + e1.getStart() + "\t" + e1.getStop() + "\t" + e.getStart() + "\t" + e.getStop() + "\t";
   
            }
        }

       

        to_print = temp;
    }

    public Exons[] getExons() {
        return exons;
    }

    public void setExons(Exons[] exons) {
        this.exons = exons;
    }

    public Transcript getTranscript() {
        return transcript;
    }

    public void setTranscript(Transcript transcript) {
        this.transcript = transcript;
    }

    public String getId() {
        return id;
    }

    public boolean isJunctionSpan() {
        return junctionSpan;
    }

    public void setJunctionSpan(boolean junctionSpan) {
        this.junctionSpan = junctionSpan;
    }

    public String getTo_print() {
        return to_print;
    }

    public void setTo_print(String to_print) {
        this.to_print = to_print;
    }

    public Exons[] getAdjacentExons() {
        return adjacentExons;
    }

    public void setAdjacentExons(Exons[] adjacentExons) {
        this.adjacentExons = adjacentExons;
    }

}
