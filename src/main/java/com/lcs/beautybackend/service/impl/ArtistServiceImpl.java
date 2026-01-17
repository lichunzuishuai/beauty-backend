package com.lcs.beautybackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lcs.beautybackend.mapper.ArtistMapper;
import com.lcs.beautybackend.model.dto.artist.ArtistQueryRequest;
import com.lcs.beautybackend.model.entity.Artist;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.ArtistVO;
import com.lcs.beautybackend.service.ArtistService;
import com.lcs.beautybackend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lcs
 * @description 针对表【artist(化妆师表)】的数据库操作Service实现
 */
@Service
public class ArtistServiceImpl extends ServiceImpl<ArtistMapper, Artist>
        implements ArtistService {

    @Resource
    private UserService userService;

    @Override
    public QueryWrapper<Artist> getQueryWrapper(ArtistQueryRequest artistQueryRequest) {
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        if (artistQueryRequest == null) {
            return queryWrapper;
        }

        String realName = artistQueryRequest.getRealName();
        String specialties = artistQueryRequest.getSpecialties();
        Integer status = artistQueryRequest.getStatus();
        Integer isRecommend = artistQueryRequest.getIsRecommend();
        String sortField = artistQueryRequest.getSortField();
        String sortOrder = artistQueryRequest.getSortOrder();

        // 模糊查询姓名
        queryWrapper.like(StrUtil.isNotBlank(realName), "realName", realName);
        // 模糊查询擅长领域
        queryWrapper.like(StrUtil.isNotBlank(specialties), "specialties", specialties);
        // 精确查询状态
        queryWrapper.eq(status != null, "status", status);
        // 精确查询推荐
        queryWrapper.eq(isRecommend != null, "isRecommend", isRecommend);
        // 评分范围
        if (artistQueryRequest.getMinRating() != null) {
            queryWrapper.ge("rating", artistQueryRequest.getMinRating());
        }
        if (artistQueryRequest.getMaxRating() != null) {
            queryWrapper.le("rating", artistQueryRequest.getMaxRating());
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            boolean isAsc = "asc".equalsIgnoreCase(sortOrder);
            queryWrapper.orderBy(true, isAsc, sortField);
        } else {
            // 默认按评分降序
            queryWrapper.orderByDesc("rating");
        }

        return queryWrapper;
    }

    @Override
    public ArtistVO getArtistVO(Artist artist) {
        if (artist == null) {
            return null;
        }
        ArtistVO artistVO = new ArtistVO();
        artistVO.setId(artist.getId());
        artistVO.setRealName(artist.getRealName());
        artistVO.setTitle(artist.getTitle());
        artistVO.setDescription(artist.getDescription());
        artistVO.setExperienceYears(artist.getExperienceYears());
        artistVO.setRating(artist.getRating());
        artistVO.setOrderCount(artist.getOrderCount());
        artistVO.setBasePrice(artist.getBasePrice());
        artistVO.setIsRecommend(artist.getIsRecommend());
        artistVO.setStatus(artist.getStatus());
        artistVO.setCreateTime(artist.getCreateTime());

        // 解析 JSON 字段
        if (StrUtil.isNotBlank(artist.getSpecialties())) {
            try {
                List<String> specialties = Arrays.asList(artist.getSpecialties().split(","));
                artistVO.setSpecialties(new ArrayList<>(specialties));
            } catch (Exception e) {
                artistVO.setSpecialties(new ArrayList<>());
            }
        }
        if (StrUtil.isNotBlank(artist.getPortfolioImages())) {
            try {
                artistVO.setPortfolioImages(JSONUtil.toList(artist.getPortfolioImages(), String.class));
            } catch (Exception e) {
                artistVO.setPortfolioImages(new ArrayList<>());
            }
        }

        // 关联用户信息（脱敏）
        Long userId = artist.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            if (user != null) {
                artistVO.setNickname(user.getNickname());
                artistVO.setAvatar(user.getAvatar());
            }
        }

        return artistVO;
    }

    @Override
    public List<ArtistVO> getArtistVOList(List<Artist> artistList) {
        if (CollUtil.isEmpty(artistList)) {
            return new ArrayList<>();
        }
        return artistList.stream()
                .map(this::getArtistVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ArtistVO> getArtistVOPage(Page<Artist> artistPage) {
        List<Artist> artistList = artistPage.getRecords();
        Page<ArtistVO> artistVOPage = new Page<>(artistPage.getCurrent(), artistPage.getSize(), artistPage.getTotal());
        if (CollUtil.isEmpty(artistList)) {
            return artistVOPage;
        }
        List<ArtistVO> artistVOList = getArtistVOList(artistList);
        artistVOPage.setRecords(artistVOList);
        return artistVOPage;
    }

    @Override
    public List<ArtistVO> listRecommendArtist(int limit) {
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("isRecommend", 1);
        queryWrapper.eq("status", 1);
        queryWrapper.orderByDesc("rating");
        queryWrapper.last("LIMIT " + limit);
        List<Artist> artistList = this.list(queryWrapper);
        return getArtistVOList(artistList);
    }
}
