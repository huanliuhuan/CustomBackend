package com.adminbase.controller;

import com.adminbase.common.R;
import com.adminbase.mapper.CodeGenMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tool/codegen")
public class CodeGenController {

    private final CodeGenMapper gen;

    public CodeGenController(CodeGenMapper gen) {
        this.gen = gen;
    }

    @GetMapping("/tables")
    public R<List<Map<String, Object>>> tables(String kw) {
        return R.ok(gen.tables(kw));
    }

    @GetMapping("/tables/{name}/columns")
    public R<List<Map<String, Object>>> columns(@PathVariable String name) {
        return R.ok(gen.columns(name));
    }
}
