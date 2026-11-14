package br.com.mutua.createchhub.auth;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

final class SessionClient implements AutoCloseable {

	private final int port;
	private final CookieManager cookies = new CookieManager();
	private final HttpClient client = HttpClient.newBuilder().cookieHandler(cookies)
			.connectTimeout(Duration.ofSeconds(5)).build();

	SessionClient(int port) {
		this.port = port;
	}

	HttpResponse<String> get(String path) throws Exception {
		return client.send(request(path).GET().build(), HttpResponse.BodyHandlers.ofString());
	}

	HttpResponse<String> post(String path, Map<String, String> fields, JsonNode csrf) throws Exception {
		var form = fields.entrySet().stream()
				.map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
				.collect(Collectors.joining("&"));
		var request = request(path).header("Content-Type", "application/x-www-form-urlencoded")
				.POST(HttpRequest.BodyPublishers.ofString(form));
		if (csrf != null) {
			request.header(csrf.path("headerName").asString(), csrf.path("token").asString());
		}
		return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

	JsonNode csrf() throws Exception {
		return JsonMapper.builder().build().readTree(get("/api/v1/admin/auth/csrf").body());
	}

	String sessionId() {
		return cookies.getCookieStore().getCookies().stream()
				.filter(cookie -> cookie.getName().equals("JSESSIONID"))
				.findFirst().orElseThrow().getValue();
	}

	@Override
	public void close() {
		client.close();
	}

	private HttpRequest.Builder request(String path) {
		return HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
				.timeout(Duration.ofSeconds(10));
	}

	private String encode(String value) {
		return URLEncoder.encode(value, StandardCharsets.UTF_8);
	}
}
