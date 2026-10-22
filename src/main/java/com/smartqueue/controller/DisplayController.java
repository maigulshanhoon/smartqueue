package com.smartqueue.controller;

import com.smartqueue.model.QueueStats;
import com.smartqueue.model.QueueToken;
import com.smartqueue.service.QueueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DisplayController {

    private final QueueService queueService;

    public DisplayController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/display")
    public String display(Model model) {
        model.addAttribute("appName", "SmartQueue");

        QueueStats stats = queueService.getQueueStats();
        model.addAttribute("stats", stats);

        List<QueueToken> serving = queueService.getCurrentlyServing();
        model.addAttribute("servingTokens", serving);

        List<QueueToken> waiting = queueService.getWaitingTokens();
        model.addAttribute("waitingTokens", waiting);

        return "display";
    }
}
