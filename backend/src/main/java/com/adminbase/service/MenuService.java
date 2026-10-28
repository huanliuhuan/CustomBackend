package com.adminbase.service;

import com.adminbase.domain.Menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuService {

    /** 按 parentId 组装菜单树,返回根节点列表 */
    public static java.util.List<Menu> tree(List<Menu> all) {
        Map<Long, Menu> idx = new HashMap<Long, Menu>();
        for (Menu m : all) idx.put(m.getId(), m);
        java.util.List<Menu> roots = new java.util.ArrayList<Menu>();
        for (Menu m : all) {
            Menu p = idx.get(m.getParentId());
            if (p == null || p == m) roots.add(m);
            else p.getChildren().add(m);
        }
        return roots;
    }
}
