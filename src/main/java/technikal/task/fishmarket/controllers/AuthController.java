package technikal.task.fishmarket.controllers;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@Log4j2
public class AuthController {

    @GetMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }

}
