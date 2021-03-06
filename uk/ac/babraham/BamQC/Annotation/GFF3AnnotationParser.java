/**
 * Copyright 2010-14 Simon Andrews
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * The Class GFFAnnotationParser reads sequence features from GFFv3 files
 */


public class GFF3AnnotationParser {

	public void parseAnnotation(AnnotationSet annotationSet, File file) throws Exception {
				
		Hashtable<String, FeatureGroup> groupedFeatures = new Hashtable<String, FeatureGroup>();
		
		BufferedReader br  = new BufferedReader(new FileReader(file));
		String line;

		int count = 0;
		while ((line = br.readLine())!= null) {
			
//			if (cancel) {
//				progressCancelled();
//				br.close();
//				return null;
//			}
//			
//			if (count % 1000 == 0) {
//				progressUpdated("Read "+count+" lines from "+file.getName(), 0, 1);
//			}
//			
//			if (count>1000000 && count%1000000 == 0) {
//				progressUpdated("Caching...",0,1);
//				annotationSet.finalise();
//				annotationSet = new AnnotationSet(genome, file.getName()+"["+annotationSets.size()+"]");
//				annotationSets.add(annotationSet);
//			}

			
			++count;
			
			
			if (line.trim().length() == 0) continue;  //Ignore blank lines
			if (line.startsWith("#")) continue; //Skip comments
						
			String [] sections = line.split("\t");
			
			/*
			 * The GFFv3 file fileds are:
			 *    1. name (which must be the chromosome here)
			 *    2. source (which we ignore)
			 *    3. feature type
			 *    4. start pos
			 *    5. end pos
			 *    6. score (which we ignore)
			 *    7. strand
			 *    8. frame (which we ignore)
			 *    9. attributes (structured field allowing us to group features together)
			 *    
			 */
			
			// Check to see if we've got enough data to work with
//			if (sections.length < 7) {
//				progressWarningReceived(new BamQCException("Not enough data from line '"+line+"'"));
//				continue;
//			}

			int strand;
			int start;
			int end;
			
			try {
				
				start = Integer.parseInt(sections[3]);
				end = Integer.parseInt(sections[4]);
				
				// End must always be later than start
				if (end < start) {
					int temp = start;
					start = end;
					end = temp;
				}
				
				if (sections.length >= 7) {
					if (sections[6].equals("+")) {
						strand = Location.FORWARD;
					}
					else if (sections[6].equals("-")) {
						strand = Location.REVERSE;
					}
					else {
						strand = Location.UNKNOWN;
					}
				}
				else {
					strand = Location.UNKNOWN;
				}
			}
			catch (NumberFormatException e) {
//				progressWarningReceived(new BamQCException("Location "+sections[3]+"-"+sections[4]+" was not an integer"));
				continue;
			}

			Chromosome c = annotationSet.chromosomeFactory().getChromosome(sections[0]);
									
			if (sections.length > 8 && sections[8].trim().length() > 0) {
				
				String [] attributes = sections[8].split(" *; *"); // Should check for escaped colons

				// Make up a data structure of the attributes we have
				Hashtable<String,Vector<String>> keyValuePairs = new Hashtable<String, Vector<String>>();
				
				for (int a=0;a<attributes.length;a++) {
					String [] keyValue = attributes[a].split("=",2); // Should check for escaped equals

					// See if we didn't get split
					if (keyValue.length == 1) {
						// This could be a GTF file which uses quoted values in space delimited fields
						keyValue = attributes[a].split(" \"");
						if (keyValue.length == 2) {
							// We need to remove the quote from the end of the value
							keyValue[1] = keyValue[1].substring(0, keyValue[1].length()-1);
//							System.out.println("Key='"+keyValue[0]+"' value='"+keyValue[1]+"'");
						}
					}
					
					if (keyValue.length == 2) {
						if (keyValuePairs.containsKey(keyValue[0])) {
							keyValuePairs.get(keyValue[0]).add(keyValue[1]);
						}
						else {
							Vector<String> newVector = new Vector<String>();
							newVector.add(keyValue[1]);
							keyValuePairs.put(keyValue[0], newVector);
						}
						
					}
					
					else {
//						progressWarningReceived(new BamQCException("No key value delimiter in "+attributes[a]));
					}
					
				}
				
				// We now need to figure out what we're going to do with this feature.
				
				// If it's a GFFv3 file and this feature is a subfeature of another
				// type of feature then we need to simply add this as a sublocation
				// to the existing feature.  We only allow this for exon and CDS features
				// since mRNA has gene as a parent and we don't want to boot that
				
				if (keyValuePairs.containsKey("Parent")  && ! sections[2].equals("mRNA")) {
					
					// Features of a type get combined under their parent
					
					// We change exons to mRNA so we don't end up with spliced exon objects
					if (sections[2].equals("exon")) sections[2] = "mRNA";
					
					String [] parents = keyValuePairs.get("Parent").elementAt(0).split(",");
					
					for (int p=0;p<parents.length;p++) {

//						System.out.println("Adding feature "+sections[2]+" to GFFv3 parent "+parents[p]);

						if (!groupedFeatures.containsKey(sections[2]+"_"+parents[p])) {
							// Make a new feature to which we can add this
							Feature feature = new Feature(sections[2],c);
							groupedFeatures.put(sections[2]+"_"+parents[p], new FeatureGroup(feature, strand, feature.location()));
						}	
						groupedFeatures.get(sections[2]+"_"+parents[p]).addSublocation(new Location(start, end, strand));
						
					}
				}
				
				
				// This could be a GTF file.  If so then we add the subfeature to the appropriate
				// parent feature
				else if (keyValuePairs.containsKey("transcript_id")) {

					if (sections[2].equals("exon")) sections[2] = "mRNA";
					
//					System.out.println("Adding feature "+sections[2]+" to GTF parent "+keyValuePairs.get("trancript_id").elementAt(0));

					if (! groupedFeatures.containsKey(sections[2]+"_"+keyValuePairs.get("transcript_id").elementAt(0))) {
						Feature feature = new Feature(sections[2],c);
												
						groupedFeatures.put(sections[2]+"_"+keyValuePairs.get("transcript_id").elementAt(0), new FeatureGroup(feature, strand, feature.location()));
					}						
						
					groupedFeatures.get(sections[2]+"_"+keyValuePairs.get("transcript_id").elementAt(0)).addSublocation(new Location(start, end, strand));
				}

				else {
					// If we get here we're making a feature with attributes
				
					Feature feature = new Feature(sections[2],c);
					feature.setLocation(new Location(start,end,strand));
					Enumeration<String> en = keyValuePairs.keys();
					if (keyValuePairs.containsKey("ID")) {
						// This is a feature which may end up having subfeatures
						groupedFeatures.put(sections[2]+"_"+keyValuePairs.get("ID").elementAt(0), new FeatureGroup(feature, strand, feature.location()));				
					}
					else {
						// We can just add this to the annotation collection
						annotationSet.addFeature(feature);
					}
				}
				
			}
			else {
				// No group parameter to worry about
				Feature feature = new Feature(sections[2],c);
				feature.setLocation(new Location(start,end,strand));
				annotationSet.addFeature(feature);
			}
			
						
		}
		br.close();
		
		// Now go through the grouped features adding them to the annotation set
		
		Iterator<FeatureGroup> i = groupedFeatures.values().iterator();
		while (i.hasNext()) {
			Feature f = i.next().feature();
			annotationSet.addFeature(f);
		}
				
	}

	/**
	 * The Class featureGroup.
	 */
	private class FeatureGroup {
		
		/** The feature. */
		private Feature feature;
		
		/** The sub locations. */
		private Vector<Location> subLocations = new Vector<Location>();
		
		/** The location */
		private Location location;
				
		/**
		 * Instantiates a new feature group.
		 * 
		 * @param feature the feature
		 * @param strand the strand
		 * @param location the location
		 */
		public FeatureGroup (Feature feature, int strand, Location location) {
			this.feature = feature;
			this.location = location;
		}
		
		/**
		 * Adds a sublocation.
		 * 
		 * @param location the location
		 */
		public void addSublocation (Location location) {
			subLocations.add(location);
		}
		
		/**
		 * Feature.
		 * 
		 * @return the feature
		 */
		public Feature feature () {
			if (subLocations.size() == 0) {
				feature.setLocation(location);					
			}
			else if (subLocations.size() == 1) {
				feature.setLocation(subLocations.elementAt(0));					
			}
			else {
				feature.setLocation(new SplitLocation(subLocations.toArray(new Location[0])));
			}
			
			return feature;
		}
		
		
	}
	
}
