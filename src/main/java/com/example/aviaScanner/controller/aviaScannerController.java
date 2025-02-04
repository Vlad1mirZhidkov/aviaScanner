package com.example.aviaScanner.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class aviaScannerController {
    @GetMapping("/main")
    public String main(){
        return "mainPage";
    }
    @GetMapping("/dates")
    public String dates(){
        return "datesPage";
    }

    @GetMapping("/hotels")
    public String hotels(){
        return "hotelsPage";
    }
}

