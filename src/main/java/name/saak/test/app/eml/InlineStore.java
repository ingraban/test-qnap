package name.saak.test.app.eml;

import org.springframework.http.MediaType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InlineStore {
	public record Bin(byte[] data, String contentType) {}

	// mailId -> (cid -> Bin)
	private static final Map<String, Map<String, Bin>> STORAGE = new ConcurrentHashMap<>();

	public static void put(String mailId, String cid, byte[] data, String contentType) {
		STORAGE.computeIfAbsent(mailId, k -> new ConcurrentHashMap<>()).put(cid,
				new Bin(data, contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE));
	}

	public static Bin get(String mailId, String cid) {
		var m = STORAGE.get(mailId);
		return m == null ? null : m.get(cid);
	}

	public static void clear(String mailId) {
		STORAGE.remove(mailId);
	}
}