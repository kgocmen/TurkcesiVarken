package com.kaan.turkcesivarken.controller;

import com.kaan.turkcesivarken.service.NisanyanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/nisanyan")
public class NisanyanController {

    @Autowired
    private NisanyanService nisanyanService;

    /*
    --------------------------------
    GET WORD
    --------------------------------
    */

    @GetMapping("/{word}")
    public Map<String, Object> getWord(
            @PathVariable String word
    ) {

        return nisanyanService
                .fetchWord(word);

    }

}