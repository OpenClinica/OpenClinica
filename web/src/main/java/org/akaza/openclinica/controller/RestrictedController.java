package org.akaza.openclinica.controller;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RestrictedController {



    @RequestMapping("/restricted/secret")
    public String secret(HttpServletRequest request, Model model) {
        Account account = AccountResolver.INSTANCE.getAccount(request);

        if (account == null) {
            return "redirect:/login";
        }
        System.out.println("****in SSO restricted controller");
        return "restricted/secret";
    }
}
