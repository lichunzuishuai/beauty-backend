package com.lcs.beautybackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.model.dto.admin.StatusUpdateRequest;
import com.lcs.beautybackend.model.dto.service.ServiceCreateRequest;
import com.lcs.beautybackend.model.dto.service.ServiceUpdateRequest;
import com.lcs.beautybackend.model.entity.Artist;
import com.lcs.beautybackend.model.entity.Service;
import com.lcs.beautybackend.model.entity.User;
import com.lcs.beautybackend.model.vo.ServiceVO;
import com.lcs.beautybackend.service.ArtistService;
import com.lcs.beautybackend.service.ServiceService;
import com.lcs.beautybackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 化妆师服务套餐管理接口
 */
@Slf4j
@RestController
@RequestMapping("/artist/service")
@Tag(name = "化妆师服务套餐管理", description = "化妆师管理自己的服务套餐")
public class ArtistServiceController {

    @Resource
    private ServiceService serviceService;

    @Resource
    private UserService userService;

    @Resource
    private ArtistService artistService;

    /**
     * 获取当前登录的化妆师
     */
    private Artist getCurrentArtist(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // role: 0用户 1化妆师 2管理员
        if (currentUser.getRole() != 1) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有化妆师可以使用此功能");
        }
        // 查找对应的化妆师记录
        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", currentUser.getId());
        Artist artist = artistService.getOne(queryWrapper);
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师信息不存在");
        }
        return artist;
    }

    /**
     * 创建服务套餐
     */
    @PostMapping("/create")
    @Operation(summary = "创建服务套餐", description = "化妆师创建新的服务套餐")
    public BaseResponse<Long> createService(@RequestBody ServiceCreateRequest request, HttpServletRequest httpRequest) {
        Artist artist = getCurrentArtist(httpRequest);

        // 参数校验
        if (StringUtils.isBlank(request.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "套餐名称不能为空");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格必须大于0");
        }

        Service service = new Service();
        service.setArtistId(artist.getId());
        service.setCategoryId(request.getCategoryId());
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setDuration(request.getDuration());
        service.setCoverImage(request.getCoverImage());

        // 将图片列表转为JSON字符串
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            service.setImages(JSONUtil.toJsonStr(request.getImages()));
        }

        service.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        service.setStatus(1); // 默认上架
        service.setIsDeleted(0);
        service.setCreateTime(new Date());
        service.setUpdateTime(new Date());

        serviceService.save(service);
        return ResultUtils.success(service.getId());
    }

    /**
     * 修改服务套餐
     */
    @PutMapping("/update/{id}")
    @Operation(summary = "修改服务套餐", description = "修改服务套餐信息")
    public BaseResponse<Boolean> updateService(
            @Parameter(description = "服务套餐ID") @PathVariable Long id,
            @RequestBody ServiceUpdateRequest request,
            HttpServletRequest httpRequest) {
        Artist artist = getCurrentArtist(httpRequest);

        Service service = serviceService.getById(id);
        if (service == null || service.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }
        // 验证权限：只能修改自己的套餐
        if (!service.getArtistId().equals(artist.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权修改此套餐");
        }

        // 更新字段
        if (request.getCategoryId() != null) {
            service.setCategoryId(request.getCategoryId());
        }
        if (StringUtils.isNotBlank(request.getName())) {
            service.setName(request.getName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格必须大于0");
            }
            service.setPrice(request.getPrice());
        }
        if (request.getDuration() != null) {
            service.setDuration(request.getDuration());
        }
        if (request.getCoverImage() != null) {
            service.setCoverImage(request.getCoverImage());
        }
        if (request.getImages() != null) {
            service.setImages(JSONUtil.toJsonStr(request.getImages()));
        }
        if (request.getSortOrder() != null) {
            service.setSortOrder(request.getSortOrder());
        }
        service.setUpdateTime(new Date());

        serviceService.updateById(service);
        return ResultUtils.success(true);
    }

    /**
     * 上架/下架服务套餐
     */
    @PutMapping("/status/{id}")
    @Operation(summary = "上架/下架服务套餐", description = "修改服务套餐的上架状态")
    public BaseResponse<Boolean> updateServiceStatus(
            @Parameter(description = "服务套餐ID") @PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        Artist artist = getCurrentArtist(httpRequest);

        Service service = serviceService.getById(id);
        if (service == null || service.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }
        // 验证权限
        if (!service.getArtistId().equals(artist.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此套餐");
        }

        // 状态值校验: 0下架 1上架
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "状态值无效，应为0(下架)或1(上架)");
        }

        service.setStatus(request.getStatus());
        service.setUpdateTime(new Date());
        serviceService.updateById(service);

        return ResultUtils.success(true);
    }

    /**
     * 删除服务套餐（逻辑删除）
     */
    @DeleteMapping("/delete/{id}")
    @Operation(summary = "删除服务套餐", description = "删除服务套餐（逻辑删除）")
    public BaseResponse<Boolean> deleteService(
            @Parameter(description = "服务套餐ID") @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Artist artist = getCurrentArtist(httpRequest);

        Service service = serviceService.getById(id);
        if (service == null || service.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }
        // 验证权限
        if (!service.getArtistId().equals(artist.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权删除此套餐");
        }

        // 逻辑删除
        service.setIsDeleted(1);
        service.setUpdateTime(new Date());
        serviceService.updateById(service);

        return ResultUtils.success(true);
    }

    /**
     * 查询我的服务套餐列表
     */
    @GetMapping("/my/list")
    @Operation(summary = "查询我的服务套餐列表", description = "分页查询当前化妆师的所有服务套餐")
    public BaseResponse<Page<ServiceVO>> getMyServiceList(
            @Parameter(description = "状态筛选: 0下架 1上架") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest httpRequest) {
        Artist artist = getCurrentArtist(httpRequest);

        QueryWrapper<Service> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("artistId", artist.getId());
        queryWrapper.eq("isDeleted", 0);
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByAsc("sortOrder").orderByDesc("createTime");

        Page<Service> servicePage = serviceService.page(new Page<>(current, pageSize), queryWrapper);
        Page<ServiceVO> serviceVOPage = serviceService.getServiceVOPage(servicePage);

        return ResultUtils.success(serviceVOPage);
    }
}
