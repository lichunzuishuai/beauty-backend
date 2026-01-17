package com.lcs.beautybackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.model.entity.Systemsetting;
import com.lcs.beautybackend.service.SystemsettingService;
import com.lcs.beautybackend.mapper.SystemsettingMapper;
import org.springframework.stereotype.Service;

/**
* @author lcs
* @description 针对表【systemsetting(系统设置表)】的数据库操作Service实现
* @createDate 2026-01-14 19:20:46
*/
@Service
public class SystemsettingServiceImpl extends ServiceImpl<SystemsettingMapper, Systemsetting>
    implements SystemsettingService{

}




