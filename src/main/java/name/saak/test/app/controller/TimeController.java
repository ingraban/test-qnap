package name.saak.test.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class TimeController {

    @GetMapping("/time")
    public String showCurrentTime(Model model) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedTime = now.format(formatter);
        
        model.addAttribute("currentTime", formattedTime);
        return "time";
    }
}