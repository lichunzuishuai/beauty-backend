package com.lcs.beautybackend.controller;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lcs.beautybackend.common.BaseResponse;
import com.lcs.beautybackend.common.ResultUtils;
import com.lcs.beautybackend.exception.BusinessException;
import com.lcs.beautybackend.exception.ErrorCode;
import com.lcs.beautybackend.model.dto.admin.*;
import com.lcs.beautybackend.model.entity.*;
import com.lcs.beautybackend.model.vo.*;
import com.lcs.beautybackend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 管理员接口
 */
@Tag(name = "管理员接口", description = "管理员后台相关接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final ArtistService artistService;
    private final ArtistapplicationService artistapplicationService;
    private final AppointmentService appointmentService;
    private final ReviewService reviewService;
    private final ServiceService serviceService;

    /**
     * 盐值，用于密码加密
     */
    private static final String SALT = "beauty_salt_2026";

    /**
     * 检查管理员权限
     */
    private void checkAdmin(HttpServletRequest request) {
        User currentUser = userService.getLoginUser(request);
        // role: 0用户 1化妆师 2管理员
        if (currentUser == null || currentUser.getRole() != 2) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无管理员权限");
        }
    }

    // ======================== 统计接口 ========================

    @Operation(summary = "获取统计数据")
    @GetMapping("/stats")
    public BaseResponse<AdminStatsVO> getStats(HttpServletRequest request) {
        checkAdmin(request);

        AdminStatsVO stats = new AdminStatsVO();
        stats.setUserCount(userService.count());
        stats.setArtistCount(artistService.count());
        stats.setAppointmentCount(appointmentService.count());

        // 待审核申请数
        QueryWrapper<Artistapplication> appQuery = new QueryWrapper<>();
        appQuery.eq("status", 0);
        stats.setPendingApplications(artistapplicationService.count(appQuery));

        // 待确认预约数
        QueryWrapper<Appointment> pendingQuery = new QueryWrapper<>();
        pendingQuery.eq("status", 1);
        stats.setPendingAppointments(appointmentService.count(pendingQuery));

        return ResultUtils.success(stats);
    }

    // ======================== 用户管理 ========================

    @Operation(summary = "查询用户列表")
    @GetMapping("/user/list")
    public BaseResponse<Page<User>> getUserList(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        checkAdmin(request);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        if (StringUtils.isNotBlank(phone)) {
            queryWrapper.like("phone", phone);
        }
        if (StringUtils.isNotBlank(role)) {
            queryWrapper.eq("role", parseRole(role));
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("createTime");

        Page<User> page = userService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    @Operation(summary = "创建用户")
    @PostMapping("/user/create")
    public BaseResponse<Long> createUser(@RequestBody AdminUserCreateRequest request, HttpServletRequest httpRequest) {
        checkAdmin(httpRequest);

        if (StringUtils.isBlank(request.getUsername()) || StringUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名和密码不能为空");
        }

        // 检查用户名是否已存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", request.getUsername());
        if (userService.count(queryWrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名已存在");
        }

        String password = DigestUtil.md5Hex(SALT + request.getPassword());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(password);
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setRole(parseRole(request.getRole()));
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());

        userService.save(user);
        return ResultUtils.success(user.getId());
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/user/update/{id}")
    public BaseResponse<Boolean> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUserUpdateRequest request,
            HttpServletRequest httpRequest) {
        checkAdmin(httpRequest);

        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        if (StringUtils.isNotBlank(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (StringUtils.isNotBlank(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.isNotBlank(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }
        if (StringUtils.isNotBlank(request.getRole())) {
            user.setRole(parseRole(request.getRole()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setUpdateTime(new Date());

        userService.updateById(user);
        return ResultUtils.success(true);
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/user/status/{id}")
    public BaseResponse<Boolean> updateUserStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        checkAdmin(httpRequest);

        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        user.setStatus(request.getStatus());
        user.setUpdateTime(new Date());
        userService.updateById(user);

        return ResultUtils.success(true);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/user/delete/{id}")
    public BaseResponse<Boolean> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        userService.removeById(id);
        return ResultUtils.success(true);
    }

    // ======================== 化妆师管理 ========================
    @Operation(summary = "查询化妆师列表")
    @GetMapping("/artist/list")
    public BaseResponse<Page<Artist>> getArtistList(
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isRecommend,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        checkAdmin(request);

        QueryWrapper<Artist> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(realName)) {
            queryWrapper.like("realName", realName);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        if (isRecommend != null) {
            queryWrapper.eq("isRecommend", isRecommend);
        }
        queryWrapper.orderByDesc("createTime");

        Page<Artist> page = artistService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    @Operation(summary = "获取化妆师详情")
    @GetMapping("/artist/{id}")
    public BaseResponse<Artist> getArtistDetail(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Artist artist = artistService.getById(id);
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师不存在");
        }

        return ResultUtils.success(artist);
    }

    @Operation(summary = "修改化妆师状态")
    @PutMapping("/artist/status/{id}")
    public BaseResponse<Boolean> updateArtistStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        checkAdmin(httpRequest);

        Artist artist = artistService.getById(id);
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师不存在");
        }

        artist.setStatus(request.getStatus());
        artist.setUpdateTime(new Date());
        artistService.updateById(artist);

        return ResultUtils.success(true);
    }

    @Operation(summary = "设置推荐化妆师")
    @PutMapping("/artist/recommend/{id}")
    public BaseResponse<Boolean> setArtistRecommend(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            HttpServletRequest httpRequest) {
        checkAdmin(httpRequest);

        Artist artist = artistService.getById(id);
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师不存在");
        }

        artist.setIsRecommend(request.getStatus());
        artist.setUpdateTime(new Date());
        artistService.updateById(artist);

        return ResultUtils.success(true);
    }

    @Operation(summary = "更新化妆师信息")
    @PutMapping("/artist/update/{id}")
    public BaseResponse<Boolean> updateArtist(
            @PathVariable Long id,
            @RequestBody AdminArtistUpdateRequest request,
            HttpServletRequest httpRequest) {
        checkAdmin(httpRequest);

        Artist artist = artistService.getById(id);
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师不存在");
        }

        if (StringUtils.isNotBlank(request.getRealName())) {
            artist.setRealName(request.getRealName());
        }
        if (StringUtils.isNotBlank(request.getSpecialty())) {
            artist.setSpecialties(request.getSpecialty());
        }
        if (request.getExperienceYears() != null) {
            artist.setExperienceYears(request.getExperienceYears());
        }
        if (StringUtils.isNotBlank(request.getIntroduction())) {
            artist.setDescription(request.getIntroduction());
        }
        if (StringUtils.isNotBlank(request.getPortfolio())) {
            artist.setPortfolioImages(request.getPortfolio());
        }
        if (StringUtils.isNotBlank(request.getAvatar())) {
            artist.setAvatar(request.getAvatar());
        }
        artist.setUpdateTime(new Date());
        artistService.updateById(artist);

        return ResultUtils.success(true);
    }

    @Operation(summary = "删除化妆师")
    @DeleteMapping("/artist/delete/{id}")
    public BaseResponse<Boolean> deleteArtist(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Artist artist = artistService.getById(id);
        if (artist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "化妆师不存在");
        }

        artistService.removeById(id);
        return ResultUtils.success(true);
    }

    // ======================== 入驻审核 ========================

    @Operation(summary = "查询入驻申请列表")
    @GetMapping("/artist/application/list")
    public BaseResponse<Page<Artistapplication>> getApplicationList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        checkAdmin(request);

        QueryWrapper<Artistapplication> queryWrapper = new QueryWrapper<>();
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("createTime");

        Page<Artistapplication> page = artistapplicationService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    @Operation(summary = "获取申请详情")
    @GetMapping("/artist/application/{id}")
    public BaseResponse<Artistapplication> getApplicationDetail(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Artistapplication application = artistapplicationService.getById(id);
        if (application == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }

        return ResultUtils.success(application);
    }

    @Operation(summary = "审核通过")
    @PostMapping("/artist/application/approve/{id}")
    public BaseResponse<Boolean> approveApplication(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Artistapplication application = artistapplicationService.getById(id);
        if (application == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该申请已处理");
        }

        // 更新申请状态
        application.setStatus(1);
        application.setUpdateTime(new Date());
        artistapplicationService.updateById(application);

        // 更新用户角色为化妆师
        User user = userService.getById(application.getUserId());
        if (user != null) {
            user.setRole(1); // 1=化妆师
            user.setUpdateTime(new Date());
            userService.updateById(user);
        }

        // 创建化妆师记录
        Artist artist = new Artist();
        artist.setUserId(application.getUserId());
        artist.setRealName(application.getRealName());
        artist.setExperienceYears(application.getExperienceYears());
        artist.setSpecialties(application.getSpecialties());
        artist.setDescription(application.getIntroduction());
        artist.setStatus(1);
        artist.setIsRecommend(0);
        artist.setCreateTime(new Date());
        artist.setUpdateTime(new Date());
        artistService.save(artist);

        return ResultUtils.success(true);
    }

    @Operation(summary = "审核拒绝")
    @PostMapping("/artist/application/reject/{id}")
    public BaseResponse<Boolean> rejectApplication(
            @PathVariable Long id,
            @RequestBody RejectRequest rejectRequest,
            HttpServletRequest request) {
        checkAdmin(request);

        Artistapplication application = artistapplicationService.getById(id);
        if (application == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "申请不存在");
        }
        if (application.getStatus() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该申请已处理");
        }

        application.setStatus(2);
        application.setRejectReason(rejectRequest.getRejectReason());
        application.setUpdateTime(new Date());
        artistapplicationService.updateById(application);

        return ResultUtils.success(true);
    }

    // ======================== 预约管理 ========================

    @Operation(summary = "查询预约列表")
    @GetMapping("/appointment/list")
    public BaseResponse<Page<Appointment>> getAppointmentList(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        checkAdmin(request);

        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(orderNo)) {
            queryWrapper.like("orderNo", orderNo);
        }
        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }
        if (artistId != null) {
            queryWrapper.eq("artistId", artistId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByDesc("createTime");

        Page<Appointment> page = appointmentService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    @Operation(summary = "获取预约详情")
    @GetMapping("/appointment/{id}")
    public BaseResponse<Appointment> getAppointmentDetail(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Appointment appointment = appointmentService.getById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约不存在");
        }

        return ResultUtils.success(appointment);
    }

    // ======================== 评价管理 ========================

    @Operation(summary = "查询评价列表")
    @GetMapping("/review/list")
    public BaseResponse<Page<Review>> getReviewList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        checkAdmin(request);

        QueryWrapper<Review> queryWrapper = new QueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq("userId", userId);
        }
        if (artistId != null) {
            queryWrapper.eq("artistId", artistId);
        }
        if (rating != null) {
            queryWrapper.eq("rating", rating);
        }
        queryWrapper.orderByDesc("createTime");

        Page<Review> page = reviewService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(page);
    }

    @Operation(summary = "删除评价")
    @DeleteMapping("/review/delete/{id}")
    public BaseResponse<Boolean> deleteReview(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Review review = reviewService.getById(id);
        if (review == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "评价不存在");
        }

        reviewService.removeById(id);
        return ResultUtils.success(true);
    }

    // ======================== 服务套餐管理 ========================

    @Operation(summary = "查询服务套餐列表")
    @GetMapping("/service/list")
    public BaseResponse<Page<ServiceVO>> getServiceList(
            @RequestParam(required = false) Long artistId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        checkAdmin(request);

        QueryWrapper<Service> queryWrapper = new QueryWrapper<>();
        if (artistId != null) {
            queryWrapper.eq("artistId", artistId);
        }
        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like("name", name);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.eq("isDeleted", 0);
        queryWrapper.orderByDesc("createTime");

        Page<Service> servicePage = serviceService.page(new Page<>(current, pageSize), queryWrapper);
        return ResultUtils.success(serviceService.getServiceVOPage(servicePage));
    }

    @Operation(summary = "获取服务套餐详情")
    @GetMapping("/service/{id}")
    public BaseResponse<ServiceVO> getServiceDetail(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Service service = serviceService.getById(id);
        if (service == null || service.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }

        return ResultUtils.success(serviceService.toServiceVO(service));
    }

    @Operation(summary = "创建服务套餐")
    @PostMapping("/service/create")
    public BaseResponse<Long> createService(
            @RequestBody com.lcs.beautybackend.model.dto.service.ServiceCreateRequest createRequest,
            HttpServletRequest request) {
        checkAdmin(request);

        if (StringUtils.isBlank(createRequest.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "套餐名称不能为空");
        }
        if (createRequest.getPrice() == null || createRequest.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "价格必须大于0");
        }
        if (createRequest.getArtistId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "化妆师ID不能为空");
        }

        Service service = new Service();
        service.setArtistId(createRequest.getArtistId());
        service.setCategoryId(createRequest.getCategoryId());
        service.setName(createRequest.getName());
        service.setDescription(createRequest.getDescription());
        service.setPrice(createRequest.getPrice());
        service.setDuration(createRequest.getDuration());
        service.setCoverImage(createRequest.getCoverImage());
        if (createRequest.getImages() != null && !createRequest.getImages().isEmpty()) {
            service.setImages(cn.hutool.json.JSONUtil.toJsonStr(createRequest.getImages()));
        }
        service.setSortOrder(createRequest.getSortOrder() != null ? createRequest.getSortOrder() : 0);
        service.setStatus(1);
        service.setIsDeleted(0);
        service.setCreateTime(new Date());
        service.setUpdateTime(new Date());

        serviceService.save(service);
        return ResultUtils.success(service.getId());
    }

    @Operation(summary = "修改服务套餐")
    @PutMapping("/service/update/{id}")
    public BaseResponse<Boolean> updateService(
            @PathVariable Long id,
            @RequestBody com.lcs.beautybackend.model.dto.service.ServiceUpdateRequest updateRequest,
            HttpServletRequest request) {
        checkAdmin(request);

        Service service = serviceService.getById(id);
        if (service == null || service.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }

        if (updateRequest.getCategoryId() != null) {
            service.setCategoryId(updateRequest.getCategoryId());
        }
        if (StringUtils.isNotBlank(updateRequest.getName())) {
            service.setName(updateRequest.getName());
        }
        if (updateRequest.getDescription() != null) {
            service.setDescription(updateRequest.getDescription());
        }
        if (updateRequest.getPrice() != null) {
            service.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getDuration() != null) {
            service.setDuration(updateRequest.getDuration());
        }
        if (updateRequest.getCoverImage() != null) {
            service.setCoverImage(updateRequest.getCoverImage());
        }
        if (updateRequest.getImages() != null) {
            service.setImages(cn.hutool.json.JSONUtil.toJsonStr(updateRequest.getImages()));
        }
        if (updateRequest.getSortOrder() != null) {
            service.setSortOrder(updateRequest.getSortOrder());
        }
        service.setUpdateTime(new Date());

        serviceService.updateById(service);
        return ResultUtils.success(true);
    }

    @Operation(summary = "修改服务套餐状态")
    @PutMapping("/service/status/{id}")
    public BaseResponse<Boolean> updateServiceStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest statusRequest,
            HttpServletRequest request) {
        checkAdmin(request);

        Service service = serviceService.getById(id);
        if (service == null || service.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }

        service.setStatus(statusRequest.getStatus());
        service.setUpdateTime(new Date());
        serviceService.updateById(service);

        return ResultUtils.success(true);
    }

    @Operation(summary = "删除服务套餐")
    @DeleteMapping("/service/delete/{id}")
    public BaseResponse<Boolean> deleteService(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);

        Service service = serviceService.getById(id);
        if (service == null || service.getIsDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "服务套餐不存在");
        }

        service.setIsDeleted(1);
        service.setUpdateTime(new Date());
        serviceService.updateById(service);

        return ResultUtils.success(true);
    }

    /**
     * 将角色字符串转为数字
     */
    private Integer parseRole(String role) {
        if (role == null)
            return 0;
        return switch (role.toLowerCase()) {
            case "admin" -> 2;
            case "artist" -> 1;
            default -> 0;
        };
    }
}
