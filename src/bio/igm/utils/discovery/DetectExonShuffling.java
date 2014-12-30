/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.utils.discovery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author osagie
 */
public class DetectExonShuffling {

    String path, infile;
    boolean shuffled = true;

    public DetectExonShuffling(String _path, String _infile, boolean _shuffled) throws IOException {
        this.path = _path;
        this.infile = _infile;
        this.shuffled = _shuffled;

        process_merged_sam();
    }

    private void process_merged_sam() throws IOException {
        String outfile = "";
        String line;
        if (shuffled) {
            //outfile = path + "/processed_shuffled_anchors.txt";
            outfile = path + "/processedSAM.txt";
        } else {
            outfile = path + "/processed_canonical_anchors.txt";
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
        BufferedReader br = new BufferedReader(new FileReader(infile));

        while ((line = br.readLine()) != null) {
            int left_start = Integer.parseInt(line.split("\t")[3]);
            int right_start = Integer.parseInt(line.split("\t")[8]);
            int left_stop = left_start + determine_shift(line.split("\t")[4]);
            int right_stop = right_start + determine_shift(line.split("\t")[9]);
            
            int orientation = Integer.parseInt(line.split("\t")[1]) > 0 ? 1 : -1;
            
            if (shuffled) {
                switch (orientation){
                    case -1:
                        if(right_start < left_stop){
                            bw.write(line.split("\t")[0] + "\t" + line.split("\t")[2] + "\t" + left_start + 
                                    "\t" + right_start + "\t" + orientation + "\t" + (left_stop - left_start) + 
                                    "\t" + (right_stop - right_start) + "\n");
                        }; break;
                    case 1:
                        if(left_start < right_stop){
                            bw.write(line.split("\t")[0] + "\t" + line.split("\t")[2] + "\t" + left_start + 
                                    "\t" + right_start + "\t" + orientation + "\t" + (left_stop - left_start) + 
                                    "\t" + (right_stop - right_start) + "\n");
                        }; break;
                }
            } else {
                switch (orientation){
                    case -1:
                        if(right_start > left_stop){
                            bw.write(line.split("\t")[0] + "\t" + line.split("\t")[2] + "\t" + left_start + 
                                    "\t" + right_start + "\t" + orientation + "\t" + (left_stop - left_start) + 
                                    "\t" + (right_stop - right_start) + "\n");
                        }; break;
                    case 1:
                        if(left_start > right_stop){
                            bw.write(line.split("\t")[0] + "\t" + line.split("\t")[2] + "\t" + left_start + 
                                    "\t" + right_start + "\t" + orientation + "\t" + (left_stop - left_start) + 
                                    "\t" + (right_stop - right_start) + "\n");
                        }; break;
                }
            }
        }
        br.close();
        bw.close();

    }

    private int determine_shift(String cigar) {
        int shift = 0;
        String[] cigar_numbers = cigar.split("\\D");
        for(String s: cigar_numbers){
            try{
                shift += Integer.parseInt(s);
            }catch(Exception e){
                shift+=0;
            }
        }
        return shift;
        
    }
    
    public static void main(String[] args){
        try {
            new DetectExonShuffling(args[0], args[1], Boolean.parseBoolean(args[2]));
        } catch (IOException ex) {
            Logger.getLogger(DetectExonShuffling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

 
}
