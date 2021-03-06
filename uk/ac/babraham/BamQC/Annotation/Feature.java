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
package uk.ac.babraham.BamQC.Annotation;

import net.sf.samtools.SAMRecord;

public class Feature implements Comparable<Feature>{

	private String type;
	private Chromosome chr;
	private Location location;
	
	public Feature (String type, Chromosome chr) {
		this.type = type;
		this.chr = chr;
	}
	
	public void setLocation (Location l) {
		location = l;
		if (l.end() > chr.length()) chr.setLength(l.end());
	}
	
	public Location location () {
		return location;
	}
	
	public String type () {
		return type;
	}
	
	public Chromosome chr () {
		return chr;
	}
	
	public void processSequence (SAMRecord r) {
		//TODO: Placeholder in case we want to record something for every feature.
	}

	public int compareTo(Feature o) {
		return location.compareTo(o.location);
	}
	
	
}
