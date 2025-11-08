package name.saak.test.app.eml;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Controller
public class EmlController {
	private final EmlRenderService service = new EmlRenderService();

	// Upload-Seite
	@GetMapping("/")
	public String uploadForm() {
		return "upload"; // templates/upload.html
	}

	// Verarbeitet EML und zeigt Ergebnis-Seite
	@PostMapping(value = "/eml/render", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String render(@RequestPart("file") MultipartFile file, @RequestHeader(value = "X-Forwarded-Proto", required = false) String proto,
			@RequestHeader(value = "Host", required = false) String host, Model model) throws Exception {
		String mailId = UUID.randomUUID().toString();

		String scheme = proto != null ? proto : "http";
		String baseUrl = host != null ? scheme + "://" + host : scheme + "://localhost:8080";

		var res = service.render(file.getInputStream(), mailId, baseUrl);

		model.addAttribute("mailId", res.mailId());
		model.addAttribute("html", res.sanitizedHtml());
		model.addAttribute("from", res.from());
		model.addAttribute("to", res.to());
		model.addAttribute("cc", res.cc());
		model.addAttribute("date", res.date());
		model.addAttribute("subject", res.subject());
		return "eml-view"; // templates/eml-view.html
	}

	// Liefert Inline-Bilder/Parts aus
	@GetMapping("/eml/{mailId}/inline/{cid}")
	public ResponseEntity<byte[]> inline(@PathVariable String mailId, @PathVariable String cid) {
		var bin = InlineStore.get(mailId, cid);
		if (bin == null) return ResponseEntity.notFound().build();

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(bin.contentType())).cacheControl(CacheControl.noCache())
				.header("Content-Disposition", "inline; filename=\"" + cid + "\"").body(bin.data());
	}
}