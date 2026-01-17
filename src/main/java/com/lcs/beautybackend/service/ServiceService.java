package com.lcs.beautybackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.model.entity.Service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.vo.ServiceVO;

/**
 * @author lcs
 * @description 针对表【service(服务套餐表)】的数据库操作Service
 * @createDate 2026-01-14 19:20:46
 */
public interface ServiceService extends IService<Service> {

    /**
     * 将Service实体转换为ServiceVO
     */
    ServiceVO toServiceVO(Service service);

    /**
     * 将Service分页转换为ServiceVO分页
     */
    Page<ServiceVO> getServiceVOPage(Page<Service> servicePage);
}
