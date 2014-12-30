/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.utils.annotate;

import bio.igm.utils.init.Logging;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author osagie
 */
public class AnnotateStructures {

    String path, counts_path;
    Map<String, Integer> structures = new HashMap<String, Integer>();
    Map<String, String> annotated_structures = new HashMap<String, String>();
    Map<String, String> unmerged_structures = new HashMap<String, String>();
    Map<String, Map<Integer, String>> genes = new HashMap<String, Map<Integer, String>>();
    public static Logger LOG;

    public AnnotateStructures(String _path, String _counts_path) throws IOException {
        this.path = _path;
        this.counts_path = _counts_path;
        File f = new File(_path);
        
        try {
            if(f.isDirectory()){
                LOG = new Logging(_path, AnnotateStructures.class.getName()).setup();
            }else{
                LOG = new Logging(f.getParent(), AnnotateStructures.class.getName()).setup();
            }
        } catch (IOException ex) {
            Logger.getLogger(AnnotateStructures.class.getName()).log(Level.SEVERE, null, ex);
        }

        read_structures();
        read_transcript_annotation();

        annotate_structures();

        writeToFile();
    }
    

    private void read_structures() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.counts_path));
        String line = "";

        while ((line = br.readLine()) != null) {
            if(structures.containsKey(line.split("\t")[0])){
                int count = structures.get(line.split("\t")[0]) + Integer.parseInt(line.split("\t")[1]);
                structures.put(line.split("\t")[0], count);
            }else{
                structures.put(line.split("\t")[0], Integer.parseInt(line.split("\t")[1]));
            }

        }
    }

    private void read_transcript_annotation() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.path));
        String line = "";

        while ((line = br.readLine()) != null) {
            String refseq = line.split("\t")[3];
           // if (!(refseq.startsWith("NM")) || (refseq.startsWith("NR"))) {
            if (!refseq.startsWith("N")) {
                String _refseq = refseq.split("_")[0].replace(".", ":"); //assumes knowngene id
                String _line = "";
                String[] content = line.split("\t");
                content[3] = _refseq.toUpperCase();
                for(int i = 0; i < content.length; i++){
                    _line += content[i] + "\t";
                }
                refseq = _refseq.toUpperCase();
                line = _line;
            }
            int index = Integer.parseInt(line.split("\t")[4]);

            if (genes.containsKey(refseq)) {
                genes.get(refseq).put(index, line);
            } else {
                Map<Integer, String> temp = new HashMap<Integer, String>();
                temp.put(index, line);
                genes.put(refseq, temp);
            }
        }
    }

    private void annotate_structures() {
        Map<String, String> temp_ids = new HashMap<String, String>();
        Map<String, String> temp_counts = new HashMap<String, String>();
        for (String s : structures.keySet()) {
            try{
            String refseq = s.split("\\.")[0];
            int count = structures.get(s);
            int prime5 = Integer.parseInt(s.split("\\.")[1]) - 1;
            int prime3 = Integer.parseInt(s.split("\\.")[2]) - 1;

            if (genes.containsKey(refseq)) {
                Map<Integer, String> temp = genes.get(refseq);
                List<Integer> order = new ArrayList<Integer>(temp.keySet());
                Collections.sort(order);
                Integer[] index = new Integer[temp.size()];
                index = order.toArray(index);
                String exon5 = temp.get(index[prime5]);
                String exon3 = temp.get(index[prime3]);
                int start = 0;
                int end = 0;
                

                boolean negative_strand = false;

                String[] contents = exon5.split("\t");
                //String bedformat = contents[0] + "\t";
                String id = contents[0] + ':';

                if (contents[5].equalsIgnoreCase("-")) {
                    negative_strand = true;
                    start = Integer.parseInt(contents[1]);
                } else {
                    start = Integer.parseInt(contents[2]);
                }

                String bedformat = start + "";
                String appended = "";
                id += start + "-";
               // temp += contents[0]+":"+contents[1]+"-"+contents[2] + "\t" + contents[4] + "\t";

                contents = exon3.split("\t");
                // temp += contents[1] + "\t" + contents[4] + "\t";
                if (negative_strand) {
                    end = Integer.parseInt(contents[2]);
                } else {
                    end = Integer.parseInt(contents[1]);
                }
                if (start < end) {
                    bedformat = contents[0] + "\t" + bedformat + "\t" + end + "\t";
                } else {
                    bedformat = contents[0] + "\t" + end + "\t" + bedformat + "\t";
                }
                id += end;
                
                
                if(annotated_structures.containsKey(id)){
                    int countsum = Integer.parseInt(annotated_structures.get(id).split("\t")[4]) + count;
                    bedformat += s + "\t" + countsum + "\t" + contents[5];
                    appended += s + "\t" + id + "\t" + countsum;
                    annotated_structures.put(id, bedformat);
                    
                }else{
                    bedformat += s + "\t" + count + "\t" + contents[5];
                    appended += s + "\t" + id + "\t" + count;
                    annotated_structures.put(id, bedformat);
                }
                temp_ids.put(s, id);
                temp_counts.put(id, appended);
                //unmerged_structures.put(s,appended);
            }
            }catch(Exception e){
                System.err.println("Error annotating " + s);
            }
        }
        
        for(String s: temp_ids.keySet()){
            String id = temp_ids.get(s);
            if(temp_counts.containsKey(id.trim())){
                String temp = s + "\t" + id + "\t" + temp_counts.get(id.trim()).split("\t")[2];
                unmerged_structures.put(s, temp);
            }
        }
        
    }

    private void writeToFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.counts_path + ".bed"));
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(this.counts_path + ".unmerged"));
        for(String s: annotated_structures.values()){
            bw.write(s + "\n");
        }
        
        //write structures separately without collapsing isoforms to coordinates
        //note: counts may be different but accurate for respective junction coordinates
        for(String s: unmerged_structures.values()){
            bw1.write(s + "\n");
        }
        bw1.close();
        bw.close();
    }
    
    public static void main(String[] args){
        String exons_bed = args[0];
        String junction_counts = args[1];
        try {
            new AnnotateStructures(exons_bed, junction_counts);
        } catch (IOException ex) {
            Logger.getLogger(AnnotateStructures.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
