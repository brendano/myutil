package vocabalts;

import gnu.trove.map.hash.TObjectIntHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.BasicFileIO;
//import gnu.trove.map.hash.TObjectIntHashMap;


/**
 * Feature vocabulary.
 * Implemented with Trove String=>int map.
 * 
 * Empirical comparison.
 * Task: load ~3million feature vocab (English data).  5 trials per setting.
 * Memory reporting below with Runtime.totalMemory(), which is smaller than the OS process size, but presumably is a better measurement.
 
Java String keys with HashMap:
Mem usage 644.0 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  12.42s user 0.43s system 101% cpu 12.689 total
Mem usage 644.5 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  12.35s user 0.44s system 101% cpu 12.610 total
Mem usage 646.1 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  12.45s user 0.44s system 101% cpu 12.721 total
Mem usage 646.3 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  12.39s user 0.43s system 101% cpu 12.640 total
Mem usage 644.3 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  12.51s user 0.42s system 101% cpu 12.755 total

Java String keys, TObjectIntHashMap, Trove 3.0.3
Mem usage 539.0 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  6.95s user 0.40s system 102% cpu 7.146 total
Mem usage 539.1 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  6.92s user 0.39s system 102% cpu 7.099 total
Mem usage 539.4 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  6.83s user 0.38s system 102% cpu 7.008 total
Mem usage 547.6 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  6.97s user 0.38s system 102% cpu 7.146 total
Mem usage 549.8 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  7.00s user 0.38s system 102% cpu 7.190 total

Java String keys, Object2IntOpenHashMap, fastutil 6.4.6
Mem usage 550.4 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  8.01s user 0.37s system 102% cpu 8.198 total
Mem usage 551.0 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  7.87s user 0.37s system 102% cpu 8.045 total
Mem usage 551.2 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  7.88s user 0.37s system 102% cpu 8.045 total
Mem usage 576.8 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  8.10s user 0.38s system 102% cpu 8.293 total
Mem usage 578.3 MB ./java.sh vocabalts.VocabWithString src/vocabalts/featnames.txt  8.01s user 0.37s system 102% cpu 8.182 total

 */
public class VocabWithString {

//	private Object2IntMap<String> name2num;
//	private TObjectIntHashMap<String> name2num;
	private HashMap<String,Integer> name2num;
	private ArrayList<String> num2name;

	private boolean isLocked = false;

	public VocabWithString() { 
		name2num = new HashMap<String,Integer>();
//		name2num = new Object2IntOpenHashMap<String>();
//		name2num = new TObjectIntHashMap<String>();
		num2name = new ArrayList<>();
	}

	public void lock() {
		isLocked = true;
	}
	public boolean isLocked() { return isLocked; }

	public int size() {
		assert name2num.size() == num2name.size();
		return name2num.size();
	}
	
	/** 
	 *  If not locked, an unknown name is added to the vocabulary.
	 *  If locked, return -1 on OOV.
	 * @param featname
	 * @return
	 */
	public int num(String featname) {
//		ByteString featname = new ByteString(encodeUTF8(_featname));
		if (! name2num.containsKey(featname)) {
			if (isLocked) return -1;
			int n = name2num.size();
			name2num.put(featname, n);
			num2name.add(featname);
			return n;
		} else {
			return name2num.get(featname);
		}
	}

	public String name(int num) {
		if (num2name.size() <= num) {
			throw new RuntimeException("Unknown number for vocab: " + num);
		} else {
			return num2name.get(num);
//			return decodeUTF8(num2name.get(num).data);
		}
	}

	public boolean contains(String name) {
		return name2num.containsKey(name);
	}

//	public String toString() {
//		return "[" + StringUtils.join(num2name) + "]";
//	}

	/** Throw an error if OOV **/
	public int numStrict(String string) {
		assert isLocked;
		int n = num(string);
		if (n == -1) throw new RuntimeException("OOV happened");
		return n;
	}
	
	/** please don't modify this return value, will cause problems */
	public List<String> names() {
		return num2name;
	}
	
	public void dump(String filename) throws IOException {
		BasicFileIO.writeLines(names(), filename);
	}
	
	public static void main(String[] args) throws IOException {
		VocabWithString vocab = new VocabWithString();
		for (String line : BasicFileIO.openFileLines(args[0])) {
			vocab.num(line);
		}
		System.gc();
		System.out.printf("Mem usage %.1f MB\n", Runtime.getRuntime().totalMemory()/1e6);

//		System.out.print("> ");
//		System.in.read();
	}

}

