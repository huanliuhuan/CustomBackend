package com.adminbase.service;

import com.adminbase.common.BizException;
import com.adminbase.domain.Dept;
import com.adminbase.mapper.DeptMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeptService {

    private final DeptMapper depts;

    public DeptService(DeptMapper depts) {
        this.depts = depts;
    }

    public List<Dept> tree() {
        List<Dept> all = depts.all();
        return build(all);
    }

    public static List<Dept> build(List<Dept> all) {
        Map<Long, Dept> idx = new HashMap<Long, Dept>();
        for (Dept d : all) idx.put(d.getId(), d);
        List<Dept> roots = new ArrayList<Dept>();
        for (Dept d : all) {
            Dept p = idx.get(d.getParentId());
            if (p == null || p == d) roots.add(d);
            else p.getChildren().add(d);
        }
        return roots;
    }

    /** 从指定节点向上回溯 ancestors 串,如 0,1,3 */
    public String ancestorsOf(Long parentId) {
        if (parentId == null || parentId == 0L) return "0";
        StringBuilder sb = new StringBuilder();
        Long cur = parentId;
        int guard = 0;
        while (cur != null && cur != 0L && guard++ < 20) {
            Dept d = depts.byId(cur);
            if (d == null) break;
            sb.insert(0, cur + ",");
            cur = d.getParentId();
        }
        return "0," + sb.toString();
    }

    public void checkDelete(Long id) {
        if (depts.childCount(id) > 0) throw new BizException("存在下级部门,无法删除");
        if (depts.userCount(id) > 0) throw new BizException("部门下存在用户,无法删除");
    }
}
