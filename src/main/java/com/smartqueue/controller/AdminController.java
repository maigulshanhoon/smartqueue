package com.smartqueue.controller;

import com.smartqueue.model.QueueStats;
import com.smartqueue.model.QueueToken;
import com.smartqueue.model.ServiceType;
import com.smartqueue.service.AdminAuthService;
import com.smartqueue.service.QueueService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final QueueService queueService;
    private final AdminAuthService authService;

    public AdminController(QueueService queueService, AdminAuthService authService) {
        this.queueService = queueService;
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (authService.isLoggedIn(session)) {
            return "redirect:/admin";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String doLogin(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (authService.authenticate(username, password)) {
            authService.login(session);
            return "redirect:/admin";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Invalid username or password. Check demo credentials in README.");
        return "redirect:/admin/login";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        authService.logout(session);
        redirectAttributes.addFlashAttribute("successMessage", "Logged out successfully.");
        return "redirect:/admin/login";
    }

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        if (!authService.isLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        QueueStats stats = queueService.getQueueStats();
        List<QueueToken> serving = queueService.getCurrentlyServing();
        List<QueueToken> waiting = queueService.getWaitingTokens();
        List<QueueToken> recent = queueService.getRecentTokens();

        model.addAttribute("stats", stats);
        model.addAttribute("servingTokens", serving);
        model.addAttribute("waitingTokens", waiting);
        model.addAttribute("recentTokens", recent);
        model.addAttribute("services", ServiceType.values());

        return "admin/dashboard";
    }

    @PostMapping("/call-next")
    public String callNext(
            @RequestParam(name = "counterName", defaultValue = "Counter 1") String counterName,
            @RequestParam(name = "serviceType", required = false) String serviceTypeCode,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!authService.isLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        ServiceType filter = (serviceTypeCode != null && !serviceTypeCode.isBlank())
                ? ServiceType.fromString(serviceTypeCode)
                : null;

        Optional<QueueToken> called = queueService.callNext(counterName, filter);
        if (called.isPresent()) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Called token " + called.get().getTokenNumber() + " to " + counterName + "!");
        } else {
            redirectAttributes.addFlashAttribute("infoMessage",
                    "No waiting tokens found for " + (filter != null ? filter.getDisplayName() : "any service") + ".");
        }

        return "redirect:/admin";
    }

    @PostMapping("/tokens/{id}/call")
    public String callSpecificToken(
            @PathVariable Long id,
            @RequestParam(name = "counterName", defaultValue = "Counter 1") String counterName,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!authService.isLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        try {
            QueueToken token = queueService.callSpecificToken(id, counterName);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Token " + token.getTokenNumber() + " called to " + counterName + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/tokens/{id}/complete")
    public String completeToken(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!authService.isLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        try {
            QueueToken token = queueService.completeToken(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Token " + token.getTokenNumber() + " marked as COMPLETED.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin";
    }

    @PostMapping("/tokens/{id}/skip")
    public String skipToken(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!authService.isLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        try {
            QueueToken token = queueService.skipToken(id);
            redirectAttributes.addFlashAttribute("infoMessage",
                    "Token " + token.getTokenNumber() + " marked as SKIPPED.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin";
    }
}
