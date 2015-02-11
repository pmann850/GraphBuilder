/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphbuilder;

import com.esotericsoftware.yamlbeans.YamlReader;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author patrickmann
 */
public class YamlParser {

    private static String ltrim(String str) {
        int startIndex = 0;
        while (startIndex < str.length() && Character.isWhitespace(str.charAt(startIndex))) {
            startIndex++;
        }
        return str.substring(startIndex);
    }

    public static List<Map<String, Object>> loadYamlFile(File file) {
        List<Map<String, Object>> records = new LinkedList();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            List<String> documents = new LinkedList();
            Map<String, String> docMap = new HashMap();
            String document = "";
            String key = null;
            while (true) {
                String data = reader.readLine();
                if (data == null) {
                    break;
                }
                if (data.equals("---") && !document.isEmpty()) {
                    if (key != null) {
                        docMap.put(key, document);
                    } else {
                        documents.add(document);
                    }
                    document = "";
                    key = null;
                }
                if (data.equals("---")) {
                    continue;
                }
                if (key == null && ltrim(data).startsWith("#")) {
                    key = ltrim(data.replaceAll("#", "")).trim();
                    continue;
                }
                document += data.replaceAll("\t", " ") + "\n";
            }
            if (key != null) {
                docMap.put(key, document);
            } else {
                documents.add(document);
            }
            reader.close();

            for (String doc : documents) {
                CharArrayReader creader = new CharArrayReader(doc.toCharArray());
                YamlReader yreader = new YamlReader(creader);
                Map map = (Map) yreader.read();
                records.add(map);
            }
            for (String docKey : docMap.keySet()) {
                String doc = docMap.get(docKey);
                CharArrayReader creader = new CharArrayReader(doc.toCharArray());
                YamlReader yreader = new YamlReader(creader);
                Map map = (Map) yreader.read();
                map.put("name", docKey);
                records.add(map);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return records;
    }

}
