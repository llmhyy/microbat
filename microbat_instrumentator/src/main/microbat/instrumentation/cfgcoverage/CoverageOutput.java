package microbat.instrumentation.cfgcoverage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import microbat.instrumentation.cfgcoverage.graph.CoverageSFlowGraph;
import microbat.instrumentation.cfgcoverage.output.CoverageOutputReader;
import microbat.instrumentation.cfgcoverage.output.CoverageOutputWriter;
import microbat.instrumentation.cfgcoverage.runtime.MethodExecutionData;
import microbat.instrumentation.utils.FileUtils;

public class CoverageOutput {
	private CoverageSFlowGraph coverageGraph;
	private Map<Integer, List<MethodExecutionData>> inputData;
	
	public CoverageOutput() {
		
	}
	
	public CoverageOutput(CoverageSFlowGraph coverageGraph) {
		this.coverageGraph = coverageGraph;
	}
	
	public static CoverageOutput readFromFile(String filePath) {
		FileInputStream stream = null;
		CoverageOutputReader reader = null;
		CoverageOutput output =  new CoverageOutput();
		try {
			stream = new FileInputStream(filePath);
			reader = new CoverageOutputReader(new BufferedInputStream(stream));
			CoverageSFlowGraph coverageGraph = reader.readCfgCoverage();
			output.coverageGraph = coverageGraph;
			output.inputData = reader.readInputData();
			return output;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (stream != null) {
					stream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public void saveToFile(String dumpFile) throws IOException {
		File file = new File(dumpFile);
		FileOutputStream fileStream = new FileOutputStream(file);
		fileStream.getChannel().lock();
		OutputStream bufferedStream = new BufferedOutputStream(fileStream);
		CoverageOutputWriter outputWriter = null;
		try {
			outputWriter = new CoverageOutputWriter(bufferedStream);
			outputWriter.writeCfgCoverage(coverageGraph);
			outputWriter.writeInputData(inputData);
		} finally {
			FileUtils.closeStreams(outputWriter);
		}
	}

	public CoverageSFlowGraph getCoverageGraph() {
		return coverageGraph;
	}

	public void setCoverageGraph(CoverageSFlowGraph coverageGraph) {
		this.coverageGraph = coverageGraph;
	}

	public Map<Integer, List<MethodExecutionData>> getInputData() {
		return inputData;
	}

	public void setInputData(Map<Integer, List<MethodExecutionData>> inputData) {
		this.inputData = inputData;
	}
}
