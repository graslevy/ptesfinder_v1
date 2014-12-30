/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.entities;

import java.io.Serializable;

/**
 *
 * @author osagie izuogu - 05/2013
 */


//@Table(name="CanJunctions")
//@Embeddable
public class CanJunctions implements Serializable {
   
    private String junction;
    private String refseq;
    private int count;
    private int prime5, prime3;

    public CanJunctions() {
    }
    
    public CanJunctions(String string){
        this.junction = string.split("\t")[0];
        this.refseq = junction.split("\\.")[0];
        this.prime3 = Integer.parseInt(junction.split("\\.")[2]);
        this.prime5 = Integer.parseInt(junction.split("\\.")[1]);
        this.count = Integer.parseInt(string.split("\t")[1]);
        
    }
   // @Column(name="Junction")
    public String getJunction() {
        return junction;
    }

    public void setJunction(String junction) {
        this.junction = junction;
    }
   // @Column(name="Locus")
    public String getRefseq() {
        return refseq;
    }

    public void setRefseq(String refseq) {
        this.refseq = refseq;
    }
   // @Column(name="Count")
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    //@Column(name="Exon5")
    public int getPrime5() {
        return prime5;
    }

    public void setPrime5(int prime5) {
        this.prime5 = prime5;
    }
   // @Column(name="Exon3")
    public int getPrime3() {
        return prime3;
    }

    public void setPrime3(int prime3) {
        this.prime3 = prime3;
    }
    
    
    
}
