package com.lcs.beautybackend.common;

import lombok.Data;

/*
分页请求
 */
@Data
public class PageRequest {
    /*
     * 当前页码
     */
    private int current= 1;
    /*
     * 每页显示条数
     */
    private int pageSize= 10;
    /*
    排序字段
    */
    private String sortField;
    /*
    排序规则（默认升序）
    */
    private String sortOrder= "asc";
}
