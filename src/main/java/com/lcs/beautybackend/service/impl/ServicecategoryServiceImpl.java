package com.lcs.beautybackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.model.entity.Servicecategory;
import com.lcs.beautybackend.service.ServicecategoryService;
import com.lcs.beautybackend.mapper.ServicecategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author lcs
* @description 针对表【servicecategory(服务分类表)】的数据库操作Service实现
* @createDate 2026-01-14 19:20:46
*/
@Service
public class ServicecategoryServiceImpl extends ServiceImpl<ServicecategoryMapper, Servicecategory>
    implements ServicecategoryService{

}




