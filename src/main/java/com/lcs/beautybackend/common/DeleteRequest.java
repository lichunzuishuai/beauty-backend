package com.lcs.beautybackend.common;

import lombok.Data;

import java.io.Serializable;
/*
删除请求
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * 删除的id
     */
    private Long id;


    private static final long serialVersionUID = 1L;
}
