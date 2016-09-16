package edu.depauw.demo_corenlp_neo4j;

import java.util.List;

import edu.stanford.nlp.simple.Sentence;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	Sentence sent = new Sentence("Lucy is in the sky with diamonds.");
    	List<String> nerTags = sent.nerTags();
    	String firstPOSTag = sent.posTag(0);
    	
    	System.out.println(nerTags);
    	System.out.println(firstPOSTag);
    }
}
