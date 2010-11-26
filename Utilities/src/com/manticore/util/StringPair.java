package com.manticore.util;

public class StringPair implements Comparable<StringPair> {

	 public Integer distance;
	 public String t;

	 public StringPair(String s, String t) {
		  distance = Levenshtein.distance(s, t);
		  this.t = t;
	 }

	 @Override
	 public int compareTo(StringPair o) {
		  return distance.compareTo(o.distance);
	 }
}
