package com.code2cash.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller to serve the React SPA (Single Page Application)
 * 
 * Routes all non-static requests to index.html to allow React Router
 * client-side routing to work properly. This ensures that URLs like
 * /auctions, /my-bids, /leaderboard are all handled by React on the client.
 */
@Controller
public class DashboardController {
    
    /**
     * Routes root path and common SPA routes to index.html
     */
    @GetMapping({
        "/",
        "/home",
        "/dashboard",
        "/auctions",
        "/browse",
        "/my-bids",
        "/create-auction",
        "/leaderboard"
    })
    public String index() {
        return "forward:/index.html";
    }
}
