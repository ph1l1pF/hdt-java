package org.rdfhdt.hdt.dictionary.impl;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HuffmanFacade {

    public static final String FILE_LITERALS = "strings.ser";
    public static final String FILE_TREE = "tree.ser";
    public static final String FILE_CHARS = "chars.ser";


    public static Huffman.HuffmanNode treeRoot;

    public static Map<Character, String> charCode= new LinkedHashMap<>();

    private static List<String> encodedStrings = new ArrayList<>();

    public static void findCharacterCounts(String filePath){
        Model model = null;
        try {
            model = ModelFactory.createDefaultModel().read(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<Character, Integer> mapCharToCount = new LinkedHashMap<>();
        ExtendedIterator<Triple> tripleExtendedIterator = model.getGraph().find();
        while (tripleExtendedIterator.hasNext()) {
            Node object = tripleExtendedIterator.next().getObject();
            if(object.isLiteral()) {
                String s = "\""+object.getLiteral().toString()+"\"";
                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);
                    if (mapCharToCount.containsKey(c)) {
                        mapCharToCount.put(c, mapCharToCount.get(c) + 1);
                    } else {
                        mapCharToCount.put(c, 1);
                    }
                }
            }
        }

        encodedStrings.clear();
        treeRoot = Huffman.buildTree(mapCharToCount);
        charCode = Huffman.generateCodes(mapCharToCount.keySet(), treeRoot);

        System.out.println("Finished construction of Huffman Tree.");
    }

    public static boolean addEncodedString(CharSequence str){
        final StringBuilder binaryCode = new StringBuilder();
        for (int i = 0; i < str.toString().length(); i++) {
            char c = str.toString().charAt(i);
            String cCode = charCode.get(c);
            if(cCode==null){
                // this happens if the literal is combined with a URI
                return false;
            }
            binaryCode.append(cCode);
        }

        encodedStrings.add(binaryCode.toString());
        return true;
    }

    public static void storeData(){
        Huffman.serializeMessages(encodedStrings, FILE_LITERALS);
        Huffman.serializeTree(treeRoot, FILE_TREE,FILE_CHARS);
    }
}
