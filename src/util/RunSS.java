package util;
import java.io.IOException;
import java.util.*;

import edu.cmu.ark.*;
import edu.stanford.nlp.util.StringUtils;


/** Run Mike's supersense tagger on parallel-token TSent input. 
 * 
 * requires you to be in the working directory of the SST software.
 * it has config and stuff to try to not require this, but too hard to figure out, screw it
 * **/
public class RunSS {
	public static void main(String args[]) throws IOException {
		
		DiscriminativeTagger sst;
		DiscriminativeTagger.loadProperties("tagger.properties");
		sst = DiscriminativeTagger.loadModel("models/superSenseModelAllSemcor.ser.gz");
		
		String line;
		while ((line = BasicFileIO.stdin().readLine()) != null) {
//			Sentence sent = Sentence.readTSVLine(line);
			String[] parts = line.split("\t");
			if ( ! parts[1].startsWith("S")) {
				U.p(line);
				continue;
			}
			String[] tokens = parts[2].split(" ");
			String[] lemmas = parts[3].split(" ");
			String[] poses = parts[4].split(" ");
			
			LabeledSentence labeled = prepareSentenceForSST(tokens, lemmas, poses);
			sst.findBestLabelSequenceViterbi(labeled, sst.getWeights());
			List<String> ssTags = labeled.getPredictions();
			String outline = line + "\t" + StringUtils.join(ssTags, " ");
			U.p(outline);
		}
	}
	
	public static LabeledSentence prepareSentenceForSST(String[] tokens, String[] lemmas, String[] poses) {
		LabeledSentence ls = new LabeledSentence();
		for (int t=0; t < tokens.length; t++) {
			ls.addToken(tokens[t], lemmas[t], poses[t], "0");
		}
		return ls;
	}

//	public static LabeledSentence prepareSentenceForSST(Sentence sent) {
//		LabeledSentence ls = new LabeledSentence();
//		for (int t=0; t < sent.T(); t++) {
//			Token tok = sent.tokens.get(t);
//			ls.addToken(tok.word, tok.lemma, tok.posTag, "0");
//		}
//		return ls;
//	}

}
