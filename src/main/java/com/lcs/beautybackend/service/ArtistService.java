package com.lcs.beautybackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.model.entity.Artist;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lcs.beautybackend.model.dto.artist.ArtistQueryRequest;
import com.lcs.beautybackend.model.vo.ArtistVO;

import java.util.List;

/**
 * @author lcs
 * @description 针对表【artist(化妆师表)】的数据库操作Service
 */
public interface ArtistService extends IService<Artist> {

    /**
     * 获取查询条件
     *
     * @param artistQueryRequest 查询请求
     * @return QueryWrapper
     */
    QueryWrapper<Artist> getQueryWrapper(ArtistQueryRequest artistQueryRequest);

    /**
     * 获取脱敏化妆师信息
     *
     * @param artist 化妆师
     * @return 脱敏化妆师信息
     */
    ArtistVO getArtistVO(Artist artist);

    /**
     * 获取脱敏化妆师列表
     *
     * @param artistList 化妆师列表
     * @return 脱敏化妆师列表
     */
    List<ArtistVO> getArtistVOList(List<Artist> artistList);

    /**
     * 分页获取脱敏化妆师列表
     *
     * @param artistPage 化妆师分页
     * @return 脱敏化妆师分页
     */
    Page<ArtistVO> getArtistVOPage(Page<Artist> artistPage);

    /**
     * 获取推荐化妆师列表
     *
     * @param limit 数量限制
     * @return 推荐化妆师列表
     */
    List<ArtistVO> listRecommendArtist(int limit);
}
