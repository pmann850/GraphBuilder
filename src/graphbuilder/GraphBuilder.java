/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphbuilder;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author patrickmann
 */
public class GraphBuilder {

    public static void processProviderDetail(Map<String, Object> physicianDetail, Statement stmt) throws SQLException {
        String id = (String) physicianDetail.get("ID");
        String match = "(PHY" + id + ":Physician {ID:'" + id + "'})";
        String key = "PHY" + id;

        //create child relationship for certifications
        List<Map<String, Object>> certs = (List) physicianDetail.get("board certifications");
        
        //we could process certifications like this
        //for(Map<String, Object> cert : certs) {
        //    DBTools.createChild(match, key, cert, "CERTIFICATION", "Certification", stmt);
        //}
        
        //we can also process certifications like this
        for(Map<String, Object> cert : certs) {
            String speciality = (String)cert.get("speciality");
            String date = (String)cert.get("date");
            String query = "MATCH (s:Specialty {name: '"+speciality+"'}) RETURN s";
            List<Map<String,Object>> results = DBTools.query(query,stmt);
            if(results.isEmpty()) {
                Map<String,Object> map = new java.util.HashMap();
                map.put("name", speciality);
                DBTools.createNode(map, "Specialty", stmt);
            }
            String fk = "(s:Specialty {name:'"+speciality+"'})";
            String fkId = "s";
            String prop = "{date:'"+date+"'}";
            DBTools.createRelationship(match, fk, key, fkId, "CERTIFIED", prop, stmt);
        }
    }
    
    public static void processPatientDetail(Map<String, Object> patientDetail, Statement stmt) throws SQLException {
        String id = (String) patientDetail.get("ID");
        String match = "(PAT" + id + ":Patient {ID:'" + id + "'})";
        String key = "PAT" + id;

        //merge patient detail
        Map<String, Object> patient = (Map) patientDetail.get("patient");
        DBTools.updateNode(match, key, patient, stmt);
        
        //create child relationship for certifications
        List<Map<String, Object>> meds = (List) patient.get("medications");
        for(Map<String, Object> med : meds) {
            DBTools.createChild(match, key, med, "MEDICATION", "Medication", stmt);
        }

        //create child relationship for companions
        Map<String, Object> companion = (Map) patientDetail.get("companion");
        DBTools.createChild(match, key, companion, "COMPANION", "Companion", stmt);
    }

    public static void buildGraph() {
        try {
            String host = System.getProperty("db.host", "localhost");
            String port = System.getProperty("db.port", "7474");
            String inputDir = System.getProperty("input.dir","/Develop/GraphTest/input/");
            
            Connection conn = DBTools.getConnection(host,port);
            Statement stmt = conn.createStatement();

            //parse the yaml files and create nodes
            List<Map<String, Object>> providers = YamlParser.loadYamlFile(new File(inputDir,"physician-details.yml"));
            for (Map<String, Object> provider : providers) {
                DBTools.createNode(provider,"Physician", stmt);
            }

            List<Map<String, Object>> patientDetails = YamlParser.loadYamlFile(new File(inputDir,"patient-details.yml"));
            for (Map<String, Object> patientDetail : patientDetails) {
                DBTools.createNode(patientDetail, "PatientDetail", stmt);
            }

            //parse the excel file and create nodes and relationships
            List<Map<String, Object>> patients = ExcelParser.loadExcelFile(new File(inputDir,"data-records.xlsx"));
            for (Map<String, Object> patient : patients) {
                DBTools.createNode(patient, "Patient", stmt);
                DBTools.createRelationship(patient, "ID", "Patient", "TREATED_BY", "Physician", "Physician", stmt);
                DBTools.createRelationship(patient, "ID", "Patient", "DETAIL", "ID", "PatientDetail", stmt);
            }

            //further process patient detail
            for (Map<String, Object> patientDetail : patientDetails) {
                processPatientDetail(patientDetail, stmt);
            }
            
            //further process physician detail
            for (Map<String, Object> provider : providers) {
                processProviderDetail(provider, stmt);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            FileInputStream in = new FileInputStream("config.properties");
            Properties prop = new Properties();
            prop.load(in);
            System.getProperties().putAll(prop);
            System.out.println("Running with custom settings...");
            prop.list(System.out);
        } catch(Exception ex) {
            System.out.println("Running with default settings...");
        }
        buildGraph();
    }
}
