import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    int count;
    int docsCount;

    TrieNode() {
        isEndOfWord = false;
        count = 0;
        docsCount = 0;
    }
}

class TestTrie {
    TrieNode root = new TrieNode();
    int size;

    TestTrie() {
        size = 0;
    }

    public int size() {
        return size;
    }

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        size++; 
        node.count++;
        node.isEndOfWord = true;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }

    public boolean updateDocsCount(String word, int count) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        node.docsCount = count;
        return node.isEndOfWord;
    }

    public int countOccurrences(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (node.children[index] == null) {
                return 0;
            }
            node = node.children[index];
        }
        return node.isEndOfWord ? node.count : 0;
    }

    public int docsCountOccurrences(String word) {
        TrieNode node = root;
        for (char ch : word.toCharArray()) {
            int index = ch - 'a';
            if (node.children[index] == null) {
                return 0;
            }
            node = node.children[index];
        }
        return node.isEndOfWord ? node.docsCount : 0;
    }
}

public class TFIDFCalculator {
    public static double tf(TestTrie doc, String term) {
        double number_term_in_doc = doc.countOccurrences(term);
        return number_term_in_doc / doc.size();
    }
    public static double idf(ArrayList<TestTrie> docList, TestTrie docs, String term) {
        double number_doc_contain_term = docs.docsCountOccurrences(term); 
        return Math.log(docList.size() / number_doc_contain_term);
    }
    
    public static double tfIdfCalculate(ArrayList<TestTrie> docList, TestTrie doc, TestTrie docs, String term) {
          return tf(doc, term) * idf(docList, docs, term);
    }

    public static ArrayList<TestTrie> SplitDocs(String content) {
        ArrayList<String> segments = new ArrayList<>();
        ArrayList<TestTrie> docs = new ArrayList<>();
        StringBuilder segment = new StringBuilder();
        String[] splitContent;
        String[] tempSegment;
        int targetLine = 0;
        
        splitContent = content.split("\n");
        //String[] subArray = Arrays.copyOfRange(splitContent, (Integer.parseInt(lineNum) * 5), (Integer.parseInt(lineNum) * 5) + 5);
        for(String line : splitContent) {     
            //System.out.println(line);  
            line = line.toLowerCase();
            line = line.replaceAll("[^a-zA-Z]"," ");
            line = line.replaceAll("\\s+"," ").trim();
            segment.append(line).append(" ");
            targetLine ++;

            if(targetLine == 5) {
                //segments.add(segment.toString());
                tempSegment = segment.toString().split("\\s+");
                TestTrie segmentTrie = new TestTrie();
                for(String iSegment : tempSegment) {
                    //System.out.print("Segment " + lineNum + ": " + "-------");
                    //System.out.println(iSegment);
                    segmentTrie.insert(iSegment);
                }
                docs.add(segmentTrie);
                segment.setLength(0);
                targetLine = 0;
                //System.out.println(segmentTrie.countOccurrences("t"));
            }
        }
        //System.out.println(segments.get(32));
        return docs;
    }

    public static ArrayList<String> removeDuplicate(ArrayList<String> testcase, TestTrie indexTrie) {
        ArrayList<String> newArray = new ArrayList<>();
        for(String tc : testcase) {
            if(!indexTrie.search(tc)) {
                indexTrie.insert(tc);
                newArray.add(tc);
            }
        }
        return newArray;

    }

    public static void main(String[] args) {
        String filePath = args[0];
        String testcase = args[1];
        String content = "";
        String[] temp;
        ArrayList<TestTrie> lineSegment = new ArrayList<>();
        ArrayList<String> tcString = new ArrayList<>();
        ArrayList<String> tcNum = new ArrayList<>();
        //System.out.println(tcNum.size());
        ArrayList<String> sortedTcString = new ArrayList<>();
        TestTrie indexTrie = new TestTrie();

        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
            temp = Files.readString(Paths.get(testcase)).split("\n");
            for(String i : temp[0].split("\\s+")) {
                //System.out.println(i);
                tcString.add(i);
            }
            for(String i : temp[1].split("\\s+")) {
                //System.out.println(i);
                tcNum.add(i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("start--------");
        lineSegment = SplitDocs(content);
        System.out.println("end-------");

        sortedTcString = removeDuplicate(tcString, indexTrie);
        for(String i : sortedTcString) {
            int count = 0;
            for(TestTrie j : lineSegment) {
                if(j.search(i)) {
                    count ++;
                }
            }
            indexTrie.updateDocsCount(i, count);
            //System.out.println(indexTrie.docsCountOccurrences(i));
        }

        //System.out.println(lineSegment.size());
        
        String fileName = "output.txt";

        try {
            FileWriter writer = new FileWriter(fileName);
            for(int i = 0; i < tcNum.size(); i ++) {
                //System.out.println(tf(lineSegment.get(Integer.parseInt(tcNum.get(i))), tcString.get(i)));
                //System.out.println(idf(lineSegment, indexTrie, tcString.get(i)));
                //System.out.println(indexTrie.docsCountOccurrences("the"));
                double tfIdf = tfIdfCalculate(lineSegment, lineSegment.get(Integer.parseInt(tcNum.get(i))), indexTrie, tcString.get(i));
                writer.write(String.format("%.5f", tfIdf) + " ");
                //System.out.println(tfIdf);
            }

            writer.close();

            System.out.println("Success " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
