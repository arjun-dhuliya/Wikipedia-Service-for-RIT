import com.mongodb.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;

public class Handlers {
    private final static ArrayList<DB> db;
    private static String hostname;


    static {
        db = new ArrayList<>();
        try {
            connect_to_db();
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static DBCollection get_table(String table_name, String pageName) {
        int hash = Arrays.hashCode(pageName.toCharArray());
        System.out.println(pageName+",hash: "+hash);
        int index = Math.abs(hash % db.size());
        System.out.println("Using :"+db.get(index).toString());
        return db.get(index).getCollection(table_name);
    }

    private static ArrayList<DBCollection> get_all_table(String table_name) {
        ArrayList<DBCollection> dbs = new ArrayList<>();
        for (DB d : db) {
            dbs.add(d.getCollection(table_name));
        }
        return dbs;
    }

    private static String get_collection(DBCollection table, String pageName) {
        BasicDBObject q = new BasicDBObject();
        q.put("Page_Name", pageName);
        StringBuilder result = new StringBuilder();
        DBCursor page_content = table.find(q);
        while (page_content.hasNext()) {
            result.append(page_content.next().get("Page_Content")).append("\n");
        }
        System.out.println("retrieved content based on query! "+pageName+" "+table.toString());
        System.out.println(result);
        return result.toString();
    }

    private static void set_collection(DBCollection table, String pageName, String value) {
        BasicDBObject q = new BasicDBObject("Page_Name", pageName);
        BasicDBObject updateTo = new BasicDBObject("$set", new BasicDBObject("Page_Content", value));
        table.update(q, updateTo);
//        DBCursor dbObjects = table.find(q);
//        while (dbObjects.hasNext()) {
//            System.out.print(dbObjects.next().get("Page_Content") + " ");
//        }
//        System.out.println();
//        System.out.println("retrieved content based on query!");
    }

    private static void add_collection(DBCollection table, ArrayList<String> values) {
        BasicDBObject q = new BasicDBObject("Page_Name", values.get(0)).append("Page_Content", values.get(1));
        WriteResult insert = table.insert(q);
//        DBCursor dbObjects = table.find(q);
//        while (dbObjects.hasNext()) {
//            System.out.print(dbObjects.next().get("Page_Content") + " ");
//        }
//        System.out.println();
//        System.out.println("retrieved content based on query!");
    }


    private static void connect_to_db() {
        ArrayList<String> dbIp = WorkerNode.dbIp;
        for (int i = 0; i < 1; i++) {
            String ip = dbIp.get(i);
            int port = WorkerNode.dbPort.get(i);
            MongoClient mongoClient = new MongoClient(ip, port);
            db.add(mongoClient.getDB("Wiki_database"));
            System.out.println("Connected to database " + db.get(i).getName());

        }


    }

    @SuppressWarnings("unchecked")
    private static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

        if (query != null) {
            String pairs[] = query.split("[&]");

            for (String pair : pairs) {
                String param[] = pair.split("[=]");

                String key = null;
                String value = null;
                if (param.length > 0) {
                    key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
                }

                if (param.length > 1) {
                    value = URLDecoder.decode(param[1], System.getProperty("file.encoding"));
                }

                if (parameters.containsKey(key)) {
                    Object obj = parameters.get(key);
                    if (obj instanceof List<?>) {
                        List<String> values = (List<String>) obj;
                        values.add(value);
                    } else if (obj instanceof String) {
                        List<String> values = new ArrayList<>();
                        values.add((String) obj);
                        values.add(value);
                        parameters.put(key, values);
                    }
                } else {
                    parameters.put(key, value);
                }
            }
        }
    }

    public static class RootHandler implements HttpHandler {

        final String RedirectHeader = "<!DOCTYPE HTML>\n<html lang=\"en-US\">\n<head>\n<meta charset=\"UTF-8\">\n<meta http-eq" +
                "uiv=\"refresh\" content=\"1; url=";
        //        "http://example.com\;
        final String url = "http://129.21.37.28:8080/echoGet?Hello=World";
        //		String url = "http://example.com";
        final String RedirectMid = "\">\n" +
                "<script type=\"text/javascript\">\n" +
                "window.location.href = ";
        final String RedirectFooter = "</script>\n" +
                "<title>Index Page</title>\n" +
                "</head>\n" +
                "<body></body>\n</html>";
        private String allPages;

        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Root Reached!!!");
            Map<String, Object> parameters = new HashMap<>();
            URI requestedUri = he.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);
            // send response
            String pageName = "";
            String value = "";
            for (String key : parameters.keySet()) {
                System.out.println("Had parameters so skipped!!");
                return;
            }

            ArrayList<String> pages = getAllPages(get_all_table("Table1"));
            String response = htmlFormat(pages);
//            String response = RedirectHeader + url + RedirectMid + url + RedirectFooter;
            he.sendResponseHeaders(200, response.length());
            OutputStream os = he.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String htmlFormat(ArrayList<String> pages) {
            StringBuilder sb = new StringBuilder();
            sb.append("<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<body></br><h1>Available Pages :</h1></br>");
            sb.append("<p><a href=\"/add\">Click Here to Add New Page</a></p></br>");
            for (String page : pages) {
                sb.append("<p><a href=\"");
//                sb.append(Config.VALID_IPS.get(0));
                sb.append("/get?");
                sb.append(page);
                sb.append("=value\">");
                sb.append(page);
                sb.append("</a></p>");
            }
            sb.append("</br><h1> generated by: ").append(WorkerNode.ownIp).append(" ");
            sb.append(hostname);
            sb.append("</h1></body>\n" +
                    "</html>");
//            System.out.println("Sent: " + sb.toString());
            return sb.toString();
        }

        ArrayList<String> getAllPages(ArrayList<DBCollection> tables) {
            ArrayList<String> allPages = new ArrayList<>();
            for (DBCollection table : tables) {
                DBCursor page_content = table.find();
                while (page_content.hasNext()) {
                    allPages.add(page_content.next().get("Page_Name") + "");
                }
            }
//            System.out.println("fetched: "+allPages.toString()+","+allPages.size());
            return allPages;
        }

    }

    public static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            System.out.println("Get Reached!!!");
            System.out.println("query page requested!!!");
            try {
                // parse request
                Map<String, Object> parameters = new HashMap<>();
                URI requestedUri = httpExchange.getRequestURI();
                String query = requestedUri.getRawQuery();
                parseQuery(query, parameters);
                // send response
                String pageName = "";
                for (String key : parameters.keySet()) {
                    pageName = key;
                    break;
                }
                String response;
                String table_results = get_collection(get_table("Table1", pageName), pageName);
                if (table_results.length() > 0) {
                    String editUrl = "yes.cs.rit.edu" + ":8080/edit?" + pageName + "=value";
                    response = "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>" + pageName + ":</h1></br>" +
                            table_results + "</br>" +
                            "<form action=\"" + editUrl + "\">\n" +
                            "<p><a href=\"" +
                            "/edit?" +
                            pageName +
                            "=value\">" +
                            "Go to edit" +
                            "</a>" +
//                            "<h1>" + pageName + ":</h1></br>" + table_results + "</br></br>"+
//                            "    <input type=\"submit\" value=\"Go to Edit\" />\n" +
                            "</br></br><h1>Generated by:" + WorkerNode.ownIp + " " + hostname +
                            "</h1></form>\n</html>";
//                    response = "<h1>" + pageName + ":</h1></br>" + table_results + "</br></br><input type=\"button\" onclick=\"location.href='" +editUrl+
//                            "';\" value=\"Go to Edit\" />\n" + "\n";
                } else
                    response = "<h1>No Such Page<h1>"+pageName+":"+table_results;
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class EditHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) {
            System.out.println("Edit Reached!!!");
            System.out.println("query page requested!!!");
            try {
                // parse request
                Map<String, Object> parameters = new HashMap<>();
                URI requestedUri = httpExchange.getRequestURI();
                String query = requestedUri.getRawQuery();
                parseQuery(query, parameters);
                // send response
                String pageName = "";
                for (String key : parameters.keySet()) {
                    pageName = key;
                    break;
                }
                String response;
                String table_results = get_collection(get_table("Table1", pageName), pageName);
                if (table_results.length() > 0) {
//                    String editUrl = "yes.cs.rit.edu"+":8080/edit?"+pageName+"=value";
                    response = "<!DOCTYPE html>\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>" + pageName + ":</h1></br>" +
                            "<form action=\"/submit\">\n" +
                            "  Contents of Page:<br>\n" +
                            "  <textarea type=\"text\" name=\"" + pageName + "\" value=\"" + table_results + "\">"+table_results+"</textarea>\n" +
                            "  <br><br>\n" +
                            "  <input type=\"submit\" value=\"Submit\">\n" +
                            "</form> " +
//                            "<p><a href=\""+
//                            "/edit?"+
//                            pageName+
//                            "=value\">"+
//                            "Submit"+
//                            "</a></p>"+
//                            "<h1>" + pageName + ":</h1></br>" + table_results + "</br></br>"+
//                            "    <input type=\"submit\" value=\"Go to Edit\" />\n" +
                            "</br><h1>Generated by:" + WorkerNode.ownIp + " " + hostname +
                            "</h1></form>\n</html>";
//                    response = "<h1>" + pageName + ":</h1></br>" + table_results + "</br></br><input type=\"button\" onclick=\"location.href='" +editUrl+
//                            "';\" value=\"Go to Edit\" />\n" + "\n";
                } else
                    response = "<h1>No Such Page<h1>";
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static class SubmitHandler implements HttpHandler {

        final String RedirectHeader = "<!DOCTYPE HTML>\n<html lang=\"en-US\">\n<head>\n<meta charset=\"UTF-8\">\n<meta http-eq" +
                "uiv=\"refresh\" content=\"1; url=";
        final String RedirectMid = "\">\n" +
                "<script type=\"text/javascript\">\n" +
                "window.location.href = \"";
        final String RedirectFooter = "\"</script>\n" +
                "<title>Page Redirection</title>\n" +
                "</head>\n" +
                "<body></body>\n</html>";

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Submit Reached!!!");
            // parse request
            Map<String, Object> parameters = new HashMap<>();
            URI requestedUri = httpExchange.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);
            // send response
            String pageName = "";
            String value = "";
            for (String key : parameters.keySet()) {
                pageName = key;
                value = (String) parameters.get(key);
                break;
            }
            set_collection(get_table("Table1", pageName), pageName, value);
            String url = "http://" + Config.VALID_IPS.get(0) + ":8080/updated";
//            System.out.println(System.currentTimeMillis() + "Redirected to: " + RedirectHeader + url + RedirectMid + url + RedirectFooter);
//            System.out.println("Redirected to: "+RedirectHeader+url+RedirectMid+url+RedirectFooter);
            String response = RedirectHeader + url + RedirectMid + url + RedirectFooter;
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
//            System.out.println("Changed value and redirected to monitor!!");
        }
    }

    public static class AddHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Add Reached!!!");
//            System.out.println("query page requested!!!");
            String response = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<body>\n" +
                    "<h1>Create a New Page:</h1></br>" +
                    "<form action=\"/create\">\n" +
                    "  Page Name:<br>\n" +
                    "  <input type=\"text\" name=\"pageName\" value=\"\"\"\">\n" +
                    "  <br><br>\n" +
                    "  Page Contents:<br>\n" +
                    "  <textarea type=\"text\" name=\"pageContents\" value=\"\"\"\">\n</textarea>" +
                    "  <br><br>\n" +
                    "  <input type=\"submit\" value=\"Submit\">\n" +
                    "</form> " +
//                            "<p><a href=\""+
//                            "/edit?"+
//                            pageName+
//                            "=value\">"+
//                            "Submit"+
//                            "</a></p>"+
//                            "<h1>" + pageName + ":</h1></br>" + table_results + "</br></br>"+
//                            "    <input type=\"submit\" value=\"Go to Edit\" />\n" +
                    "</br><h1>Generated by:" + WorkerNode.ownIp + " " + hostname +
                    "</h1></form>\n</html>";
//                    response = "<h1>" + pageName + ":</h1></br>" + table_results + "</br></br><input type=\"button\" onclick=\"location.href='" +editUrl+
//                            "';\" value=\"Go to Edit\" />\n" + "\n";

            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static class CreateHandler implements HttpHandler {
        final String RedirectHeader = "<!DOCTYPE HTML>\n<html lang=\"en-US\">\n<head>\n<meta charset=\"UTF-8\">\n<meta http-eq" +
                "uiv=\"refresh\" content=\"1; url=";
        final String RedirectMid = "\">\n" +
                "<script type=\"text/javascript\">\n" +
                "window.location.href = \"";
        final String RedirectFooter = "\"</script>\n" +
                "<title>Page Redirection</title>\n" +
                "</head>\n" +
                "<body></body>\n</html>";

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            System.out.println("Create Reached!!!");
            // parse request
            Map<String, Object> parameters = new HashMap<>();
            URI requestedUri = httpExchange.getRequestURI();
            String query = requestedUri.getRawQuery();
            parseQuery(query, parameters);
            // send response
            String pageName = "";
            ArrayList<String> values = new ArrayList<>(2);
            for (String key : parameters.keySet()) {
                pageName = key;
                values.add((String) parameters.get(key));
            }
            add_collection(get_table("Table1", pageName), values);
            String url = "http://" + Config.VALID_IPS.get(0) + ":8080/updated";
//            System.out.println(System.currentTimeMillis() + "Redirected to: " + RedirectHeader + url + RedirectMid + url + RedirectFooter);
//            System.out.println("Redirected to: "+RedirectHeader+url+RedirectMid+url+RedirectFooter);
            String response = RedirectHeader + url + RedirectMid + url + RedirectFooter;
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
//            System.out.println("Changed value and redirected to monitor!!");
        }
    }

}
