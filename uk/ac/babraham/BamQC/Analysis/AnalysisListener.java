/**
 * Copyright Copyright 2010-14 Simon Andrews
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
package uk.ac.babraham.BamQC.Analysis;

import uk.ac.babraham.BamQC.Modules.QCModule;
import uk.ac.babraham.BamQC.Sequence.SequenceFile;

public interface AnalysisListener {

	public void analysisStarted(SequenceFile file);
	public void analysisUpdated(SequenceFile file, int sequencesProcessed, int percentComplete);
	public void analysisComplete(SequenceFile file, QCModule [] results);
	public void analysisExceptionReceived(SequenceFile file, Exception e);
}
