package com.lcs.beautybackend.service;

import com.lcs.beautybackend.model.dto.artistapplication.ArtistApplyRequest;
import com.lcs.beautybackend.model.entity.Artistapplication;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.vo.ArtistApplicationVO;

/**
 * @author lcs
 * @description 针对表【artistapplication(化妆师入驻申请表)】的数据库操作Service
 * @createDate 2026-01-14 19:20:46
 */
public interface ArtistapplicationService extends IService<Artistapplication> {

    /**
     * 提交化妆师入驻申请
     *
     * @param userId  用户ID
     * @param request 申请信息
     * @return 申请ID
     */
    Long apply(Long userId, ArtistApplyRequest request);

    /**
     * 查询我的申请状态
     *
     * @param userId 用户ID
     * @return 申请状态
     */
    ArtistApplicationVO getMyApplicationStatus(Long userId);
}
