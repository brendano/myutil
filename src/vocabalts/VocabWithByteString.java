package vocabalts;

import gnu.trove.map.hash.TObjectIntHashMap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import util.BasicFileIO;


/**
ByteString, HashMap
Mem usage 768.2 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  14.56s user 0.48s system 101% cpu 14.821 total
Mem usage 810.6 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  15.22s user 0.47s system 101% cpu 15.462 total
Mem usage 808.4 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  15.09s user 0.47s system 101% cpu 15.323 total
Mem usage 809.6 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  15.26s user 0.47s system 101% cpu 15.509 total
Mem usage 813.1 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  15.39s user 0.50s system 101% cpu 15.682 total

ByteString, TObjectIntHashMap (Trove 3.0.3)
Mem usage 528.9 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.42s user 0.38s system 103% cpu 8.494 total
Mem usage 530.9 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.07s user 0.36s system 103% cpu 8.133 total
Mem usage 532.6 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.05s user 0.36s system 103% cpu 8.104 total
Mem usage 534.7 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.04s user 0.37s system 103% cpu 8.100 total
Mem usage 535.0 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.07s user 0.36s system 103% cpu 8.110 total

ByteString, Object2IntOpenHashMap (fastutil 6.4.6)
Mem usage 565.8 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.58s user 0.38s system 102% cpu 8.712 total
Mem usage 566.4 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.56s user 0.38s system 102% cpu 8.705 total
Mem usage 570.7 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.63s user 0.38s system 102% cpu 8.780 total
Mem usage 577.0 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.43s user 0.39s system 102% cpu 8.580 total
Mem usage 578.0 MB ./java.sh vocabalts.VocabWithByteString src/vocabalts/featnames.txt  8.49s user 0.43s system 102% cpu 8.690 total



 */
public class VocabWithByteString {

//	private HashMap<ByteString, Integer> name2num;
//	private TObjectIntHashMap<ByteString> name2num;
	private Object2IntMap<ByteString> name2num;
	private ArrayList<ByteString> num2name;

	private boolean isLocked = false;

	public VocabWithByteString() { 
//		name2num = new HashMap<>();
//		name2num = new TObjectIntHashMap<>();
		name2num = new Object2IntOpenHashMap<>();
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
	public int num(String _featname) {
		ByteString featname = new ByteString(encodeUTF8(_featname));
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
			return decodeUTF8(num2name.get(num).data);
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
		ArrayList<String> ret = new ArrayList<>();
		for (ByteString s : num2name)  ret.add(decodeUTF8(s.data));
		return ret;
	}
	
	public void dump(String filename) throws IOException {
		BasicFileIO.writeLines(names(), filename);
	}
	
	// http://stackoverflow.com/a/3386646/86684

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

	private static String decodeUTF8(byte[] bytes) {
	    return new String(bytes, UTF8_CHARSET);
	}

	private static byte[] encodeUTF8(String string) {
	    return string.getBytes(UTF8_CHARSET);
	}


	public static void main(String[] args) throws IOException {
		VocabWithByteString vocab = new VocabWithByteString();
		for (String line : BasicFileIO.openFileLines(args[0])) {
			vocab.num(line);
		}
		System.gc();
		System.out.printf("Mem usage %.1f MB\n", Runtime.getRuntime().totalMemory()/1e6);

	}

}


class ByteString {
	final byte[] data;
	
    public ByteString(byte[] data){
        if (data == null){
            throw new NullPointerException();
        }
        this.data = data;
    }

	@Override
	public boolean equals(Object other){
        if (!(other instanceof ByteString)){
            return false;
        }
        return Arrays.equals(data, ((ByteString) other).data);
    }
	
    @Override
    public int hashCode(){
        return Arrays.hashCode(data);
    }
}
