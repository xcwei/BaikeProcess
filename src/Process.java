package src;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;

import org.jdom.JDOMException;

import ltpService.LTML;
import ltpService.LTPOption;
import ltpService.LTPService;
import ltpService.SRL;
import ltpService.Word;

import seg.Segmenter;

import Utility.ComparatorWord;
import Utility.IO;

public class Process {
	public LTPService ls;
	Hashtable<String, Boolean> stopwords = new Hashtable<String, Boolean>();

	public Process() {
		// ls = new LTPService("wxchi@qq.com:sBJjONvy");
		try {
			ArrayList<String> content = IO.loadFile("stopWord.txt");
			for (int i = 0; i < content.size(); i++) {
				stopwords.put(content.get(i).trim(), true);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Process pr = new Process();
		String root = "E:\\Xiaochi Wei\\Py space\\Baike Parser\\part-r-00000_dir_text\\";
		File f_root = new File(root);
		String[] f_list = f_root.list();
		for (int i = 0; i < f_list.length; i++) {
			System.out.println(f_list[i]);
			pr.ls = new LTPService("wxchi@qq.com:sBJjONvy");
			try {
				pr.Process(root + "\\" + f_list[i],
						"E:\\Xiaochi Wei\\Py space\\Baike Parser\\TestResult.txt");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(f_list[i] + " Error");
				e.printStackTrace();
			}
		}

		// pr.ls = new LTPService("wxchi@qq.com:sBJjONvy");
		// try {
		// pr.Process(root + "\\28409.txt",
		// "E:\\Xiaochi Wei\\Py space\\Baike Parser\\TestResult.txt");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public ArrayList<String> segSentence(String s) {
		ArrayList<String> arr_sentence = new ArrayList<>();
		s = s.replace("。", "。\t");
		s = s.replace("！", "！\t");
		s = s.replace("？", "？\t");
		s = s.replace("；", "；\t");
		String[] sentences = s.split("\t");
		for (int i = 0; i < sentences.length; i++) {
			arr_sentence.add(sentences[i]);
		}
		return arr_sentence;
	}

	public void Process(String path, String outPath) throws Exception {
		Segmenter seg = new Segmenter();
		ArrayList<String> content = IO.loadFile(path);
		String title = content.get(0);
		System.out.println(title);
		for (int i = 1; i < content.size(); i++) {
			ArrayList<String> para = segSentence(content.get(i));
			for (int j = 0; j < para.size(); j++) {
				String s = para.get(j);
				s = s.trim();
				s = s.replace(" ", "");
				if (sentDete(s)) {
					SentenceProcess(s, outPath);
				}
			}
		}
	}

	public boolean sentDete(String s) {
		boolean result = true;
		if (s.length() < 3)
			result = false;
		else if (s.length() > 100)
			result = false;
		else if (!(s.endsWith("。") || s.endsWith("！") || s.endsWith("？")
				|| s.endsWith(".") || s.endsWith("；") || s.endsWith(";") || s
					.endsWith("!")))
			result = false;
		else if (!matchSymble(s, "‘", "’"))
			result = false;
		else if (!matchSymble(s, "“", "”"))
			result = false;
		else if (!matchSymble(s, "（", "）"))
			result = false;
		else if (!matchSymble(s, "【", "】"))
			result = false;
		else if (!matchSymble(s, "{", "}"))
			result = false;
		else if (!matchSymble(s, "《", "》"))
			result = false;
		return result;
	}

	public boolean matchSymble(String s, String sy1, String sy2) {
		Boolean result = true;
		int t = 1;
		if (s.contains(sy1))
			t *= -1;
		if (s.contains(sy2))
			t *= -1;
		if (t == -1)
			result = false;
		else
			result = true;

		if (s.indexOf(sy1) > s.indexOf(sy2))
			result = false;

		return result;
	}

	public void SentenceProcess(String s, String outPath) throws Exception {
		System.out.println("\n" + s);
		ls.setEncoding(LTPOption.UTF8);

		LTML ltml = ls.analyze(LTPOption.ALL, s);

		int sentNum = ltml.countSentence(); // sentence number
		for (int ii = 0; ii < sentNum; ii++) {
			ArrayList<Word> candidate = new ArrayList<Word>();
			ArrayList<Word> sentWords = ltml.getWords(ii);
			int root_n = -1;
			for (int j = 0; j < sentWords.size(); j++) {
				Word w = sentWords.get(j);
				int parent = w.getParserParent();
				if (parent != -1)
					sentWords.get(parent).children.add(w);

				if (w.getParserParent() == -1) {
					root_n = j;
				}
			}

			for (int j = 0; j < sentWords.size(); j++) {
				Word w = sentWords.get(j);
				StringBuffer A00 = new StringBuffer();
				StringBuffer A10 = new StringBuffer();
				StringBuffer A01 = new StringBuffer();
				StringBuffer A11 = new StringBuffer();

				ArrayList<ArrayList<Word>> wa0s = new ArrayList<ArrayList<Word>>();
				ArrayList<Word> t1 = new ArrayList<Word>();
				wa0s.add(t1);
				ArrayList<ArrayList<Word>> wa1s = new ArrayList<ArrayList<Word>>();
				ArrayList<Word> t2 = new ArrayList<Word>();
				wa1s.add(t2);

				if (ParseMethod(sentWords, w, A00, A10, wa0s, wa1s)) {
//					for (int l = 0; l < wa1s.size(); l++) {
//						ArrayList<Word> t = wa1s.get(l);
//						for (int m = 0; m < t.size(); m++) {
//							System.out.print(t.get(m).getWS());
//						}
//						System.out.println();
//					}
					StringBuffer sb = SRLMethod(sentWords, w, A01, A11);
					if (A00.toString().equals(A01.toString())
							&& A10.toString().equals(A11.toString())) {
//						sb.append("\t" + s + "\n");
						
						IO.appendFile(sb, outPath);
					}
				}
			}
		}
	}

	private StringBuffer Format(ArrayList<ArrayList<Word>> wa0s, ArrayList<ArrayList<Word>> wa1s){
		StringBuffer sb = new StringBuffer();
		
		
		Word[] warr_sbv = new Word[arr_sbv.size()];
		for (int j = 0; j < arr_sbv.size(); j++) {
			warr_sbv[j] = arr_sbv.get(j);
		}
		Word[] warr_vob = new Word[arr_vob.size()];
		for (int j = 0; j < arr_vob.size(); j++) {
			warr_vob[j] = arr_vob.get(j);
		}

		int n_nunstop = 0;
		Arrays.sort(warr_sbv, new ComparatorWord());
		Arrays.sort(warr_vob, new ComparatorWord());
		
		return sb;
	}
	
	private boolean ParseMethod(ArrayList<Word> sentWords, Word wroot,
			StringBuffer A0, StringBuffer A1, ArrayList<ArrayList<Word>> wa0s,
			ArrayList<ArrayList<Word>> wa1s) {
		boolean result = false;
		Word w_ROOT = wroot;
		Boolean Flag_SBV = false;
		Boolean Flag_VOB = false;
		Boolean Flag_COO = false;
		Word w_SBV = new Word();
		Word w_VOB = new Word();
		Word w_COO = new Word();
		for (int i = 0; i < w_ROOT.children.size(); i++) {
			Word w = w_ROOT.children.get(i);
			if (w.getParserRelation().equals("SBV")) {
				Flag_SBV = true;
				w_SBV = w;
			} else if (w_ROOT.getParserRelation().equals("COO")) {
				Flag_COO = true;
				w_COO = sentWords.get(w_ROOT.getParserParent());
			}

			if (w.getParserRelation().equals("VOB")) {
				Flag_VOB = true;
				w_VOB = w;
			}
		}

		if (Flag_VOB && (Flag_SBV || Flag_COO) && wroot.getPOS().equals("v")) {
			result = false;
			ArrayList<Word> arr_vob = new ArrayList<Word>();
			ArrayList<Word> arr_sbv = new ArrayList<Word>();
			// orderTree(arr_vob, w_VOB);
			orderTreeEx(arr_vob, w_VOB, wa1s);
			if (Flag_SBV) {
				orderTree(arr_sbv, w_SBV);
				orderTreeEx(arr_sbv, w_SBV, wa0s);
				result = true;
			} else {
				for (int i = 0; i < w_COO.children.size(); i++) {
					Word w = w_COO.children.get(i);
					if (w.getParserRelation().equals("SBV")) {
						result = true;
						// orderTree(arr_sbv, w);
						orderTreeEx(arr_sbv, w, wa0s);
						break;
					}
				}
			}

			if (result) {
				Word[] warr_sbv = new Word[arr_sbv.size()];
				for (int j = 0; j < arr_sbv.size(); j++) {
					warr_sbv[j] = arr_sbv.get(j);
				}
				Word[] warr_vob = new Word[arr_vob.size()];
				for (int j = 0; j < arr_vob.size(); j++) {
					warr_vob[j] = arr_vob.get(j);
				}

				int n_nunstop = 0;
				Arrays.sort(warr_sbv, new ComparatorWord());
				Arrays.sort(warr_vob, new ComparatorWord());
				for (int j = 0; j < warr_sbv.length; j++) {
					// System.out.print(warr_sbv[j].getWS());
					String w = warr_sbv[j].getWS();
					A0.append(w);
					if (stopwords.get(w) == null) {
						n_nunstop++;
					}
				}
				if (n_nunstop == 0) {
					result = false;
					return result;
				} else {
					n_nunstop = 0;
				}

				// System.out.print("\t" + w_ROOT.getWS() + "\t");

				for (int j = 0; j < warr_vob.length; j++) {
					// System.out.print(warr_vob[j].getWS());
					String w = warr_vob[j].getWS();
					A1.append(w);
					if (stopwords.get(w) == null) {
						n_nunstop++;
					}
				}
				if (n_nunstop == 0) {
					result = false;
					return result;
				}
			}
		}

		return result;
	}

	public void orderTree(ArrayList<Word> cand, Word root) {
		cand.add(root);
		for (int i = 0; i < root.children.size(); i++) {
			orderTree(cand, root.children.get(i));
		}
	}

	public void orderTreeEx(ArrayList<Word> cand, Word root,
			ArrayList<ArrayList<Word>> cands) {
		cand.add(root);
		if (root.getParserRelation().equals("COO")) {
			ArrayList<Word> arr = new ArrayList<Word>();
			arr.add(root);
			cands.add(arr);
		} else {
			ArrayList<Word> arr = cands.get(cands.size() - 1);
			arr.add(root);
		}

		for (int i = 0; i < root.children.size(); i++) {
			orderTreeEx(cand, root.children.get(i), cands);
		}
	}

	private StringBuffer SRLMethod(ArrayList<Word> sentWords, Word w,
			StringBuffer A0, StringBuffer A1) {
		StringBuffer sb = new StringBuffer();
		if (w.isPredicate() && stopwords.get(w.getWS()) == null) {
			ArrayList<SRL> srls = w.getSRLs();

			boolean flag_a0 = false;
			int k_a0 = -1;
			boolean flag_a1 = false;
			int k_a1 = -1;
			for (int k = 0; k < srls.size(); k++) {
				if (srls.get(k).type.equals("A0")) {
					flag_a0 = true;
					k_a0 = k;
				} else if (srls.get(k).type.equals("A1")) {
					flag_a1 = true;
					k_a1 = k;
				}
			}

			if (flag_a0 && flag_a1) {
				// System.out.println("Ans");
				SRL srl_a0 = srls.get(k_a0);
				for (int m = srl_a0.beg; m <= srl_a0.end; m++) {
					if (sentWords.get(m).getPOS().equals("n")) {
						A0.append(sentWords.get(m).getWS());
						sb.append(sentWords.get(m).getWS());
					} else {
						A0 = new StringBuffer();
						sb = new StringBuffer();
						return sb;
					}
					// System.out.print(sentWords.get(m).getWS());
				}
				// System.out.print("\t" + w.getWS() + "\t");
				sb.append("\t" + w.getWS() + "\t");

				SRL srl_a1 = srls.get(k_a1);
				for (int m = srl_a1.beg; m <= srl_a1.end; m++) {
					// System.out.print(sentWords.get(m).getWS());
					String pos = sentWords.get(m).getPOS();
					if (!pos.equals("v") && !pos.equals("r")) {
						A1.append(sentWords.get(m).getWS());
						sb.append(sentWords.get(m).getWS());
					} else {
						A1 = new StringBuffer();
						sb = new StringBuffer();
						return sb;
					}
				}
			}
		}
		return sb;
	}
}
