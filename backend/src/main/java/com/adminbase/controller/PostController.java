package com.adminbase.controller;

import com.adminbase.common.*;
import com.adminbase.domain.Post;
import com.adminbase.mapper.PostMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system/posts")
public class PostController {

    private final PostMapper posts;

    public PostController(PostMapper posts) {
        this.posts = posts;
    }

    @GetMapping
    @RequiresPerm("system:post:list")
    public R<PageResult<Post>> list(PageQ q) {
        return R.ok(PageResult.of(posts.count(q), posts.page(q)));
    }

    @GetMapping("/all")
    public R<List<Post>> all() {
        return R.ok(posts.all());
    }

    @PostMapping
    @RequiresPerm("system:post:add")
    @OperLog(module = "岗位管理", action = "新增岗位")
    public R<Void> add(@RequestBody Post p) {
        if (p.getStatus() == null) p.setStatus("0");
        posts.insert(p);
        return R.ok();
    }

    @PutMapping("/{id}")
    @RequiresPerm("system:post:edit")
    @OperLog(module = "岗位管理", action = "修改岗位")
    public R<Void> update(@PathVariable Long id, @RequestBody Post p) {
        p.setId(id);
        posts.update(p);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @RequiresPerm("system:post:remove")
    @OperLog(module = "岗位管理", action = "删除岗位")
    public R<Void> delete(@PathVariable Long id) {
        if (posts.usedCount(id) > 0) throw new BizException("岗位已分配用户,无法删除");
        posts.delete(id);
        return R.ok();
    }
}
