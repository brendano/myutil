//import com.google.common.collect.*;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;

import util.Arr;
import util.BasicFileIO;
import util.JsonUtil;
import util.U;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.*;

public class RunStanford {
	
	public static void main(String[] args) {
//		String mode = args[0];
//		if (mode.equals("doc2ssplit")) {
//			// bare sentence splitting
//		}
		processDocTSV(args);
	}
	
	/**
	 * Input: one line per sentence. Last cell is space-separated tokens.
	 */
	public static void processBareSentenceTSV() {
		// group by document
	}

	/**
	 * Input: one line per doc. text payloads are JSON strings.
	 * Output: "passthru" fields are copied (prefix fields). Payload is last field.
	 * 
	 * give indexes 1-indexed and comma-separated (i.e. like unix "cut")
	 */
	public static void processDocTSV(String[] args) {
		int[] passthruInds = Arr.readIntVector(args[0], ",");
		int textInd = Integer.valueOf(args[1]);
		Arr.addInPlace(passthruInds, -1);
		textInd -= 1;
//		Arr.addInPlace(textInds, -1);
		
	    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
//	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    for (String line : BasicFileIO.STDIN_LINES) {
//			String[] outrow = new String[passthruInds.length + 1];
			String[] passthruCells = new String[passthruInds.length];
			String[] inrow  = line.split("\t");
			for (int out_i=0; out_i < passthruInds.length; out_i++) {
				passthruCells[out_i] = inrow[passthruInds[out_i]];
			}
			
			String textInput = JsonUtil.parse(inrow[textInd]).asText();
		    // create an empty Annotation just with the given text
		    Annotation document = new Annotation(textInput);

		    // run all Annotators on this text
		    pipeline.annotate(document);
		    
		    // Output mode, sentences

		    // these are all the sentences in this document
		    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
		    ArrayList<CoreMap> sentences = Lists.newArrayList(document.get(SentencesAnnotation.class));
		    for (int sentnum=0; sentnum<sentences.size(); sentnum++) {
		    	CoreMap sentence = sentences.get(sentnum);
	    		ArrayList<CoreLabel> stTokens = Lists.newArrayList(sentence.get(TokensAnnotation.class));
	    		int T = stTokens.size();
		    	String[] surface = new String[T];
		    	
		    	for (int t=0; t<T; t++) {
		    		String word = stTokens.get(t).get(TextAnnotation.class);
		    		surface[t] = word;

		    		//		        String pos = token.get(PartOfSpeechAnnotation.class);
		    		//		        String ne = token.get(NamedEntityTagAnnotation.class);       
			    	// this is the parse tree of the current sentence
			    	//		      Tree tree = sentence.get(TreeAnnotation.class);

			    	// this is the Stanford dependency graph of the current sentence
			    	//		      SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
		    	}
		    	String sentid = String.format("S%d", sentnum);
		    	String payload= StringUtils.join(surface, " ");
		    	String[] outrow = ObjectArrays.concat(passthruCells, new String[]{ sentid, payload }, String.class);
		    	System.out.println(StringUtils.join(outrow, "\t"));
		    }

		    // This is the coreference link graph
		    // Each chain stores a set of mentions that link to each other,
		    // along with a method for getting the most representative mention
		    // Both sentence and token offsets start at 1!
		    //		    Map<Integer, CorefChain> graph = 
		    //		      document.get(CorefChainAnnotation.class);
		}
	}
}
