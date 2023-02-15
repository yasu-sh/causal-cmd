package edu.pitt.dbmi.causal.cmd;

import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 *
 * Feb 12, 2023 5:24:18 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class CausalCmdApplicationExternalGraphTest {

    @TempDir
    public static Path tempDir;

    @Test
    public void testFgesWithContinuousData() throws IOException {
        String dataset = CausalCmdApplicationExternalGraphTest.class
                .getResource("/data/graph_data/sim_cont_10var_1kcase/data/data.txt").getFile();
        String graph = CausalCmdApplicationExternalGraphTest.class
                .getResource("/data/graph_data/sim_cont_10var_1kcase/graph/graph.txt").getFile();

        String dirOut = TestFiles.createSubDir(tempDir, "gfci_cont").toString();
        String[] args = {
            "--dataset", dataset,
            "--delimiter", "tab",
            "--data-type", "continuous",
            "--algorithm", "fges",
            "--score", "sem-bic-score",
            "--external-graph", graph,
            "--default",
            "--out", dirOut
        };
        CausalCmdApplication.main(args);
    }

}
