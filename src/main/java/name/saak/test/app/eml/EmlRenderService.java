package name.saak.test.app.eml;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.InputStream;
import java.util.*;

public class EmlRenderService {

	public record RenderResult(String mailId, String sanitizedHtml, String from, String to, String cc, String date, String subject) {}

	public RenderResult render(InputStream emlStream, String mailId, String baseUrl) throws Exception {
		Session session = Session.getInstance(System.getProperties());
		MimeMessage message = new MimeMessage(session, emlStream);

		Object content = message.getContent();
		String html = null, plain = null;

		// Sammeln von Inline-Parts (v.a. multipart/related)
		Map<String, byte[]> byCid = new HashMap<>();
		Map<String, String> ctypeByCid = new HashMap<>();

		if (content instanceof Multipart mp) {
			// Zuerst alle Inline-Parts sammeln (rekursiv)
			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart part = mp.getBodyPart(i);
				collectPart(part, byCid, ctypeByCid);
			}
			// Dann HTML und Plain extrahieren (rekursiv)
			ContentResult result = extractContent(mp);
			html = result.html;
			plain = result.plain;
		} else if (content instanceof String s) {
			if (message.isMimeType("text/html")) html = s;
			else plain = s;
		}

		if (html == null && plain != null) {
			html = "<pre>" + StringEscapeUtils.escapeHtml4(plain) + "</pre>";
		}
		if (html == null) html = "<em>(kein Inhalt)</em>";

		// InlineStore füllen und cid:… → absolute URLs umschreiben
		for (var e : byCid.entrySet()) {
			String cid = e.getKey();
			byte[] data = e.getValue();
			String ctype = ctypeByCid.getOrDefault(cid, "application/octet-stream");
			InlineStore.put(mailId, cid, data, ctype);
			// Ersetze alle Formen von cid:XYZ (mit/ohne < > in Content-ID)
			String url = baseUrl + "/eml/" + mailId + "/inline/" + cid;
			html = html.replace("cid:" + cid, url);
		}

		// HTML sanitisieren (relaxed + img-Attribute erlauben)
		Safelist safelist = Safelist.relaxed().addAttributes("img", "src", "alt", "title", "width", "height", "style").addProtocols("img", "src", "http",
				"https"); // optional: nur unsere eigenen URLs erlauben

		String sanitized = Jsoup.clean(html, safelist);

		// Header-Informationen extrahieren
		String from = message.getFrom() != null && message.getFrom().length > 0 ? message.getFrom()[0].toString() : "";
		String to = message.getRecipients(Message.RecipientType.TO) != null ? String.join(", ", Arrays.stream(message.getRecipients(Message.RecipientType.TO)).map(Object::toString).toArray(String[]::new)) : "";
		String cc = message.getRecipients(Message.RecipientType.CC) != null ? String.join(", ", Arrays.stream(message.getRecipients(Message.RecipientType.CC)).map(Object::toString).toArray(String[]::new)) : "";
		String date = message.getSentDate() != null ? message.getSentDate().toString() : "";
		String subject = message.getSubject() != null ? message.getSubject() : "";

		// Content-Security-Policy (optional in Controller-Header setzen)
		return new RenderResult(mailId, sanitized, from, to, cc, date, subject);
	}

	private record ContentResult(String html, String plain) {}

	private static ContentResult extractContent(Multipart mp) throws Exception {
		String html = null, plain = null;

		for (int i = 0; i < mp.getCount(); i++) {
			BodyPart part = mp.getBodyPart(i);

			if (part.isMimeType("text/html") && !isAttachment(part)) {
				html = (String) part.getContent();
			} else if (part.isMimeType("text/plain") && !isAttachment(part)) {
				plain = (String) part.getContent();
			} else if (part.isMimeType("multipart/*")) {
				// Rekursiv in verschachtelte Multiparts gehen
				Multipart nested = (Multipart) part.getContent();
				ContentResult nestedResult = extractContent(nested);
				if (nestedResult.html != null) html = nestedResult.html;
				if (nestedResult.plain != null) plain = nestedResult.plain;
			}
		}

		return new ContentResult(html, plain);
	}

	private static boolean isAttachment(BodyPart part) throws MessagingException {
		String disp = Optional.ofNullable(part.getDisposition()).orElse("");
		return disp.equalsIgnoreCase(Part.ATTACHMENT);
	}

	private static void collectPart(BodyPart part, Map<String, byte[]> byCid, Map<String, String> ctypeByCid) throws Exception {
		if (part.isMimeType("multipart/*")) {
			Multipart nested = (Multipart) part.getContent();
			for (int j = 0; j < nested.getCount(); j++) {
				collectPart(nested.getBodyPart(j), byCid, ctypeByCid);
			}
			return;
		}

		String[] cidHdr = part.getHeader("Content-ID");
		if (cidHdr != null && cidHdr.length > 0) {
			String cid = cidHdr[0].replace("<", "").replace(">", "");
			try (var is = part.getInputStream()) {
				byCid.put(cid, is.readAllBytes());
			}
			String ct = part.getContentType();
			if (ct != null) ctypeByCid.put(cid, ct.split(";")[0]);
		}
	}
}