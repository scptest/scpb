/**
 * Copyright Copyright 2014 Simon Andrews
 *
 *    This file is part of BamQC.
 *
 *    BamQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    BamQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with BamQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package uk.ac.babraham.BamQC.Modules;

import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import net.sf.samtools.SAMRecord;
import uk.ac.babraham.BamQC.Annotation.AnnotationSet;
import uk.ac.babraham.BamQC.Annotation.Chromosome;
import uk.ac.babraham.BamQC.Graphs.HorizontalBarGraph;
import uk.ac.babraham.BamQC.Report.HTMLReportArchive;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public class FeatureCoverage extends AbstractQCModule {

	private String [] featureNames;
	private float [] readCounts;
	
	public void processSequence(SAMRecord read) {}

	public void processFile(SequenceFile file) {}

	public void processAnnotationSet(AnnotationSet annotation) {

		featureNames = annotation.listFeatureTypes();
		
		readCounts = new float[featureNames.length];
		
		for (int i=0;i<featureNames.length;i++) {
			readCounts[i] = annotation.getFeatureClassForType(featureNames[i]).count();
		}
	}

	public JPanel getResultsPanel() {
		return new HorizontalBarGraph(featureNames, readCounts, "Feature read counts");
	}

	public String name() {
		return "Featue type read counts";
	}

	public String description() {
		return "Tells how reads are distributed between feature types";
	}

	public void reset() {
		// TODO Auto-generated method stub

	}

	public boolean raisesError() {
		//TODO: Set this
		return false;
	}

	public boolean raisesWarning() {
		//TODO: Set this
		return false;
	}

	public boolean needsToSeeSequences() {
		return false;
	}

	public boolean needsToSeeAnnotation() {
		return true;
	}

	public boolean ignoreInReport() {
		return false;
	}

	public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
		// TODO Auto-generated method stub

	}

}
