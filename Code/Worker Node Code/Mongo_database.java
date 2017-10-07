import com.mongodb.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * @author Arjun Dhuliya
 */
public class Mongo_database {
    private static DB db;
    private static final String[] attributes = {"Page_Name", "Page_Content"};

    private static String get_collection(DBCollection table) {
//        BasicDBObject q = new BasicDBObject();
////        q.put("Page_Name", "RIT");
//        q.put("Page_Name", 1);
        StringBuilder result = new StringBuilder();
        DBCursor page_content = table.find();
        while (page_content.hasNext()) {
//            result += page_content.next().get("Page_Content") + "\n";
            DBObject next = page_content.next();
            result.append(next.get("Page_Name")).append(",").append(next.get("Page_Content")).append("\n");
        }
        System.out.println(result.length());
        System.out.println("retrieved content based on query!");
        System.out.println(result);
//        table.drop();
        return result.toString();
    }

    public static void main(String[] args) throws Exception {
        Mongo_database mdb = new Mongo_database();
        ArrayList<String> dbIp = WorkerNode.dbIp;
        for (int i = 0; i < dbIp.size(); i++) {
            String ip = dbIp.get(i);
            int port = WorkerNode.dbPort.get(i);
            mdb.connect_to_db(ip, port);
        }

        mdb.create_collection("content.txt", "Table1");
        System.out.println();
        get_collection(mdb.get_table("Table1"));
    }

    private DBCollection create_collection(String filename, String table_name) throws Exception {
        db.getCollection(table_name).drop();
        DBCollection table = db.createCollection(table_name, null);

        DBCollection get_table = db.getCollection(table_name);

        BasicDBObject doc;

        FileReader file = new FileReader(filename);
        BufferedReader br = new BufferedReader(file);
        String l;
        String[] line;
        while ((l = br.readLine()) != null) {
            line = l.split(",");

            doc = new BasicDBObject(attributes[0], line[0]);
            for (int j = 1; j < attributes.length; j++) {
                doc.append(attributes[j], line[j]);
            }
            get_table.insert(doc);
        }
        br.close();
        System.out.println("Collection created successfully");

        DBCursor count = table.find();
        System.out.println("total entries in " + table_name + " " + count.count());
        return table;
    }

    private DBCollection get_table(String table_name) {
        return db.getCollection(table_name);
    }

    private void connect_to_db(String ip, int port) {
        MongoClient mongoClient = new MongoClient(ip, port);
        db = mongoClient.getDB("Wiki_database");
        System.out.println("Connected to database " + db.getName());
    }
}
