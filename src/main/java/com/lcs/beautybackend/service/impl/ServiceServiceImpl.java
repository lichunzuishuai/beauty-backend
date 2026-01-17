package com.lcs.beautybackend.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.model.entity.Service;
import com.lcs.beautybackend.model.entity.Servicecategory;
import com.lcs.beautybackend.model.vo.ServiceVO;
import com.lcs.beautybackend.service.ServiceService;
import com.lcs.beautybackend.service.ServicecategoryService;
import com.lcs.beautybackend.mapper.ServiceMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lcs
 * @description 针对表【service(服务套餐表)】的数据库操作Service实现
 * @createDate 2026-01-14 19:20:46
 */
@org.springframework.stereotype.Service
public class ServiceServiceImpl extends ServiceImpl<ServiceMapper, Service>
        implements ServiceService {

    @Resource
    private ServicecategoryService servicecategoryService;

    @Override
    public ServiceVO toServiceVO(Service service) {
        if (service == null) {
            return null;
        }
        ServiceVO serviceVO = new ServiceVO();
        BeanUtils.copyProperties(service, serviceVO);

        // 解析images JSON字符串为List
        if (StringUtils.isNotBlank(service.getImages())) {
            try {
                List<String> imageList = JSONUtil.toList(service.getImages(), String.class);
                serviceVO.setImages(imageList);
            } catch (Exception e) {
                serviceVO.setImages(new ArrayList<>());
            }
        } else {
            serviceVO.setImages(new ArrayList<>());
        }

        // 获取分类名称
        if (service.getCategoryId() != null) {
            Servicecategory category = servicecategoryService.getById(service.getCategoryId());
            if (category != null) {
                serviceVO.setCategoryName(category.getName());
            }
        }

        return serviceVO;
    }

    @Override
    public Page<ServiceVO> getServiceVOPage(Page<Service> servicePage) {
        List<ServiceVO> serviceVOList = servicePage.getRecords().stream()
                .map(this::toServiceVO)
                .collect(Collectors.toList());

        Page<ServiceVO> serviceVOPage = new Page<>(servicePage.getCurrent(), servicePage.getSize(),
                servicePage.getTotal());
        serviceVOPage.setRecords(serviceVOList);
        return serviceVOPage;
    }
}
