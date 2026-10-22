package com.smartqueue.controller;

import com.smartqueue.model.QueueStats;
import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.service.QueueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final QueueService queueService;

    public HomeController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/")
    public String index(
            @RequestParam(name = "tokenNumber", required = false) String tokenNumber,
            Model model) {
        model.addAttribute("appName", "WaitWise");
        model.addAttribute("tagline", "Less Waiting. More Doing.");
        model.addAttribute("services", ServiceType.values());

        QueueStats stats = queueService.getQueueStats();
        model.addAttribute("stats", stats);

        List<QueueToken> serving = queueService.getCurrentlyServing();
        model.addAttribute("servingTokens", serving);

        List<QueueToken> waiting = queueService.getWaitingTokens();
        model.addAttribute("waitingTokens", waiting);

        if (tokenNumber != null && !tokenNumber.trim().isEmpty()) {
            Optional<QueueToken> tokenOpt = queueService.findByTokenNumber(tokenNumber.trim());
            if (tokenOpt.isPresent()) {
                QueueToken token = tokenOpt.get();
                model.addAttribute("myToken", token);
                model.addAttribute("peopleAhead", queueService.getPeopleAhead(token));
                model.addAttribute("estimatedWaitMinutes", queueService.getEstimatedWaitMinutes(token));
            } else {
                model.addAttribute("searchError", "Token not found: " + tokenNumber);
            }
        }

        return "index";
    }

    @PostMapping("/tokens")
    public String issueToken(
            @RequestParam("serviceType") String serviceTypeCode,
            @RequestParam(name = "customerName", required = false) String customerName,
            RedirectAttributes redirectAttributes) {

        ServiceType serviceType = ServiceType.fromString(serviceTypeCode);
        QueueToken token = queueService.issueToken(serviceType, customerName);

        redirectAttributes.addFlashAttribute("successMessage",
                "Token " + token.getTokenNumber() + " successfully generated! Track your place in line below.");
        return "redirect:/?tokenNumber=" + token.getTokenNumber();
    }
}
