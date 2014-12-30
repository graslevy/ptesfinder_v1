/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.utils.annotate;

import bio.igm.utils.init.Logging;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.StatUtils;

/**
 *
 * @author osagie
 */
public class MergeAnnotations {

    String path;
    String can_path;
    String ptes_path;
    Map<String, String> ptes = new HashMap<String, String>();
    Map<String, List<String>> can_junctions = new HashMap<String, List<String>>();
    Map<String, String> output = new HashMap<String, String>();
    public static Logger LOG;
    
    public MergeAnnotations(String _path, String _ptes, String _can) throws IOException {
        ptes_path = _ptes;
        can_path = _can;
        path = _path;
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

        LOG.info("Reading input bed files..");
        read_ptes_bed();
        read_canonical_bed();
        LOG.info("Appending canonical junction counts to each discovered PTES structure..");
        append_can_counts();
        writeToFile();
        LOG.info("Finished merging files - see final output file: annotated-ptes.bed ");
    }

    private void read_ptes_bed() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(ptes_path));
        String line = "";
        while ((line = br.readLine()) != null) {
            ptes.put(line.split("\t")[3], line);
        }
        br.close();

    }

    private void read_canonical_bed() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(can_path));
        String line = "";
        while ((line = br.readLine()) != null) {
            String refseq = line.split("\t")[0].split("\\.")[0];
            if (can_junctions.containsKey(refseq)) {
                can_junctions.get(refseq).add(line);
            } else {
                List<String> temp = new ArrayList<>();
                temp.add(line);
                can_junctions.put(refseq, temp);
            }
        }
        br.close();
    }

    private void append_can_counts() {
        for (String s : ptes.keySet()) {
            String structure = ptes.get(s);
            String refseq = s.split("\\.")[0];
            int prime5 = Integer.parseInt(s.split("\\.")[1]);
            int prime3 = Integer.parseInt(s.split("\\.")[2]);
            String flank5 = refseq + "." + prime5 + "." + (prime5 + 1);
            String flank3 = refseq + "." + (prime3 - 1) + "." + prime3;

            double max_can_count = 0.0;
            double mean_can_count = 0.0;
            double sum_can_count = 0.0;
            double mean_flanking_count = 0.0;
            double sum_flanking_count = 0.0;
            

            String expression = "NA";

            if (can_junctions.containsKey(refseq)) {
                double[] counts = new double[can_junctions.get(refseq).size()];
                int[] index = new int[can_junctions.get(refseq).size()];
                Map<Integer, Double> all_counts = new HashMap<Integer, Double>();
                int counter = 0;

                for (String canonical : can_junctions.get(refseq)) {
                    int i = Integer.parseInt(canonical.split("\t")[0].split("\\.")[1]);
                    int x = Integer.parseInt(canonical.split("\t")[0].split("\\.")[2]);

                    int z = x - i > 1 ? (i * 1000) + x - 1 : i;     //for junctions 1.2 or 4.5, index=1, for junctions 2.5 etc, index=2005

                    all_counts.put(z, (double) Integer.parseInt(canonical.split("\t")[2]));
                    if ((canonical.split("\t")[0].equalsIgnoreCase(flank5)) || (canonical.split("\t")[0].equalsIgnoreCase(flank3))) {
                        sum_flanking_count += (double) Integer.parseInt(canonical.split("\t")[2]);
                    }
                    //counts[counter] = (double) Integer.parseInt(canonical.split("\t")[2]);
                    index[counter] = z;

                    counter++;
                }
                mean_flanking_count = sum_flanking_count / 2;

                counter = 0;
                Arrays.sort(index);
                for (int x : index) {
                    counts[counter] = all_counts.get(x);
                    counter++;
                }
                if (counts.length > 0) {
                    expression="";
                    for (int i = 0; i < counts.length; i++) {
                        int $5 = (index[i] / 1000) > 0 ? (index[i] / 1000) : index[i];
                        expression += $5 + "." + ((index[i] % 1000) + 1) + "_" + counts[i] + "|";
                    }
                }else{
                    expression = "NA";
                }
                
                max_can_count = StatUtils.max(counts);
                mean_can_count = StatUtils.mean(counts);
                sum_can_count = StatUtils.sum(counts);

            }
            String out = mean_flanking_count + "\t" + sum_flanking_count + "\t" + max_can_count + "\t" + mean_can_count + "\t" + sum_can_count + "\t" + expression;
            output.put(s, structure + "\t" + out);
        }
    }
    
    private void writeToFile() throws IOException{
        String header = "#chromosome\tstart\tstop\tstructure\traw_count\tstrand\tmean_flanking_can_count\tsum_flanking_can_count"
                + "\tmax_can_count\tmean_can_count\tsum_can_count\tcanonical_expression";
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.path + "annotated-ptes.bed"));
        bw.write(header + "\n");
        for(String s: output.values()){
            bw.write(s + "\n");
        }
        
        bw.close();
        
    }
    
    public static void main(String[] args){
        try {
            new MergeAnnotations(args[0], args[1], args[2]);
        } catch (IOException ex) {
            Logger.getLogger(MergeAnnotations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
