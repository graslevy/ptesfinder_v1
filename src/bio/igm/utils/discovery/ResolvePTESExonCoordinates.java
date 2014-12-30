/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.utils.discovery;

import bio.igm.entities.Exons;
import bio.igm.entities.MappedAnchors;
import bio.igm.entities.Transcript;
import bio.igm.utils.init.Logging;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author osagie izuogu - 05/2013
 */
public class ResolvePTESExonCoordinates {

    Map<String, Transcript> refseq = new HashMap();
    Map<String, String> to_print = new HashMap<String, String>();
    String eid;
    String path;
    private static Logger LOG;

    public ResolvePTESExonCoordinates(String _path, String _eid) throws IOException {
        this.path = _path;
        File f = new File(_path);

        try {
            if (f.isDirectory()) {
                LOG = new Logging(_path, ResolvePTESExonCoordinates.class.getName()).setup();
            } else {
                LOG = new Logging(f.getParent(), ResolvePTESExonCoordinates.class.getName()).setup();
            }
        } catch (IOException ex) {
            Logger.getLogger(ResolvePTESExonCoordinates.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.eid = _eid;

        getRefSeqCoord();

        read_processed_sam();

        LOG.info("Finished resolving shuffled coordinates to exons..");
        writeToFile();
    }

    private void getRefSeqCoord() throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(eid));
        String line = "";

        while ((line = br.readLine()) != null) {
            String[] contents = line.trim().split("\t");

            Transcript transcript = new Transcript(contents[0].toUpperCase());


            if (contents.length > 3) {
                String[] start = StringUtils.split(contents[2]);
                String[] end = StringUtils.split(contents[3]);

                for (int i = 0; i < start.length; i++) {
                    Exons exon = new Exons(transcript, Integer.parseInt(start[i]), Integer.parseInt(end[i]));
                    exon.setOrder(i + 1);

                    transcript.addExon(exon);
                }

                this.refseq.put(transcript.getRefseq(), transcript);
            }
            contents = null;
        }
        LOG.info("Transcripts: " + refseq.size());

        br.close();

    }

    private void read_processed_sam() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path + "processedSAM.txt"));
        String line = "";

        while ((line = br.readLine()) != null) {

            try {
                String locus = line.split("\t")[1].contains("ref") ? processID(line.split("\t")[1]) : line.split("\t")[1];

                int left = Integer.parseInt(line.split("\t")[2]);
                int right = Integer.parseInt(line.split("\t")[3]);
                int o = Integer.parseInt(line.split("\t")[4]);

                checkAnchorEquality(left, right, locus);
            } catch (Exception e) {
                LOG.info("Error processing this read: " + line);
            }

        }

        br.close();

    }

    private static String processID(String string) {
        String id = "";
        String[] target = string.split("ref");
        id = target[1].split("\\.")[0].replaceAll("\\..*|[^a-zA-Z0-9_]", "");
        
        return id;
    }

    private void checkAnchorEquality(int left, int right, String locus) {

        Transcript transcript = null;
        MappedAnchors anchors = null;

        if (this.refseq.containsKey(locus)) {

            transcript = (Transcript) this.refseq.get(locus);
            int x = Math.max(left, right);
            int y = Math.min(left, right);
            anchors = new MappedAnchors(x, y, transcript);

            if (anchors.checkJunction()) {

                this.to_print.put(anchors.getId(), anchors.getTo_print());

            }


        }

    }

    private void writeToFile() throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.path + "TestTargets.txt"));
        for (String s : to_print.values()) {
            bw.write(s + "\n");
        }
        bw.close();
    }

    public Map<String, Transcript> getRefseq() {
        return refseq;
    }

    public void setRefseq(Map<String, Transcript> refseq) {
        this.refseq = refseq;
    }

    public String getEid() {
        return eid;
    }

    public void setEid(String eid) {
        this.eid = eid;
    }

    public static void main(String[] args) {
        try {
            /*
             input files: Working directory and Coordinates file
             */

            String path = args[0];
            String eid = args[1];

            ResolvePTESExonCoordinates r = new ResolvePTESExonCoordinates(path, eid);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
