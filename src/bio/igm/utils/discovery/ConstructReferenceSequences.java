/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bio.igm.utils.discovery;

import bio.igm.entities.ExonConstructs;
import bio.igm.utils.init.Logging;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author osagie izuogu - 05/2013 Constructs new references for the the models described by
 * ResolvePTESExonCoordinates
 *
 * Requires working directory path, path to exons.fa and size of constructs
 * required.
 */
public class ConstructReferenceSequences {

    String path = "";
    String mrnaPath;
    Map<String, String> putative_models = new HashMap<String, String>();
    Map<String, String> transcripts = new HashMap<String, String>();
    Map<String, ExonConstructs> constructs = new HashMap<String, ExonConstructs>();
    Map<String, ExonConstructs> can_constructs = new HashMap<String, ExonConstructs>();
    List<String> canonical = new ArrayList<String>();
    List<String> ptes = new ArrayList<String>();
    int segment_size = 0;
    private static Logger LOG;

    public ConstructReferenceSequences(String _path, String mrnafa, int segmentSize) throws IOException {
        this.path = _path;
        File f = new File(_path);
        
        try {
            if(f.isDirectory()){
                LOG = new Logging(_path, ConstructReferenceSequences.class.getName()).setup();
            }else{
                LOG = new Logging(f.getParent(), ConstructReferenceSequences.class.getName()).setup();
            }
        } catch (IOException ex) {
            Logger.getLogger(ConstructReferenceSequences.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.mrnaPath = mrnafa;
        this.segment_size = segmentSize;

        read_transcriptome_fasta();
        LOG.info("Finished reading Refseq Exons .. ");

        read_structures();
        constructs = generate_constructs(ptes);
        LOG.info("Finished generating PTES sequence constructs ");

        can_constructs = generate_constructs(canonical);
        LOG.info("Finished generating Canonical sequence constructs");

        writeConstructsToFile(constructs, "ptes");
        writeConstructsToFile(can_constructs, "canonical");

    }

    public void read_transcriptome_fasta() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.mrnaPath));
        String line = "";
        StringBuilder builder = new StringBuilder();
        while ((line = br.readLine()) != null) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));

        }
        br.close();
        String[] seqs = builder.toString().split(">");
        String seq = "";

        for (String s : seqs) {
            if (StringUtils.isNotBlank(s)) {
                String[] oneSeq = s.trim().split("\n");
                String id = oneSeq[0].trim().split(" ").length < 2 ? oneSeq[0] : processId(oneSeq[0]);

                seq = StringUtils.join(oneSeq, "", 1, oneSeq.length);
                transcripts.put(id, seq.toUpperCase());
            }
        }
        LOG.info("Finished loading exons\nSize: " + transcripts.size());
    }

    private String processId(String string) {

        String temp = "";
        String contents[] = string.split(" ")[0].split("_");
        if (contents.length > 4) {
            temp = contents[2] + "_" + contents[3] + "_" + contents[4];
        }

        return temp;
    }

    private Map<String, ExonConstructs> generate_constructs(List<String> structures) {
        Map<String, ExonConstructs> _constructs = new HashMap<String, ExonConstructs>();
        for (String s : structures) {
            String refseq = s.split("\t")[0].split("\\.")[0];
            int exon5start = Integer.parseInt(s.split("\t")[1]);
            int exon5stop = Integer.parseInt(s.split("\t")[2]);
            int exon3start = Integer.parseInt(s.split("\t")[3]);
            int exon3stop = Integer.parseInt(s.split("\t")[4]);

            if (transcripts.containsKey(refseq)) {
                String seq = transcripts.get(refseq);
                String seq1 = seq.substring(exon5start - 1, exon5stop);
                String seq2 = seq.substring(exon3start - 1, exon3stop);

                ExonConstructs construct = new ExonConstructs(s.split("\t")[0], seq1, seq2, segment_size);
                _constructs.put(construct.getId(), construct);
            }

        }
        return _constructs;

    }

    private void read_structures() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(this.path + "TestTargets.txt"));
        String line = "";

        while ((line = br.readLine()) != null) {
            try {
                if (!ptes.contains(line.split("\t")[2])) {
                    ptes.add(line.split("\t")[2] + "\t" + line.split("\t")[3] + "\t" + line.split("\t")[4] + "\t" + line.split("\t")[5] + "\t" + line.split("\t")[6]);
                }
       
                for (int i = 7; i < line.split("\t").length;) {
                    if (!canonical.contains(line.split("\t")[i])) {
                        canonical.add(line.split("\t")[i] + "\t" + line.split("\t")[i + 1] + "\t" + line.split("\t")[i + 2] + "\t" + line.split("\t")[i + 3] + "\t" + line.split("\t")[i + 4]);

                    }
                    i += 5;
                }
            } catch (Exception e) {
                System.out.println("Error! '" + line + "' does not conform..");
            }
        }
        LOG.info("Finished reading putative PTES models ");
    }

    private void writeConstructsToFile(Map<String, ExonConstructs> _constructs, String structure) throws IOException {
        String filename = "";
        if (structure.equalsIgnoreCase("ptes")) {
            filename = "PTESJoints.fasta";
        } else {
            filename = "CannonicalJoints.fasta";
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(this.path + filename));
        for (String s : _constructs.keySet()) {
            bw.write(">" + s + "\n" + _constructs.get(s).getSequence() + "\n");
        }
        bw.close();
    }

    public static void main(String[] args) {
        
        String path = args[0];
        String epath = args[1];
        int segment_len = Integer.parseInt(args[2]);
        try {
            new ConstructReferenceSequences(path, epath, segment_len);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
