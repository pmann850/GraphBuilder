/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphbuilder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author patrickmann
 */
public class DBTools {

    public static Connection getConnection(String host, String port) throws ClassNotFoundException, SQLException {
        Class.forName("org.neo4j.jdbc.Driver");
        return DriverManager.getConnection("jdbc:neo4j://"+host+":"+port+"/");
    }

    public static List<Map<String,Object>> query(String query, Statement stmt) throws SQLException {
        List<Map<String,Object>> results = new LinkedList();
        ResultSet rs = stmt.executeQuery(query);
        while(rs.next()) {
            Map map = new HashMap();
            for(int i=0;i<rs.getMetaData().getColumnCount();i++) {
                String name = rs.getMetaData().getColumnName(i+1);
                String value = rs.getString(i+1);
                map.put(name,value);
            }
            results.add(map);
        }
        return results;
    }
    
    public static void createNode(Map<String, Object> node, String type, Statement stmt) throws SQLException {
        String json = "";
        for (String name : node.keySet()) {
            if (!json.isEmpty()) {
                json += ", ";
            }
            Object value = node.get(name);
            name = name.replaceAll("-", "").replaceAll(" ", "");
            if (value instanceof String) {
                value = "'" + value + "'";
            } else if (value instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                value = sdf.format((Date) value);
            } else if (value instanceof Map) {
                value = "'" + value + "'";
            } else if (value instanceof Collection) {
                value = "'" + value + "'";
            }
            json += name + ":" + value;
        }
        json = "{" + json + "}";

        //String id = prefix + node.get(pk);
        String create = "CREATE (:" + type + " " + json + ")";
        System.out.println(create);
        stmt.executeUpdate(create);
    }

    public static void updateNode(String match, String key, Map<String, Object> node, Statement stmt) throws SQLException {

        String set = "";
        for (String name : node.keySet()) {
            if (!set.isEmpty()) {
                set += ", ";
            }
            Object value = node.get(name);
            name = name.replaceAll("-", "").replaceAll(" ", "");
            if (value instanceof String) {
                value = "'" + value + "'";
            } else if (value instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                value = sdf.format((Date) value);
            } else if (value instanceof Map) {
                value = "'" + value + "'";
            } else if (value instanceof Collection) {
                value = "'" + value + "'";
            }
            set += " " + key + "." + name + "=" + value;
        }

        String update = "MATCH " + match + " SET " + set;
        System.out.println(update);
        stmt.executeUpdate(update);

    }

    public static void createChild(String match, String key, Map<String, Object> node, String relationship, String type, Statement stmt) throws SQLException {

        String json = "";
        for (String name : node.keySet()) {
            if (!json.isEmpty()) {
                json += ", ";
            }
            Object value = node.get(name);
            name = name.replaceAll("-", "").replaceAll(" ", "");
            if (value instanceof String) {
                value = "'" + value + "'";
            } else if (value instanceof Date) {
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                value = sdf.format((Date) value);
            } else if (value instanceof Map) {
                value = "'" + value + "'";
            } else if (value instanceof Collection) {
                value = "'" + value + "'";
            }
            json += name + ":" + value;
        }
        json = "{" + json + "}";

        String create = "MATCH " + match + " CREATE (" + key + ")-[:" + relationship + "]->(Child:" + type + " " + json + ")";
        System.out.println(create);
        stmt.executeUpdate(create);
    }

    public static void createRelationship(Map<String, Object> node, String pkName, String pkType, String relationship, String fkName, String fkType, Statement stmt) throws SQLException {
        String pkId = "pk";
        String pk = "(pk:" + pkType + " {" + pkName + ":'" + node.get(pkName) + "'})";
        String fkId = "fk";
        String fk = "(fk:" + fkType + " {" + pkName + ":'" + trim(node.get(fkName)) + "'})";
        createRelationship(pk,fk,pkId,fkId,relationship,"",stmt);
    }
    
    public static void createRelationship(String pk, String fk, String pkId, String fkId, String relationship, String prop, Statement stmt) throws SQLException {
        String create = null;
        if(prop==null || prop.isEmpty()) {
            create = "MATCH " + pk + "," + fk + " CREATE (" + pkId + ")-[:" + relationship + "]->(" + fkId + ")";
        } else {
            create = "MATCH " + pk + "," + fk + " CREATE (" + pkId + ")-[:" + relationship + " "+ prop +"]->(" + fkId + ")";
        }
        System.out.println(create);
        stmt.executeUpdate(create);
    }

    private static String trim(Object value) {
        return value.toString().split("\\.")[0];
    }

}
