import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Handlers {

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

		String RedirectHeader = "<!DOCTYPE HTML>\n<html lang=\"en-US\">\n<head>\n<meta charset=\"UTF-8\">\n<meta http-eq" +
				"uiv=\"refresh\" content=\"1; url=";
		String url = "";
//        "http://example.com\;
//		String url = "http://"+Monitor.get_slave()+"/echoGet?Hello=World";
//		String url = "http://example.com";
		String RedirectMid = "\">\n" +
        "<script type=\"text/javascript\">\n" +
        "window.location.href = ";
        String RedirectFooter= "</script>\n" +
                "<title>Index Page</title>\n" +
                "</head>\n" +
                "<body></body>\n</html>";

		@Override
		public void handle(HttpExchange he) throws IOException {
			System.out.println("Root Reached!!!"+System.currentTimeMillis());
			url = "http://"+Monitor.get_slave()+"/index";
            System.out.println("Redirected to: "+RedirectHeader+url+RedirectMid+url+RedirectFooter);
            String response = RedirectHeader+url+RedirectMid+url+RedirectFooter;
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public static class GetHandler implements HttpHandler {

		String RedirectHeader = "<!DOCTYPE HTML>\n<html lang=\"en-US\">\n<head>\n<meta charset=\"UTF-8\">\n<meta http-eq" +
				"uiv=\"refresh\" content=\"1; url=";
		String url = "";
		String RedirectMid = "\">\n" +
				"<script type=\"text/javascript\">\n" +
				"window.location.href = ";
		String RedirectFooter= "</script>\n" +
				"<title>Page Redirection</title>\n" +
				"</head>\n" +
				"<body></body>\n</html>";

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
				url = "http://"+Monitor.get_slave()+"/get?"+pageName+"=value";
				String response = RedirectHeader+url+RedirectMid+url+RedirectFooter;
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
		String RedirectHeader = "<!DOCTYPE HTML>\n<html lang=\"en-US\">\n<head>\n<meta charset=\"UTF-8\">\n<meta http-eq" +
				"uiv=\"refresh\" content=\"1; url=";
		String url = "";
		//        "http://example.com\;
//		String url = "http://"+Monitor.get_slave()+"/echoGet?Hello=World";
//		String url = "http://example.com";
		String RedirectMid = "\">\n" +
				"<script type=\"text/javascript\">\n" +
				"window.location.href = ";
		String RedirectFooter= "</script>\n" +
				"<title>Page Redirection</title>\n" +
				"</head>\n" +
				"<body></body>\n</html>";

		@Override
		public void handle(HttpExchange he) throws IOException {
			System.out.println("Edit Reached!!!");
			// parse request
			Map<String, Object> parameters = new HashMap<>();
			URI requestedUri = he.getRequestURI();
			String query = requestedUri.getRawQuery();
			parseQuery(query, parameters);
			// send response
			String pageName = "";
			for (String key : parameters.keySet()) {
				pageName = key;
				break;
			}
			System.out.println("Edit reached!");
			url = "http://"+Monitor.get_slave()+"/edit?"+pageName+"=value";
			System.out.println("Redirected to: "+RedirectHeader+url+RedirectMid+url+RedirectFooter);
			String response = RedirectHeader+url+RedirectMid+url+RedirectFooter;
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}

	public static class UpdatedHandler implements HttpHandler {

		String response = "<!DOCTYPE html>\n"+
				"<html>\n"+
				"<body><h1>Changes reflected :</h1>\n"+
				"</br></br><p><a href=\"http://129.21.37.28:8080/index\">Click here to go to Index</a>\n"+
				"</p>\n"+
				"</body>\n"+
				"</html>";

		@Override
		public void handle(HttpExchange he) throws IOException {
			System.out.println("Updated Reached!!!");
			he.sendResponseHeaders(200, response.length());
			OutputStream os = he.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}
	}
}
